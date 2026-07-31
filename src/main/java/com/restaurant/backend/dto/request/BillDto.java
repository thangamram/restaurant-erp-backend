package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.BillStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillDto {

    @Data
    public static class GenerateBillRequest {
        @NotNull(message = "Order ID is required")
        private Long orderId;
        private String couponCode;
        private BigDecimal customDiscount;
        private BigDecimal serviceChargePercentage;
    }

    @Data
    public static class BillResponse {
        private Long id;
        private String invoiceNumber;
        private Long orderId;
        private String orderNumber;
        private Long customerId;
        private String customerName;
        private String customerMobile;
        private Long cashierId;
        private String cashierName;
        private BigDecimal itemTotal;
        private BigDecimal gstAmount;
        private BigDecimal serviceCharge;
        private BigDecimal discountAmount;
        private String couponCode;
        private BigDecimal roundOff;
        private BigDecimal grandTotal;
        private BillStatus status;
        private String pdfFilePath;
        private LocalDateTime generatedAt;
    }
}
