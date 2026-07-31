package com.restaurant.backend.repository;

import com.restaurant.backend.entity.MenuItem;
import com.restaurant.backend.enums.DietaryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategoryIdAndDeletedFalseAndAvailableTrue(Long categoryId);

    Page<MenuItem> findByDeletedFalse(Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.deleted = false " +
           "AND (:categoryId IS NULL OR m.category.id = :categoryId) " +
           "AND (:dietaryType IS NULL OR m.dietaryType = :dietaryType) " +
           "AND (:available IS NULL OR m.available = :available) " +
           "AND (:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MenuItem> searchMenuItems(@Param("categoryId") Long categoryId,
                                   @Param("dietaryType") DietaryType dietaryType,
                                   @Param("available") Boolean available,
                                   @Param("search") String search,
                                   Pageable pageable);
}
