package com.scoop.api.repository;

import com.scoop.api.entity.Ingredient;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Ingredient i where i.id = :id")
    Optional<Ingredient> findByIdForUpdate(@Param("id") Long id);

    @Query(value = """
            SELECT i.id AS ingredientId, i.name AS ingredientName, i.unit AS unit,
                   i.stock_qty AS stock, i.low_stock_threshold AS threshold,
                   COALESCE(SUM(CASE WHEN o.id IS NOT NULL THEN fi.qty_per_serving * oi.quantity ELSE 0 END) / 336, 0) AS averageUsage
            FROM ingredients i
            LEFT JOIN flavor_ingredients fi ON fi.ingredient_id = i.id
            LEFT JOIN order_items oi ON oi.flavor_id = fi.flavor_id
            LEFT JOIN orders o ON o.id = oi.order_id AND o.status = 'SERVED'
                AND o.created_at >= DATE_SUB(NOW(), INTERVAL 14 DAY)
            GROUP BY i.id, i.name, i.unit, i.stock_qty, i.low_stock_threshold
            ORDER BY i.name
            """, nativeQuery = true)
    java.util.List<InventoryUsageRow> findInventoryUsage();

    interface InventoryUsageRow {
        Long getIngredientId();
        String getIngredientName();
        String getUnit();
        java.math.BigDecimal getStock();
        java.math.BigDecimal getThreshold();
        java.math.BigDecimal getAverageUsage();
    }
}
