package com.restaurant.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class CategoryDto {

    @Data
    public static class CategoryRequest {
        @NotBlank(message = "Category name is required")
        private String name;
        private String description;
        private String imageUrl;
        private Boolean active = true;
        private Integer displayOrder = 0;
    }

    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private boolean active;
        private int displayOrder;
    }
}
