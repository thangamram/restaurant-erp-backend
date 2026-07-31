package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.InventoryDto;
import com.restaurant.backend.entity.*;
import com.restaurant.backend.enums.InventoryAction;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final MenuItemIngredientRepository menuItemIngredientRepository;
    private final MenuItemRepository menuItemRepository;
    private final SupplierRepository supplierRepository;
    private final AuditService auditService;

    // ----------------------------- Ingredient CRUD -----------------------------

    @Transactional
    public InventoryDto.IngredientResponse createIngredient(InventoryDto.IngredientRequest request, String performedBy, String ipAddress) {
        if (ingredientRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Ingredient with name '" + request.getName() + "' already exists.");
        }
        Ingredient ingredient = Ingredient.builder()
                .name(request.getName())
                .unitOfMeasure(request.getUnitOfMeasure())
                .currentStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : BigDecimal.TEN)
                .unitCost(request.getUnitCost() != null ? request.getUnitCost() : BigDecimal.ZERO)
                .build();
        Ingredient saved = ingredientRepository.save(ingredient);

        // Log initial stock
        logInventoryAction(saved, request.getCurrentStock(), InventoryAction.INITIAL_STOCK,
                null, performedBy, "Initial stock entry");

        auditService.logAction(performedBy, "ADMIN", "INGREDIENT_CREATED", "INVENTORY",
                "Ingredient '" + saved.getName() + "' added with stock: " + saved.getCurrentStock(), ipAddress);

        return mapToIngredientResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryDto.IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::mapToIngredientResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryDto.IngredientResponse getIngredientById(Long id) {
        return mapToIngredientResponse(ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", id)));
    }

    @Transactional(readOnly = true)
    public List<InventoryDto.IngredientResponse> getLowStockIngredients() {
        return ingredientRepository.findLowStockIngredients().stream()
                .map(this::mapToIngredientResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryDto.IngredientResponse updateIngredient(Long id, InventoryDto.IngredientRequest request, String performedBy, String ipAddress) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", id));
        ingredient.setName(request.getName());
        ingredient.setUnitOfMeasure(request.getUnitOfMeasure());
        ingredient.setMinimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : ingredient.getMinimumStock());
        ingredient.setUnitCost(request.getUnitCost() != null ? request.getUnitCost() : ingredient.getUnitCost());
        Ingredient saved = ingredientRepository.save(ingredient);
        auditService.logAction(performedBy, "ADMIN", "INGREDIENT_UPDATED", "INVENTORY",
                "Ingredient '" + saved.getName() + "' updated.", ipAddress);
        return mapToIngredientResponse(saved);
    }

    @Transactional
    public void deleteIngredient(Long id, String performedBy, String ipAddress) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", id));
        ingredientRepository.delete(ingredient);
        auditService.logAction(performedBy, "ADMIN", "INGREDIENT_DELETED", "INVENTORY",
                "Ingredient '" + ingredient.getName() + "' deleted.", ipAddress);
    }

    // ----------------------------- Stock Adjustment -----------------------------

    @Transactional
    public InventoryDto.IngredientResponse adjustStock(InventoryDto.StockAdjustmentRequest request, String performedBy, String ipAddress) {
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", request.getIngredientId()));

        BigDecimal newStock = ingredient.getCurrentStock().add(request.getQuantityChange());
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Insufficient stock for ingredient: " + ingredient.getName());
        }

        ingredient.setCurrentStock(newStock);
        ingredientRepository.save(ingredient);

        logInventoryAction(ingredient, request.getQuantityChange(), request.getActionType(),
                null, performedBy, request.getNotes());

        auditService.logAction(performedBy, "ADMIN", "STOCK_ADJUSTED", "INVENTORY",
                "Stock for '" + ingredient.getName() + "' adjusted by " + request.getQuantityChange()
                        + ". New stock: " + newStock, ipAddress);

        if (newStock.compareTo(ingredient.getMinimumStock()) <= 0) {
            log.warn("LOW STOCK ALERT: Ingredient '{}' is below minimum stock! Current: {}, Minimum: {}",
                    ingredient.getName(), newStock, ingredient.getMinimumStock());
        }

        return mapToIngredientResponse(ingredient);
    }

    // ----------------------------- Recipe Management -----------------------------

    @Transactional
    public InventoryDto.RecipeResponse addRecipe(InventoryDto.RecipeRequest request, String performedBy, String ipAddress) {
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()));
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", request.getIngredientId()));

        if (menuItemIngredientRepository.existsByMenuItemIdAndIngredientId(menuItem.getId(), ingredient.getId())) {
            throw new BadRequestException("Recipe entry already exists for this menu item and ingredient.");
        }

        MenuItemIngredient recipe = MenuItemIngredient.builder()
                .menuItem(menuItem)
                .ingredient(ingredient)
                .quantityRequired(request.getQuantityRequired())
                .build();
        MenuItemIngredient saved = menuItemIngredientRepository.save(recipe);

        auditService.logAction(performedBy, "ADMIN", "RECIPE_ADDED", "INVENTORY",
                "Recipe: " + ingredient.getName() + " x" + request.getQuantityRequired() + " for " + menuItem.getName(), ipAddress);

        return mapToRecipeResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryDto.RecipeResponse> getRecipesByMenuItemId(Long menuItemId) {
        return menuItemIngredientRepository.findByMenuItemId(menuItemId).stream()
                .map(this::mapToRecipeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRecipe(Long recipeId, String performedBy, String ipAddress) {
        MenuItemIngredient recipe = menuItemIngredientRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", recipeId));
        menuItemIngredientRepository.delete(recipe);
        auditService.logAction(performedBy, "ADMIN", "RECIPE_DELETED", "INVENTORY",
                "Recipe #" + recipeId + " deleted.", ipAddress);
    }

    // ----------------------------- Inventory Logs -----------------------------

    @Transactional(readOnly = true)
    public List<InventoryDto.InventoryLogResponse> getInventoryLogs(Long ingredientId) {
        List<InventoryLog> logs = ingredientId != null
                ? inventoryLogRepository.findByIngredientIdOrderByTimestampDesc(ingredientId)
                : inventoryLogRepository.findAllByOrderByTimestampDesc();
        return logs.stream().map(this::mapToLogResponse).collect(Collectors.toList());
    }

    // ----------------------------- Auto-Deduction on Order Close -----------------------------

    @Transactional
    public void deductInventoryForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            List<MenuItemIngredient> recipes = menuItemIngredientRepository.findByMenuItemId(item.getMenuItem().getId());
            for (MenuItemIngredient recipe : recipes) {
                BigDecimal totalRequired = recipe.getQuantityRequired().multiply(BigDecimal.valueOf(item.getQuantity()));
                Ingredient ingredient = recipe.getIngredient();

                if (ingredient.getCurrentStock().compareTo(totalRequired) < 0) {
                    log.warn("Insufficient stock for ingredient '{}' during order close. Required: {}, Available: {}",
                            ingredient.getName(), totalRequired, ingredient.getCurrentStock());
                    // Don't throw - just log. Order is already delivered.
                    ingredient.setCurrentStock(BigDecimal.ZERO);
                } else {
                    ingredient.setCurrentStock(ingredient.getCurrentStock().subtract(totalRequired));
                }

                ingredientRepository.save(ingredient);
                logInventoryAction(ingredient, totalRequired.negate(), InventoryAction.AUTO_DEDUCTION,
                        order.getOrderNumber(), "SYSTEM", "Auto-deducted for Order #" + order.getOrderNumber());

                if (ingredient.getCurrentStock().compareTo(ingredient.getMinimumStock()) <= 0) {
                    log.warn("LOW STOCK ALERT: '{}' - Current: {}", ingredient.getName(), ingredient.getCurrentStock());
                }
            }
        }
    }

    // ----------------------------- Private Helpers -----------------------------

    private void logInventoryAction(Ingredient ingredient, BigDecimal change, InventoryAction action,
                                    String referenceId, String performedBy, String notes) {
        InventoryLog logEntry = InventoryLog.builder()
                .ingredient(ingredient)
                .quantityChange(change)
                .actionType(action)
                .referenceId(referenceId)
                .performedBy(performedBy != null ? performedBy : "SYSTEM")
                .notes(notes)
                .timestamp(LocalDateTime.now())
                .build();
        inventoryLogRepository.save(logEntry);
    }

    private InventoryDto.IngredientResponse mapToIngredientResponse(Ingredient ingredient) {
        InventoryDto.IngredientResponse res = new InventoryDto.IngredientResponse();
        res.setId(ingredient.getId());
        res.setName(ingredient.getName());
        res.setUnitOfMeasure(ingredient.getUnitOfMeasure());
        res.setCurrentStock(ingredient.getCurrentStock());
        res.setMinimumStock(ingredient.getMinimumStock());
        res.setUnitCost(ingredient.getUnitCost());
        res.setLowStock(ingredient.getCurrentStock().compareTo(ingredient.getMinimumStock()) <= 0);
        return res;
    }

    private InventoryDto.RecipeResponse mapToRecipeResponse(MenuItemIngredient recipe) {
        InventoryDto.RecipeResponse res = new InventoryDto.RecipeResponse();
        res.setId(recipe.getId());
        res.setMenuItemId(recipe.getMenuItem().getId());
        res.setMenuItemName(recipe.getMenuItem().getName());
        res.setIngredientId(recipe.getIngredient().getId());
        res.setIngredientName(recipe.getIngredient().getName());
        res.setUnitOfMeasure(recipe.getIngredient().getUnitOfMeasure());
        res.setQuantityRequired(recipe.getQuantityRequired());
        return res;
    }

    private InventoryDto.InventoryLogResponse mapToLogResponse(InventoryLog log) {
        InventoryDto.InventoryLogResponse res = new InventoryDto.InventoryLogResponse();
        res.setId(log.getId());
        res.setIngredientId(log.getIngredient().getId());
        res.setIngredientName(log.getIngredient().getName());
        res.setQuantityChange(log.getQuantityChange());
        res.setActionType(log.getActionType());
        res.setReferenceId(log.getReferenceId());
        res.setPerformedBy(log.getPerformedBy());
        res.setTimestamp(log.getTimestamp());
        res.setNotes(log.getNotes());
        return res;
    }
}
