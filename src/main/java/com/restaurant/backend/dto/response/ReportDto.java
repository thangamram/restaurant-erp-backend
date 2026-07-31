package com.restaurant.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDto {

    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalGstCollected;
    private List<PopularItemDto> popularItems;
    private Map<String, BigDecimal> revenueByPaymentMethod;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PopularItemDto {
        private String itemName;
        private long totalQuantitySold;
    }
}
