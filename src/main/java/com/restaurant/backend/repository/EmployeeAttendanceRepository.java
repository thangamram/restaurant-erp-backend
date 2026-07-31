package com.restaurant.backend.repository;

import com.restaurant.backend.entity.EmployeeAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Long> {
    Optional<EmployeeAttendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
    List<EmployeeAttendance> findByAttendanceDate(LocalDate attendanceDate);
    List<EmployeeAttendance> findByEmployeeIdAndAttendanceDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    List<EmployeeAttendance> findByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);
    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
}
