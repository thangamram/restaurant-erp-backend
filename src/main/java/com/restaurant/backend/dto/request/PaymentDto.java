package com.restaurant.backend.dto.request;

import com.restaurant.backend.enums.PaymentMethod;
import com.restaurant.backend.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Data
    public static class ProcessPaymentRequest {
        @NotNull(message = "Bill ID is required")
        private Long billId;

        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;

        private String transactionId;
        private String referenceNumber;
    }

    @Data
    public static class PaymentResponse {
        private Long id;
        private Long billId;
        private String invoiceNumber;
        private Long orderId;
        private String cashierName;
        private PaymentMethod paymentMethod;
        private String transactionId;
        private String referenceNumber;
        private BigDecimal amount;
        private PaymentStatus status;
        private LocalDateTime paymentTime;
    }
}
