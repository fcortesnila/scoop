package com.scoop.api.service;

import com.scoop.api.dto.ApiDtos.*;
import com.scoop.api.entity.*;
import com.scoop.api.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final UserRepository users;
    private final FlavorRepository flavors;
    private final FlavorIngredientRepository recipes;
    private final IngredientRepository ingredients;
    private final StopRepository stops;

    public OrderService(OrderRepository orders, UserRepository users, FlavorRepository flavors,
                        FlavorIngredientRepository recipes, IngredientRepository ingredients,
                        StopRepository stops) {
        this.orders = orders;
        this.users = users;
        this.flavors = flavors;
        this.recipes = recipes;
        this.ingredients = ingredients;
        this.stops = stops;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request, String googleSub) {
        AppUser user = users.findByGoogleSub(googleSub)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in before placing an order"));
        return createOrder(request, user);
    }

    @Transactional
    public OrderResponse createWalkUp(CreateOrderRequest request) {
        return createOrder(request, null);
    }

    private OrderResponse createOrder(CreateOrderRequest request, AppUser user) {
        Map<Long, Flavor> selected = new LinkedHashMap<>();
        for (ItemRequest item : request.items()) {
            Flavor flavor = flavors.findById(item.flavorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flavor not found"));
            if (!flavor.isAvailable()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, flavor.getName() + " is sold out");
            }
            selected.put(flavor.getId(), flavor);
        }

        ScoopOrder order = new ScoopOrder();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        BigDecimal total = BigDecimal.ZERO;
        for (ItemRequest item : request.items()) {
            Flavor flavor = selected.get(item.flavorId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFlavor(flavor);
            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPrice(flavor.getPrice());
            order.getItems().add(orderItem);
            total = total.add(flavor.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }
        order.setTotal(total);
        assertIngredientsAvailable(order.getItems());
        return response(orders.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> queue() {
        return orders.findByStatusInOrderByCreatedAtAsc(List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY))
                .stream().map(OrderService::response).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> customerOrders(String googleSub) {
        AppUser user = users.findByGoogleSub(googleSub)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return orders.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(OrderService::response).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse customerOrder(Long id, String googleSub) {
        AppUser user = users.findByGoogleSub(googleSub)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        ScoopOrder order = orders.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return response(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        ScoopOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!isNextStatus(order.getStatus(), status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order status cannot move from "
                    + order.getStatus() + " to " + status);
        }
        if (status == OrderStatus.SERVED) {
            deductIngredients(order);
            order.setStop(stops.findFirstByEndedAtIsNullOrderByStartedAtDesc().orElse(null));
        }
        order.setStatus(status);
        return response(order);
    }

    private void assertIngredientsAvailable(List<OrderItem> items) {
        Map<Long, BigDecimal> required = requiredIngredients(items);
        for (Map.Entry<Long, BigDecimal> entry : required.entrySet()) {
            Ingredient ingredient = ingredients.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Flavor recipe is incomplete"));
            if (ingredient.getStockQty().compareTo(entry.getValue()) < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough " + ingredient.getName() + " in stock");
            }
        }
    }

    private void deductIngredients(ScoopOrder order) {
        Map<Long, BigDecimal> required = requiredIngredients(order.getItems());
        Map<Long, Ingredient> lockedIngredients = new LinkedHashMap<>();
        required.keySet().stream().sorted().forEach(id -> {
            Ingredient ingredient = ingredients.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Flavor recipe is incomplete"));
            if (ingredient.getStockQty().compareTo(required.get(id)) < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough " + ingredient.getName() + " in stock");
            }
            lockedIngredients.put(id, ingredient);
        });
        lockedIngredients.forEach((id, ingredient) -> ingredient.setStockQty(ingredient.getStockQty().subtract(required.get(id))));
        for (FlavorIngredient recipe : recipes.findByFlavorIdIn(
                order.getItems().stream().map(item -> item.getFlavor().getId()).distinct().toList())) {
            if (lockedIngredients.get(recipe.getIngredient().getId()).getStockQty().signum() == 0) {
                recipe.getFlavor().setAvailable(false);
            }
        }
    }

    private Map<Long, BigDecimal> requiredIngredients(List<OrderItem> items) {
        Map<Long, BigDecimal> required = new HashMap<>();
        List<Long> flavorIds = items.stream().map(item -> item.getFlavor().getId()).distinct().toList();
        Map<Long, Integer> quantitiesByFlavor = new HashMap<>();
        items.forEach(item -> quantitiesByFlavor.merge(item.getFlavor().getId(), item.getQuantity(), Integer::sum));
        Set<Long> flavorsWithRecipes = new HashSet<>();
        for (FlavorIngredient recipe : recipes.findByFlavorIdIn(flavorIds)) {
            flavorsWithRecipes.add(recipe.getFlavor().getId());
            BigDecimal amount = recipe.getQtyPerServing()
                    .multiply(BigDecimal.valueOf(quantitiesByFlavor.get(recipe.getFlavor().getId())));
            required.merge(recipe.getIngredient().getId(), amount, BigDecimal::add);
        }
        if (required.isEmpty() || !flavorsWithRecipes.containsAll(flavorIds)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flavor recipe is incomplete");
        }
        return required;
    }

    private static boolean isNextStatus(OrderStatus current, OrderStatus requested) {
        return switch (current) {
            case PENDING -> requested == OrderStatus.PREPARING || requested == OrderStatus.CANCELLED;
            case PREPARING -> requested == OrderStatus.READY || requested == OrderStatus.CANCELLED;
            case READY -> requested == OrderStatus.SERVED || requested == OrderStatus.CANCELLED;
            case SERVED, CANCELLED -> false;
        };
    }

    private static OrderResponse response(ScoopOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getFlavor().getId(), item.getFlavor().getName(),
                        item.getQuantity(), item.getUnitPrice())).toList();
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotal(), order.getCreatedAt(), items);
    }
}
