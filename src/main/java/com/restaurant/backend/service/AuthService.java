package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.AuthDto;
import com.restaurant.backend.dto.response.JwtAuthResponse;
import com.restaurant.backend.dto.response.UserDto;
import com.restaurant.backend.entity.LoginHistory;
import com.restaurant.backend.entity.RefreshToken;
import com.restaurant.backend.entity.Role;
import com.restaurant.backend.entity.User;
import com.restaurant.backend.enums.RoleName;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.exception.UnauthorizedException;
import com.restaurant.backend.repository.LoginHistoryRepository;
import com.restaurant.backend.repository.RefreshTokenRepository;
import com.restaurant.backend.repository.RoleRepository;
import com.restaurant.backend.repository.UserRepository;
import com.restaurant.backend.repository.EmployeeRepository;
import com.restaurant.backend.entity.Employee;
import com.restaurant.backend.security.JwtTokenProvider;
import com.restaurant.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;
    private final EmployeeRepository employeeRepository;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenDurationMs;

    @Value("${app.security.max-failed-login-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.account-lock-duration-minutes:30}")
    private int lockDurationMinutes;

    @Transactional
    public UserDto registerUser(AuthDto.RegisterRequest request, String ipAddress) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email Address is already in use!");
        }
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BadRequestException("Mobile number is already registered!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .accountNonLocked(true)
                .failedAttempt(0)
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        RoleName roleName = RoleName.ROLE_CUSTOMER;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                roleName = RoleName.valueOf(request.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role specified: " + request.getRole());
            }
        }

        final RoleName finalRoleName = roleName;
        Role userRole = roleRepository.findByName(finalRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", finalRoleName.name()));
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);

        Employee employee = Employee.builder()
                .user(savedUser)
                .employeeCode(savedUser.getUsername())
                .department("Operations")
                .designation(finalRoleName.name())
                .baseSalary(request.getBaseSalary() != null ? java.math.BigDecimal.valueOf(request.getBaseSalary()) : java.math.BigDecimal.ZERO)
                .joiningDate(java.time.LocalDate.now())
                .status(com.restaurant.backend.enums.EmployeeStatus.ACTIVE)
                .build();
        employeeRepository.save(employee);

        auditService.logAction(savedUser.getUsername(), roleName.name(), "USER_REGISTER", "AUTH",
                "User registered with email: " + savedUser.getEmail(), ipAddress);

        return mapUserToDto(savedUser);
    }

    @Transactional
    public JwtAuthResponse login(AuthDto.LoginRequest request, String ipAddress, String userAgent) {
        String loginVal = request.getUsernameOrEmailOrMobile();

        User user = userRepository.findByUsernameOrEmailOrMobileNumber(loginVal)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Account Lock Check
        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now())) {
                user.setAccountNonLocked(true);
                user.setFailedAttempt(0);
                user.setLockTime(null);
                userRepository.save(user);
            } else {
                recordLoginHistory(user.getId(), user.getUsername(), ipAddress, userAgent, "FAILED", "Account Locked");
                throw new LockedException("Account is locked due to repeated failed login attempts. Try again later.");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );

            // Reset failed attempt on success
            if (user.getFailedAttempt() > 0) {
                user.setFailedAttempt(0);
                userRepository.save(user);
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = tokenProvider.generateToken(authentication);
            RefreshToken refreshToken = createRefreshToken(user);

            recordLoginHistory(user.getId(), user.getUsername(), ipAddress, userAgent, "SUCCESS", null);
            auditService.logAction(user.getUsername(), getRolesString(user), "USER_LOGIN", "AUTH", "Login successful", ipAddress);

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return JwtAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .id(userPrincipal.getId())
                    .username(userPrincipal.getUsername())
                    .email(userPrincipal.getEmail())
                    .mobileNumber(userPrincipal.getMobileNumber())
                    .fullName(userPrincipal.getFullName())
                    .roles(roles)
                    .build();

        } catch (BadCredentialsException ex) {
            int attempts = user.getFailedAttempt() + 1;
            user.setFailedAttempt(attempts);
            if (attempts >= maxFailedAttempts) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());
                recordLoginHistory(user.getId(), user.getUsername(), ipAddress, userAgent, "FAILED", "Account Locked on 5th attempt");
            } else {
                recordLoginHistory(user.getId(), user.getUsername(), ipAddress, userAgent, "FAILED", "Bad credentials (" + attempts + "/" + maxFailedAttempts + ")");
            }
            userRepository.save(user);
            throw ex;
        }
    }

    @Transactional
    public JwtAuthResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(token -> {
                    if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
                        refreshTokenRepository.delete(token);
                        throw new UnauthorizedException("Refresh token was expired or revoked. Please log in again.");
                    }
                    User user = token.getUser();
                    token.setRevoked(true); // Token Rotation
                    refreshTokenRepository.save(token);

                    RefreshToken newRefreshToken = createRefreshToken(user);
                    String newAccessToken = tokenProvider.generateTokenFromUsername(user.getUsername());

                    List<String> roles = user.getRoles().stream()
                            .map(r -> r.getName().name())
                            .collect(Collectors.toList());

                    return JwtAuthResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(newRefreshToken.getToken())
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .mobileNumber(user.getMobileNumber())
                            .fullName(user.getFullName())
                            .roles(roles)
                            .build();
                })
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private void recordLoginHistory(Long userId, String username, String ipAddress, String userAgent, String status, String failureReason) {
        LoginHistory history = LoginHistory.builder()
                .userId(userId)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .failureReason(failureReason)
                .loginTime(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(history);
    }

    private String getRolesString(User user) {
        return user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.joining(","));
    }

    private UserDto mapUserToDto(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .fullName(user.getFullName())
                .accountNonLocked(user.isAccountNonLocked())
                .enabled(user.isEnabled())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
