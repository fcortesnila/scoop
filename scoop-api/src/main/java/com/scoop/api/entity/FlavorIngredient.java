package com.scoop.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "flavor_ingredients")
@Getter
@Setter
@NoArgsConstructor
public class FlavorIngredient {
    @EmbeddedId
    private FlavorIngredientId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("flavorId")
    @JoinColumn(name = "flavor_id")
    private Flavor flavor;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;
    @Column(name = "qty_per_serving", nullable = false, precision = 12, scale = 3)
    private BigDecimal qtyPerServing;
}
