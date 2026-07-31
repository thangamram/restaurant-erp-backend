package com.restaurant.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDto {
    private Long id;
    private String username;
    private String userRole;
    private String action;
    private String module;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
}
