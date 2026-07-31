package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.AttendanceStatus;
import com.restaurant.backend.enums.EmployeeStatus;
import com.restaurant.backend.enums.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EmployeeDto {

    @Data
    public static class EmployeeRequest {
        @NotNull(message = "User ID is required")
        private Long userId;

        @NotBlank(message = "Employee code is required")
        private String employeeCode;

        @NotBlank(message = "Department is required")
        private String department;

        @NotBlank(message = "Designation is required")
        private String designation;

        private BigDecimal baseSalary = BigDecimal.ZERO;

        @NotNull(message = "Joining date is required")
        private LocalDate joiningDate;

        private EmployeeStatus status = EmployeeStatus.ACTIVE;
    }

    @Data
    public static class EmployeeResponse {
        private Long id;
        private Long userId;
        private String username;
        private String fullName;
        private String email;
        private String mobileNumber;
        private String employeeCode;
        private String department;
        private String designation;
        private BigDecimal baseSalary;
        private LocalDate joiningDate;
        private EmployeeStatus status;
    }

    @Data
    public static class ShiftRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Shift date is required")
        private LocalDate shiftDate;

        @NotNull(message = "Shift type is required")
        private ShiftType shiftType;

        @NotNull(message = "Start time is required")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        private LocalTime endTime;

        private String notes;
    }

    @Data
    public static class ShiftResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private LocalDate shiftDate;
        private ShiftType shiftType;
        private LocalTime startTime;
        private LocalTime endTime;
        private String notes;
    }

    @Data
    public static class AttendanceRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Attendance date is required")
        private LocalDate attendanceDate;

        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private AttendanceStatus status = AttendanceStatus.PRESENT;
    }

    @Data
    public static class AttendanceResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private LocalDate attendanceDate;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private AttendanceStatus status;
    }
}
