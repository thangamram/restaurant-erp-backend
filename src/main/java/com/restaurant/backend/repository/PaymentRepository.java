package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Payment;
import com.restaurant.backend.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findAllByBillId(Long billId);

    Optional<Payment> findByBillId(Long billId);

    Optional<Payment> findByOrderId(Long orderId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' " +
           "AND p.paymentTime >= :startDate AND p.paymentTime <= :endDate GROUP BY p.paymentMethod")
    List<Object[]> sumRevenueByPaymentMethod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
