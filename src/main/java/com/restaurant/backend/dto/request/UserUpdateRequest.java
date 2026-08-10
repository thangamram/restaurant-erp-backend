package com.restaurant.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String role;
    
    private Boolean enabled;
}
