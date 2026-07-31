package com.restaurant.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username, Email, or Mobile Number is required")
        private String usernameOrEmailOrMobile;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number format")
        private String mobileNumber;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Full name is required")
        private String fullName;

        private String role; // Optional: e.g. "ROLE_CUSTOMER"
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
}
