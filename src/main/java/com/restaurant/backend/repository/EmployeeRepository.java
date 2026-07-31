package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Employee;
import com.restaurant.backend.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Boolean existsByEmployeeCode(String employeeCode);
    Boolean existsByUserId(Long userId);
    List<Employee> findByStatus(EmployeeStatus status);
}
