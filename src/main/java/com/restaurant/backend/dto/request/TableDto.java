package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class TableDto {

    @Data
    public static class TableRequest {
        @NotBlank(message = "Table number is required")
        private String tableNumber;

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        private Integer capacity;

        private TableStatus status = TableStatus.AVAILABLE;
        private String locationSection;
        private String assignedWaiter;
    }

    @Data
    public static class TableResponse {
        private Long id;
        private String tableNumber;
        private int capacity;
        private TableStatus status;
        private String locationSection;
        private String assignedWaiter;
    }
}
