package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.PayrollStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollDto {

    @Data
    public static class PayrollRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotBlank(message = "Month is required")
        private String month;

        @NotNull(message = "Year is required")
        private Integer year;

        @NotNull(message = "Basic salary is required")
        private BigDecimal basicSalary;

        private BigDecimal overtimePay = BigDecimal.ZERO;
        private BigDecimal bonus = BigDecimal.ZERO;
        private BigDecimal deductions = BigDecimal.ZERO;

        private PayrollStatus status = PayrollStatus.PENDING;
    }

    @Data
    public static class PayrollResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private String role;
        private String month;
        private int year;
        private BigDecimal basicSalary;
        private BigDecimal overtimePay;
        private BigDecimal bonus;
        private BigDecimal deductions;
        private BigDecimal netPay;
        private PayrollStatus status;
        private LocalDateTime generatedAt;
    }
}
