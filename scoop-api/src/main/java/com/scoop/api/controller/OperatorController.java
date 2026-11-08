package com.scoop.api.controller;

import com.scoop.api.dto.ApiDtos.*;
import com.scoop.api.service.OrderService;
import com.scoop.api.service.StopService;
import com.scoop.api.repository.OrderRepository;
import com.scoop.api.entity.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operator")
@PreAuthorize("hasAuthority('ROLE_OPERATOR')")
public class OperatorController {
    private final OrderService orderService;
    private final OrderRepository orders;
    private final StopService stopService;

    public OperatorController(OrderService orderService, OrderRepository orders, StopService stopService) {
        this.orderService = orderService;
        this.orders = orders;
        this.stopService = stopService;
    }

    @GetMapping("/orders")
    public List<OrderResponse> queue() {
        return orderService.queue();
    }

    @PostMapping("/orders")
    public OrderResponse walkUp(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createWalkUp(request);
    }

    @PatchMapping("/orders/{id}")
    public OrderResponse updateOrder(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @GetMapping("/summary/daily")
    public DailySummary dailySummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<ScoopOrder> served = orders.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                OrderStatus.SERVED, start, end);
        Map<Long, long[]> totalsByFlavor = new HashMap<>();
        Map<Long, String> flavorNames = new HashMap<>();
        Map<Integer, Long> countsByHour = new TreeMap<>();
        for (ScoopOrder order : served) {
            countsByHour.merge(order.getCreatedAt().getHour(), 1L, Long::sum);
            for (OrderItem item : order.getItems()) {
                totalsByFlavor.computeIfAbsent(item.getFlavor().getId(), ignored -> new long[1])[0] += item.getQuantity();
                flavorNames.put(item.getFlavor().getId(), item.getFlavor().getName());
            }
        }
        List<TopFlavor> top = totalsByFlavor.entrySet().stream()
                .sorted((first, second) -> Long.compare(second.getValue()[0], first.getValue()[0]))
                .limit(5).map(entry -> new TopFlavor(entry.getKey(), flavorNames.get(entry.getKey()), entry.getValue()[0])).toList();
        List<HourlyOrders> hourly = java.util.stream.IntStream.range(0, 24)
                .mapToObj(hour -> new HourlyOrders(hour, countsByHour.getOrDefault(hour, 0L))).toList();
        BigDecimal value = served.stream().map(ScoopOrder::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DailySummary(served.size(), value, top, hourly);
    }

    @PostMapping("/stops")
    public StopResponse startStop(@Valid @RequestBody StopRequest request) {
        return stopService.start(request);
    }

    @PostMapping("/stops/end")
    public StopResponse endStop() {
        return stopService.end();
    }
}
