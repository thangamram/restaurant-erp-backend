package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Bill;
import com.restaurant.backend.enums.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByInvoiceNumber(String invoiceNumber);

    Optional<Bill> findByOrderId(Long orderId);

    Page<Bill> findByCustomerId(Long customerId, Pageable pageable);

    Page<Bill> findByStatus(BillStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.gstAmount), 0) FROM Bill b WHERE b.status = 'PAID' AND b.generatedAt >= :startDate AND b.generatedAt <= :endDate")
    Double sumGstCollectedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
