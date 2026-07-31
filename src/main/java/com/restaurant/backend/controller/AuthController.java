package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.AuthDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.JwtAuthResponse;
import com.restaurant.backend.dto.response.UserDto;
import com.restaurant.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@Valid @RequestBody AuthDto.RegisterRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        UserDto user = authService.registerUser(request, ipAddress);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody AuthDto.LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        JwtAuthResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> refreshToken(@Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        JwtAuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
