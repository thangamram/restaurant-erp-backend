package com.restaurant.backend.entity;

import com.restaurant.backend.enums.BillStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;

    @Column(name = "gst_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "service_charge", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "coupon_code", length = 30)
    private String couponCode;

    @Column(name = "round_off", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal roundOff = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.UNPAID;

    @Column(name = "pdf_file_path", length = 500)
    private String pdfFilePath;

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
}
