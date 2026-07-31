package com.restaurant.backend.entity;

import com.restaurant.backend.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false, length = 50)
    private String designation;

    @Column(name = "base_salary", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
}
