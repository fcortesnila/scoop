package com.scoop.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 120)
    private String name;
    @Column(nullable = false, length = 32)
    private String unit;
    @Column(name = "stock_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockQty;
    @Column(name = "low_stock_threshold", nullable = false, precision = 12, scale = 3)
    private BigDecimal lowStockThreshold;
}
