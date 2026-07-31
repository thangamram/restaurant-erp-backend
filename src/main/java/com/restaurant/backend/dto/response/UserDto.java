package com.restaurant.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String mobileNumber;
    private String fullName;
    private boolean accountNonLocked;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
