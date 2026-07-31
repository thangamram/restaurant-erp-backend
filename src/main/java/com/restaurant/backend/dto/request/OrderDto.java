package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.OrderItemStatus;
import com.restaurant.backend.enums.OrderStatus;
import com.restaurant.backend.enums.OrderType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class CreateOrderRequest {
        private Long tableId;
        private OrderType orderType = OrderType.DINE_IN;
        private String remarks;

        @NotEmpty(message = "Order must contain at least one item")
        private List<OrderItemRequest> items;
    }

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private String specialInstructions;
    }

    @Data
    public static class UpdateOrderStatusRequest {
        @NotNull(message = "Order status is required")
        private OrderStatus status;
        private String remarks;
        private Long waiterId;
    }

    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long menuItemId;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String specialInstructions;
        private OrderItemStatus status;
    }

    @Data
    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private Long customerId;
        private String customerName;
        private String customerMobile;
        private Long tableId;
        private String tableNumber;
        private Long waiterId;
        private String waiterName;
        private OrderStatus status;
        private OrderType orderType;
        private BigDecimal totalAmount;
        private String remarks;
        private LocalDateTime placedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime closedAt;
        private List<OrderItemResponse> items;
    }
}
