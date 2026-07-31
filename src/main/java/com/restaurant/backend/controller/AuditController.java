package com.restaurant.backend.controller;

import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.PageResponse;
import com.restaurant.backend.entity.AuditLog;
import com.restaurant.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Page<AuditLog> auditPage = module != null 
                ? auditService.getLogsByModule(module, PageRequest.of(page, size, sort))
                : auditService.getAllLogs(PageRequest.of(page, size, sort));

        PageResponse<AuditLog> response = new PageResponse<>(
                auditPage.getContent(), auditPage.getNumber(), auditPage.getSize(),
                auditPage.getTotalElements(), auditPage.getTotalPages(), auditPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", response));
    }
}
