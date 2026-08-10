package com.restaurant.backend.service;

import com.restaurant.backend.dto.response.UserDto;
import com.restaurant.backend.entity.User;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.UserRepository;
import com.restaurant.backend.repository.RoleRepository;
import com.restaurant.backend.repository.EmployeeRepository;
import com.restaurant.backend.entity.Employee;
import com.restaurant.backend.entity.Role;
import com.restaurant.backend.enums.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDto);
    }

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public UserDto updateUser(Long id, com.restaurant.backend.dto.request.UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getRole() != null && !request.getRole().isEmpty()) {
            RoleName roleName = RoleName.valueOf(request.getRole());
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            java.util.Set<Role> newRoles = new java.util.HashSet<>();
            newRoles.add(role);
            user.setRoles(newRoles);

            Employee employee = employeeRepository.findByUserId(user.getId()).orElseGet(() -> {
                Employee newEmp = Employee.builder()
                        .user(user)
                        .employeeCode(user.getUsername())
                        .department("Operations")
                        .designation(roleName.name())
                        .baseSalary(java.math.BigDecimal.ZERO)
                        .joiningDate(java.time.LocalDate.now())
                        .status(com.restaurant.backend.enums.EmployeeStatus.ACTIVE)
                        .build();
                return newEmp;
            });
            
            employee.setDesignation(roleName.name());
            if (request.getBaseSalary() != null) {
                employee.setBaseSalary(java.math.BigDecimal.valueOf(request.getBaseSalary()));
            }
            employeeRepository.save(employee);
        } else {
            Employee employee = employeeRepository.findByUserId(user.getId()).orElseGet(() -> {
                Employee newEmp = Employee.builder()
                        .user(user)
                        .employeeCode(user.getUsername())
                        .department("Operations")
                        .designation("ROLE_WAITER")
                        .baseSalary(java.math.BigDecimal.ZERO)
                        .joiningDate(java.time.LocalDate.now())
                        .status(com.restaurant.backend.enums.EmployeeStatus.ACTIVE)
                        .build();
                return newEmp;
            });
            
            if (request.getBaseSalary() != null) {
                employee.setBaseSalary(java.math.BigDecimal.valueOf(request.getBaseSalary()));
                employeeRepository.save(employee);
            }
        }

        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Remove roles first to satisfy foreign key constraints (handled by JPA cascades, but good practice)
        user.getRoles().clear();
        userRepository.save(user);
        
        userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        UserDto dto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .fullName(user.getFullName())
                .accountNonLocked(user.isAccountNonLocked())
                .enabled(user.isEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();

        employeeRepository.findByUserId(user.getId()).ifPresent(employee -> {
            if (employee.getBaseSalary() != null) {
                dto.setBaseSalary(employee.getBaseSalary().doubleValue());
            }
            // employmentType is not in the DB, so we can ignore it or leave it null.
        });

        return dto;
    }
}