package com.restaurant.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class RestaurantSettingDto {

    @Data
    public static class SettingRequest {
        @NotBlank(message = "Setting key is required")
        private String settingKey;

        @NotBlank(message = "Setting value is required")
        private String settingValue;

        private String description;
    }

    @Data
    public static class SettingResponse {
        private Long id;
        private String settingKey;
        private String settingValue;
        private String description;
    }
}
