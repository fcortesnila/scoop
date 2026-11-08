package com.scoop.api.controller;

import com.scoop.api.dto.ApiDtos.InventoryResponse;
import com.scoop.api.dto.ApiDtos.RouteResponse;
import com.scoop.api.service.InventoryInsightService;
import com.scoop.api.service.RouteSuggestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAuthority('ROLE_OPERATOR')")
public class AiController {
    private final InventoryInsightService inventory;
    private final RouteSuggestionService routes;

    public AiController(InventoryInsightService inventory, RouteSuggestionService routes) {
        this.inventory = inventory;
        this.routes = routes;
    }

    @GetMapping("/inventory-alerts")
    public InventoryResponse inventoryAlerts() {
        return inventory.getAlerts();
    }

    @GetMapping("/route-suggestions")
    public RouteResponse routeSuggestions() {
        return routes.suggestions();
    }
}
