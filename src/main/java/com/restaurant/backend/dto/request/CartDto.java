package com.restaurant.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {

    @Data
    public static class CartItemRequest {
        @NotNull(message = "Menu Item ID is required")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        private String specialInstructions;
    }

    @Data
    public static class CartItemResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private String menuItemImage;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String specialInstructions;
    }

    @Data
    public static class CartResponse {
        private Long id;
        private Long customerId;
        private Long tableId;
        private String tableNumber;
        private List<CartItemResponse> items;
        private BigDecimal cartTotal;
    }
}
