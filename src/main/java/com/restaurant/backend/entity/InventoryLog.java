package com.restaurant.backend.entity;

import com.restaurant.backend.enums.InventoryAction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantity_change", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityChange;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private InventoryAction actionType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(length = 255)
    private String notes;
}
