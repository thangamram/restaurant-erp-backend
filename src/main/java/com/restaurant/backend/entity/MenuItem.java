package com.restaurant.backend.entity;

import com.restaurant.backend.enums.DietaryType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "gst_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPercentage = new BigDecimal("5.00");

    @Column(name = "prep_time_minutes")
    @Builder.Default
    private int prepTimeMinutes = 15;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_type", nullable = false, length = 20)
    @Builder.Default
    private DietaryType dietaryType = DietaryType.VEG;

    @Builder.Default
    private boolean available = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    private boolean deleted = false;
}
