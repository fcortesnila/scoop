package com.scoop.api.controller;

import com.scoop.api.dto.ApiDtos.FlavorRequest;
import com.scoop.api.dto.ApiDtos.FlavorResponse;
import com.scoop.api.service.FlavorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flavors")
public class FlavorController {
    private final FlavorService flavorService;

    public FlavorController(FlavorService flavorService) {
        this.flavorService = flavorService;
    }

    @GetMapping
    public List<FlavorResponse> list() {
        return flavorService.list();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_OPERATOR')")
    public FlavorResponse create(@Valid @RequestBody FlavorRequest request) {
        return flavorService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OPERATOR')")
    public FlavorResponse update(@PathVariable Long id, @Valid @RequestBody FlavorRequest request) {
        return flavorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OPERATOR')")
    public void delete(@PathVariable Long id) {
        flavorService.delete(id);
    }
}
