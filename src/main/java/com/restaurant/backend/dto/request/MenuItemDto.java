package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.DietaryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

public class MenuItemDto {

    @Data
    public static class MenuItemRequest {
        @NotNull(message = "Category ID is required")
        private Long categoryId;

        @NotBlank(message = "Item name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        private BigDecimal gstPercentage = new BigDecimal("5.00");
        private Integer prepTimeMinutes = 15;
        private DietaryType dietaryType = DietaryType.VEG;
        private Boolean available = true;
        private String imageUrl;
    }

    @Data
    public static class MenuItemResponse {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal gstPercentage;
        private int prepTimeMinutes;
        private DietaryType dietaryType;
        private boolean available;
        private String imageUrl;
    }
}
