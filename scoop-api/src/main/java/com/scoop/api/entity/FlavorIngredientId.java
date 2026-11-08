package com.scoop.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class FlavorIngredientId implements Serializable {
    @Column(name = "flavor_id")
    private Long flavorId;
    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof FlavorIngredientId that)) return false;
        return Objects.equals(flavorId, that.flavorId) && Objects.equals(ingredientId, that.ingredientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flavorId, ingredientId);
    }
}
