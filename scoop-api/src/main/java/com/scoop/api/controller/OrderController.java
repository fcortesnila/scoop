package com.scoop.api.controller;

import com.scoop.api.dto.ApiDtos.CreateOrderRequest;
import com.scoop.api.dto.ApiDtos.OrderResponse;
import com.scoop.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OPERATOR')")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, Jwt jwt) {
        return orderService.create(request, jwt.getSubject());
    }

    @GetMapping
    public List<OrderResponse> list(Jwt jwt) {
        return orderService.customerOrders(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id, Jwt jwt) {
        return orderService.customerOrder(id, jwt.getSubject());
    }
}
