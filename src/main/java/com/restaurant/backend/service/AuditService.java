package com.restaurant.backend.service;

import com.restaurant.backend.entity.AuditLog;
import com.restaurant.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logAction(String username, String userRole, String action, String module, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .username(username != null ? username : "ANONYMOUS")
                .userRole(userRole != null ? userRole : "ROLE_PUBLIC")
                .action(action)
                .module(module)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLog> getLogsByModule(String module, org.springframework.data.domain.Pageable pageable) {
        return auditLogRepository.findByModuleOrderByTimestampDesc(module, pageable);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLog> getAllLogs(org.springframework.data.domain.Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
}
