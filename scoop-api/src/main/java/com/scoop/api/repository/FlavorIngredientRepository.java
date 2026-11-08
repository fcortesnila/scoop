package com.scoop.api.repository;

import com.scoop.api.entity.FlavorIngredient;
import com.scoop.api.entity.FlavorIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlavorIngredientRepository extends JpaRepository<FlavorIngredient, FlavorIngredientId> {
    List<FlavorIngredient> findByFlavorIdIn(List<Long> flavorIds);
    List<FlavorIngredient> findByFlavorId(Long flavorId);
}
