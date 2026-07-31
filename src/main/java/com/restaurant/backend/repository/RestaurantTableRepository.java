package com.restaurant.backend.repository;

import com.restaurant.backend.entity.RestaurantTable;
import com.restaurant.backend.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByTableNumber(String tableNumber);
    List<RestaurantTable> findByStatus(TableStatus status);
    Boolean existsByTableNumber(String tableNumber);
}
