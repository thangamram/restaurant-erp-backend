package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.InventoryAction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryDto {

    @Data
    public static class IngredientRequest {
        @NotBlank(message = "Ingredient name is required")
        private String name;

        @NotBlank(message = "Unit of measure is required")
        private String unitOfMeasure;

        @NotNull(message = "Current stock is required")
        private BigDecimal currentStock;

        private BigDecimal minimumStock = new BigDecimal("10.000");
        private BigDecimal unitCost = BigDecimal.ZERO;
    }

    @Data
    public static class IngredientResponse {
        private Long id;
        private String name;
        private String unitOfMeasure;
        private BigDecimal currentStock;
        private BigDecimal minimumStock;
        private BigDecimal unitCost;
        private boolean lowStock;
    }

    @Data
    public static class RecipeRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @NotNull(message = "Ingredient ID is required")
        private Long ingredientId;

        @NotNull(message = "Quantity required is required")
        @DecimalMin(value = "0.001", message = "Quantity required must be > 0")
        private BigDecimal quantityRequired;
    }

    @Data
    public static class RecipeResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private Long ingredientId;
        private String ingredientName;
        private String unitOfMeasure;
        private BigDecimal quantityRequired;
    }

    @Data
    public static class StockAdjustmentRequest {
        @NotNull(message = "Ingredient ID is required")
        private Long ingredientId;

        @NotNull(message = "Quantity change is required")
        private BigDecimal quantityChange;

        @NotNull(message = "Action type is required")
        private InventoryAction actionType;

        private String notes;
    }

    @Data
    public static class InventoryLogResponse {
        private Long id;
        private Long ingredientId;
        private String ingredientName;
        private BigDecimal quantityChange;
        private InventoryAction actionType;
        private String referenceId;
        private String performedBy;
        private LocalDateTime timestamp;
        private String notes;
    }
}
