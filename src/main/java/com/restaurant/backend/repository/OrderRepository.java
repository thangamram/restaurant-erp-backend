package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Order;
import com.restaurant.backend.enums.OrderStatus;
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
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    List<Order> findByStatusInOrderByPlacedAtAsc(List<OrderStatus> statuses);

    List<Order> findByStatusOrderByPlacedAtAsc(OrderStatus status);

    List<Order> findByStatusOrderByPlacedAtDesc(OrderStatus status);

    List<Order> findByStatusNotIn(List<OrderStatus> statuses);

    List<Order> findByWaiterIdAndStatusIn(Long waiterId, List<OrderStatus> statuses);

    boolean existsByTableIdAndStatusNotIn(Long tableId, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o WHERE (:status IS NULL OR o.status = :status) " +
           "AND (:startDate IS NULL OR o.placedAt >= :startDate) " +
           "AND (:endDate IS NULL OR o.placedAt <= :endDate)")
    Page<Order> filterOrders(@Param("status") OrderStatus status,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate,
                             Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.placedAt >= :startDate AND o.placedAt <= :endDate")
    long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN ('PAID', 'CLOSED') AND o.placedAt >= :startDate AND o.placedAt <= :endDate")
    Double sumTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
