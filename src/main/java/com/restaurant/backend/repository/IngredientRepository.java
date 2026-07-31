package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);
    Boolean existsByName(String name);
    Boolean existsByNameIgnoreCase(String name);

    @Query("SELECT i FROM Ingredient i WHERE i.currentStock <= i.minimumStock")
    List<Ingredient> findLowStockIngredients();
}
