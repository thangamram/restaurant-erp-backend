package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.EmployeeDto;
import com.restaurant.backend.entity.*;
import com.restaurant.backend.enums.EmployeeStatus;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeAttendanceRepository employeeAttendanceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // -------------------------------- Employees --------------------------------

    @Transactional
    public EmployeeDto.EmployeeResponse createEmployee(EmployeeDto.EmployeeRequest request, String performedBy, String ipAddress) {
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BadRequestException("Employee code '" + request.getEmployeeCode() + "' already exists.");
        }
        if (employeeRepository.existsByUserId(request.getUserId())) {
            throw new BadRequestException("A user can only have one employee profile.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Employee employee = Employee.builder()
                .user(user)
                .employeeCode(request.getEmployeeCode())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .baseSalary(request.getBaseSalary())
                .joiningDate(request.getJoiningDate())
                .status(request.getStatus() != null ? request.getStatus() : EmployeeStatus.ACTIVE)
                .build();

        Employee saved = employeeRepository.save(employee);
        auditService.logAction(performedBy, "ADMIN", "EMPLOYEE_CREATED", "EMPLOYEE",
                "Employee '" + saved.getEmployeeCode() + "' created for user: " + user.getUsername(), ipAddress);

        return mapToEmployeeResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto.EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::mapToEmployeeResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeDto.EmployeeResponse getEmployeeById(Long id) {
        return mapToEmployeeResponse(employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id)));
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto.EmployeeResponse> getActiveEmployees() {
        return employeeRepository.findByStatus(EmployeeStatus.ACTIVE).stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDto.EmployeeResponse updateEmployee(Long id, EmployeeDto.EmployeeRequest request, String performedBy, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setBaseSalary(request.getBaseSalary());
        employee.setJoiningDate(request.getJoiningDate());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        Employee saved = employeeRepository.save(employee);
        auditService.logAction(performedBy, "ADMIN", "EMPLOYEE_UPDATED", "EMPLOYEE",
                "Employee '" + saved.getEmployeeCode() + "' updated.", ipAddress);
        return mapToEmployeeResponse(saved);
    }

    @Transactional
    public void deleteEmployee(Long id, String performedBy, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
        auditService.logAction(performedBy, "ADMIN", "EMPLOYEE_TERMINATED", "EMPLOYEE",
                "Employee '" + employee.getEmployeeCode() + "' marked as TERMINATED.", ipAddress);
    }

    // -------------------------------- Shifts --------------------------------

    @Transactional
    public EmployeeDto.ShiftResponse assignShift(EmployeeDto.ShiftRequest request, String performedBy, String ipAddress) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        if (employeeShiftRepository.existsByEmployeeIdAndShiftDate(employee.getId(), request.getShiftDate())) {
            throw new BadRequestException("Shift already assigned for employee " + employee.getEmployeeCode() + " on " + request.getShiftDate());
        }

        EmployeeShift shift = EmployeeShift.builder()
                .employee(employee)
                .shiftDate(request.getShiftDate())
                .shiftType(request.getShiftType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .notes(request.getNotes())
                .build();

        EmployeeShift saved = employeeShiftRepository.save(shift);
        auditService.logAction(performedBy, "ADMIN", "SHIFT_ASSIGNED", "EMPLOYEE",
                "Shift assigned to employee " + employee.getEmployeeCode() + " on " + request.getShiftDate(), ipAddress);

        return mapToShiftResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto.ShiftResponse> getShiftsByEmployee(Long employeeId) {
        return employeeShiftRepository.findByEmployeeIdOrderByShiftDateDesc(employeeId).stream()
                .map(this::mapToShiftResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto.ShiftResponse> getShiftsByDate(LocalDate date) {
        return employeeShiftRepository.findByShiftDate(date).stream()
                .map(this::mapToShiftResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------- Attendance --------------------------------

    @Transactional
    public EmployeeDto.AttendanceResponse markAttendance(EmployeeDto.AttendanceRequest request, String performedBy, String ipAddress) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        EmployeeAttendance attendance = employeeAttendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), request.getAttendanceDate())
                .orElse(EmployeeAttendance.builder()
                        .employee(employee)
                        .attendanceDate(request.getAttendanceDate())
                        .build());

        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setStatus(request.getStatus());

        EmployeeAttendance saved = employeeAttendanceRepository.save(attendance);
        auditService.logAction(performedBy, "ADMIN", "ATTENDANCE_MARKED", "EMPLOYEE",
                "Attendance marked for employee " + employee.getEmployeeCode() + " on " + request.getAttendanceDate()
                        + ": " + request.getStatus(), ipAddress);

        return mapToAttendanceResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto.AttendanceResponse> getAttendanceByEmployee(Long employeeId) {
        return employeeAttendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(employeeId).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto.AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return employeeAttendanceRepository.findByAttendanceDate(date).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------- Mappers --------------------------------

    private EmployeeDto.EmployeeResponse mapToEmployeeResponse(Employee emp) {
        EmployeeDto.EmployeeResponse res = new EmployeeDto.EmployeeResponse();
        res.setId(emp.getId());
        res.setUserId(emp.getUser().getId());
        res.setUsername(emp.getUser().getUsername());
        res.setFullName(emp.getUser().getFullName());
        res.setEmail(emp.getUser().getEmail());
        res.setMobileNumber(emp.getUser().getMobileNumber());
        res.setEmployeeCode(emp.getEmployeeCode());
        res.setDepartment(emp.getDepartment());
        res.setDesignation(emp.getDesignation());
        res.setBaseSalary(emp.getBaseSalary());
        res.setJoiningDate(emp.getJoiningDate());
        res.setStatus(emp.getStatus());
        return res;
    }

    private EmployeeDto.ShiftResponse mapToShiftResponse(EmployeeShift shift) {
        EmployeeDto.ShiftResponse res = new EmployeeDto.ShiftResponse();
        res.setId(shift.getId());
        res.setEmployeeId(shift.getEmployee().getId());
        res.setEmployeeName(shift.getEmployee().getUser().getFullName());
        res.setShiftDate(shift.getShiftDate());
        res.setShiftType(shift.getShiftType());
        res.setStartTime(shift.getStartTime());
        res.setEndTime(shift.getEndTime());
        res.setNotes(shift.getNotes());
        return res;
    }

    private EmployeeDto.AttendanceResponse mapToAttendanceResponse(EmployeeAttendance att) {
        EmployeeDto.AttendanceResponse res = new EmployeeDto.AttendanceResponse();
        res.setId(att.getId());
        res.setEmployeeId(att.getEmployee().getId());
        res.setEmployeeName(att.getEmployee().getUser().getFullName());
        res.setAttendanceDate(att.getAttendanceDate());
        res.setCheckInTime(att.getCheckInTime());
        res.setCheckOutTime(att.getCheckOutTime());
        res.setStatus(att.getStatus());
        return res;
    }
}
