package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.PayrollDto;
import com.restaurant.backend.entity.Employee;
import com.restaurant.backend.entity.Payroll;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.EmployeeRepository;
import com.restaurant.backend.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Transactional
    public PayrollDto.PayrollResponse generatePayroll(PayrollDto.PayrollRequest request, String performedBy, String ipAddress) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        BigDecimal netPay = request.getBasicSalary()
                .add(request.getOvertimePay())
                .add(request.getBonus())
                .subtract(request.getDeductions());

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(request.getMonth())
                .year(request.getYear())
                .basicSalary(request.getBasicSalary())
                .overtimePay(request.getOvertimePay())
                .bonus(request.getBonus())
                .deductions(request.getDeductions())
                .netPay(netPay)
                .status(request.getStatus())
                .build();

        Payroll saved = payrollRepository.save(payroll);
        auditService.logAction(performedBy, "ADMIN", "PAYROLL_GENERATED", "PAYROLL",
                "Payroll generated for employee '" + employee.getEmployeeCode() + "' for " + request.getMonth() + " " + request.getYear(), ipAddress);

        return mapToPayrollResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PayrollDto.PayrollResponse> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(this::mapToPayrollResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayrollDto.PayrollResponse> getPayrollsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        return payrollRepository.findByEmployeeIdOrderByGeneratedAtDesc(employeeId).stream()
                .map(this::mapToPayrollResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollDto.PayrollResponse updatePayrollStatus(Long id, com.restaurant.backend.enums.PayrollStatus status, String performedBy, String ipAddress) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", "id", id));
        payroll.setStatus(status);
        Payroll saved = payrollRepository.save(payroll);

        auditService.logAction(performedBy, "ADMIN", "PAYROLL_STATUS_UPDATED", "PAYROLL",
                "Payroll ID " + id + " status updated to " + status, ipAddress);

        return mapToPayrollResponse(saved);
    }

    @Transactional
    public void deletePayroll(Long id, String performedBy, String ipAddress) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", "id", id));
        payrollRepository.delete(payroll);
        auditService.logAction(performedBy, "ADMIN", "PAYROLL_DELETED", "PAYROLL",
                "Payroll ID " + id + " deleted", ipAddress);
    }

    private PayrollDto.PayrollResponse mapToPayrollResponse(Payroll payroll) {
        PayrollDto.PayrollResponse response = new PayrollDto.PayrollResponse();
        response.setId(payroll.getId());
        response.setEmployeeId(payroll.getEmployee().getId());
        response.setEmployeeName(payroll.getEmployee().getUser().getFullName());
        response.setEmployeeCode(payroll.getEmployee().getEmployeeCode());
        response.setRole(payroll.getEmployee().getDesignation());
        response.setMonth(payroll.getMonth());
        response.setYear(payroll.getYear());
        response.setBasicSalary(payroll.getBasicSalary());
        response.setOvertimePay(payroll.getOvertimePay());
        response.setBonus(payroll.getBonus());
        response.setDeductions(payroll.getDeductions());
        response.setNetPay(payroll.getNetPay());
        response.setStatus(payroll.getStatus());
        response.setGeneratedAt(payroll.getGeneratedAt());
        return response;
    }
}
