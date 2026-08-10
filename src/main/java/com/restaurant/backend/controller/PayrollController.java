package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.PayrollDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.PayrollService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PayrollDto.PayrollResponse>> generatePayroll(
            @Valid @RequestBody PayrollDto.PayrollRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Payroll generated successfully",
                payrollService.generatePayroll(request, currentUser.getUsername(), ipAddress)), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<PayrollDto.PayrollResponse>>> getAllPayrolls() {
        return ResponseEntity.ok(ApiResponse.success("Payrolls fetched successfully", payrollService.getAllPayrolls()));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER', 'ROLE_WAITER', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<List<PayrollDto.PayrollResponse>>> getPayrollsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Payrolls fetched successfully", payrollService.getPayrollsByEmployee(employeeId)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PayrollDto.PayrollResponse>> updatePayrollStatus(
            @PathVariable Long id,
            @RequestParam com.restaurant.backend.enums.PayrollStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payroll status updated successfully",
                payrollService.updatePayrollStatus(id, status, currentUser.getUsername(), ipAddress)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePayroll(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        payrollService.deletePayroll(id, currentUser.getUsername(), ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Payroll deleted successfully"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
