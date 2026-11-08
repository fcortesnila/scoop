package com.scoop.api.dto;

import com.scoop.api.entity.OrderStatus;
import com.scoop.api.entity.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record UserResponse(Long id, String name, String email, UserRole role) {}
    public record AuthResponse(String accessToken, UserResponse user) {}
    public record FlavorResponse(Long id, String name, BigDecimal price, boolean available) {}
    public record FlavorRequest(@NotBlank @Size(max = 120) String name, @NotNull @DecimalMin("0.00") BigDecimal price,
                                boolean available) {}
    public record ItemRequest(@NotNull @Positive Long flavorId, @Positive int quantity) {}
    public record CreateOrderRequest(@NotEmpty List<@Valid ItemRequest> items) {}
    public record OrderItemResponse(Long flavorId, String flavorName, int quantity, BigDecimal unitPrice) {}
    public record OrderResponse(Long id, OrderStatus status, BigDecimal total, LocalDateTime createdAt,
                                List<OrderItemResponse> items) {}
    public record StatusRequest(@NotNull OrderStatus status) {}
    public record StopRequest(@NotBlank @Size(max = 160) String name,
                              @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
                              @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude) {}
    public record StopResponse(Long id, String name, BigDecimal latitude, BigDecimal longitude,
                               LocalDateTime startedAt, LocalDateTime endedAt, boolean active) {}
    public record TopFlavor(Long flavorId, String name, long quantity) {}
    public record HourlyOrders(int hour, long orders) {}
    public record DailySummary(long ordersServed, BigDecimal totalValue, List<TopFlavor> topFlavors,
                               List<HourlyOrders> ordersPerHour) {}
    public record InventoryAlert(Long ingredientId, String name, String unit, BigDecimal stock,
                                 BigDecimal averageUsagePerHour, BigDecimal hoursUntilStockout,
                                 BigDecimal threshold, String urgency) {}
    public record InventoryResponse(List<InventoryAlert> alerts, String recommendation) {}
    public record RouteSuggestion(String stopName, BigDecimal latitude, BigDecimal longitude, String reason) {}
    public record RouteResponse(boolean enoughHistory, List<RouteSuggestion> suggestions) {}
    public record ApiError(String message, int status, LocalDateTime timestamp) {}
}
