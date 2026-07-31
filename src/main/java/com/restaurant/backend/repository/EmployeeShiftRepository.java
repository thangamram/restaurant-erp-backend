package com.restaurant.backend.repository;

import com.restaurant.backend.entity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {
    List<EmployeeShift> findByEmployeeIdAndShiftDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    List<EmployeeShift> findByShiftDate(LocalDate shiftDate);
    List<EmployeeShift> findByEmployeeIdOrderByShiftDateDesc(Long employeeId);
    boolean existsByEmployeeIdAndShiftDate(Long employeeId, LocalDate shiftDate);
}
