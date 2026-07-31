package com.restaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure; // e.g. KG, LITER, GRAM, PIECE

    @Column(name = "current_stock", nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "minimum_stock", nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal minimumStock = new BigDecimal("10.000");

    @Column(name = "unit_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;
}
