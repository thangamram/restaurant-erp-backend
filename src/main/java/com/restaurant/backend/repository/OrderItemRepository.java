package com.restaurant.backend.repository;

import com.restaurant.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.itemName as name, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
           "JOIN oi.order o WHERE o.status IN ('PAID', 'CLOSED') " +
           "AND o.placedAt >= :startDate AND o.placedAt <= :endDate " +
           "GROUP BY oi.itemName ORDER BY totalQty DESC")
    List<Object[]> findPopularItems(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
