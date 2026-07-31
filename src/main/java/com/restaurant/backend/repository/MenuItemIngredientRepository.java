package com.restaurant.backend.repository;

import com.restaurant.backend.entity.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {
    List<MenuItemIngredient> findByMenuItemId(Long menuItemId);
    void deleteByMenuItemId(Long menuItemId);
    boolean existsByMenuItemIdAndIngredientId(Long menuItemId, Long ingredientId);
}
