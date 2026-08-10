package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.EmployeeDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.PageResponse;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeDto.EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Page<EmployeeDto.EmployeeResponse> employeePage = employeeService.getAllEmployees(PageRequest.of(page, size, sort));

        PageResponse<EmployeeDto.EmployeeResponse> response = new PageResponse<>(
                employeePage.getContent(), employeePage.getNumber(), employeePage.getSize(),
                employeePage.getTotalElements(), employeePage.getTotalPages(), employeePage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER', 'ROLE_WAITER', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<List<EmployeeDto.EmployeeResponse>>> getActiveEmployees() {
        return ResponseEntity.ok(ApiResponse.success("Active employees fetched successfully", employeeService.getActiveEmployees()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDto.EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", employeeService.getEmployeeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDto.EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeDto.EmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Employee created successfully", 
                employeeService.createEmployee(request, currentUser.getUsername(), ipAddress)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDto.EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto.EmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", 
                employeeService.updateEmployee(id, request, currentUser.getUsername(), ipAddress)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        employeeService.deleteEmployee(id, currentUser.getUsername(), ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully"));
    }

    // -------------------------------- Shifts --------------------------------

    @PostMapping("/shifts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDto.ShiftResponse>> assignShift(
            @Valid @RequestBody EmployeeDto.ShiftRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Shift assigned successfully", 
                employeeService.assignShift(request, currentUser.getUsername(), ipAddress)), HttpStatus.CREATED);
    }

    @GetMapping("/{employeeId}/shifts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeDto.ShiftResponse>>> getShiftsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Shifts fetched successfully", employeeService.getShiftsByEmployee(employeeId)));
    }

    @GetMapping("/shifts/date/{date}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeDto.ShiftResponse>>> getShiftsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Shifts fetched successfully", employeeService.getShiftsByDate(date)));
    }

    // -------------------------------- Attendance --------------------------------

    @PostMapping("/attendance")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDto.AttendanceResponse>> markAttendance(
            @Valid @RequestBody EmployeeDto.AttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Attendance marked successfully", 
                employeeService.markAttendance(request, currentUser.getUsername(), ipAddress)), HttpStatus.CREATED);
    }

    @GetMapping("/{employeeId}/attendance")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER', 'ROLE_WAITER', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<List<EmployeeDto.AttendanceResponse>>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Attendance fetched successfully", employeeService.getAttendanceByEmployee(employeeId)));
    }

    @GetMapping("/attendance/date/{date}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeDto.AttendanceResponse>>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Attendance fetched successfully", employeeService.getAttendanceByDate(date)));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
