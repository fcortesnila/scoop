package com.scoop.api.service;

import com.scoop.api.entity.*;
import com.scoop.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private OrderRepository orders;
    private FlavorIngredientRepository recipes;
    private IngredientRepository ingredients;
    private StopRepository stops;
    private OrderService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        UserRepository users = mock(UserRepository.class);
        FlavorRepository flavors = mock(FlavorRepository.class);
        recipes = mock(FlavorIngredientRepository.class);
        ingredients = mock(IngredientRepository.class);
        stops = mock(StopRepository.class);
        service = new OrderService(orders, users, flavors, recipes, ingredients, stops);
    }

    @Test
    void servingDeductsRecipeStockAndKeepsFlavorAvailableWhenStockRemains() {
        Ingredient ingredient = ingredient(1L, "Vanilla base", "1.000");
        Flavor flavor = flavor(2L, "Vanilla Bean");
        FlavorIngredient recipe = recipe(flavor, ingredient, "0.100");
        ScoopOrder order = order(flavor, OrderStatus.READY, 2);
        when(orders.findById(7L)).thenReturn(Optional.of(order));
        when(recipes.findByFlavorIdIn(anyList())).thenReturn(List.of(recipe));
        when(ingredients.findByIdForUpdate(1L)).thenReturn(Optional.of(ingredient));
        when(stops.findFirstByEndedAtIsNullOrderByStartedAtDesc()).thenReturn(Optional.empty());

        service.updateStatus(7L, OrderStatus.SERVED);

        assertEquals(new BigDecimal("0.800"), ingredient.getStockQty());
        assertTrue(flavor.isAvailable());
        assertEquals(OrderStatus.SERVED, order.getStatus());
        verify(orders, never()).save(any());
    }

    @Test
    void servingRejectsInsufficientStockWithoutChangingOrderOrInventory() {
        Ingredient ingredient = ingredient(1L, "Vanilla base", "0.100");
        Flavor flavor = flavor(2L, "Vanilla Bean");
        FlavorIngredient recipe = recipe(flavor, ingredient, "0.100");
        ScoopOrder order = order(flavor, OrderStatus.READY, 2);
        when(orders.findById(7L)).thenReturn(Optional.of(order));
        when(recipes.findByFlavorIdIn(anyList())).thenReturn(List.of(recipe));
        when(ingredients.findByIdForUpdate(1L)).thenReturn(Optional.of(ingredient));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.updateStatus(7L, OrderStatus.SERVED));

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(new BigDecimal("0.100"), ingredient.getStockQty());
        assertEquals(OrderStatus.READY, order.getStatus());
    }

    private static Ingredient ingredient(Long id, String name, String stock) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        ingredient.setName(name);
        ingredient.setStockQty(new BigDecimal(stock));
        return ingredient;
    }

    private static Flavor flavor(Long id, String name) {
        Flavor flavor = new Flavor();
        flavor.setId(id);
        flavor.setName(name);
        flavor.setAvailable(true);
        return flavor;
    }

    private static FlavorIngredient recipe(Flavor flavor, Ingredient ingredient, String quantity) {
        FlavorIngredient recipe = new FlavorIngredient();
        recipe.setFlavor(flavor);
        recipe.setIngredient(ingredient);
        recipe.setQtyPerServing(new BigDecimal(quantity));
        return recipe;
    }

    private static ScoopOrder order(Flavor flavor, OrderStatus status, int quantity) {
        ScoopOrder order = new ScoopOrder();
        order.setId(7L);
        order.setStatus(status);
        order.setTotal(new BigDecimal("8.00"));
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setFlavor(flavor);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal("4.00"));
        order.setItems(List.of(item));
        return order;
    }
}
