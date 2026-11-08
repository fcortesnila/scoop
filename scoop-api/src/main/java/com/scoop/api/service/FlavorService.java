package com.scoop.api.service;

import com.scoop.api.dto.ApiDtos.FlavorRequest;
import com.scoop.api.dto.ApiDtos.FlavorResponse;
import com.scoop.api.entity.Flavor;
import com.scoop.api.repository.FlavorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FlavorService {
    private final FlavorRepository flavors;

    public FlavorService(FlavorRepository flavors) {
        this.flavors = flavors;
    }

    public List<FlavorResponse> list() {
        return flavors.findAllByOrderByNameAsc().stream().map(FlavorService::response).toList();
    }

    @Transactional
    public FlavorResponse create(FlavorRequest request) {
        Flavor flavor = new Flavor();
        flavor.setName(request.name().trim());
        flavor.setPrice(request.price());
        flavor.setAvailable(request.available());
        return response(flavors.save(flavor));
    }

    @Transactional
    public FlavorResponse update(Long id, FlavorRequest request) {
        Flavor flavor = flavors.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Flavor not found"));
        flavor.setName(request.name().trim());
        flavor.setPrice(request.price());
        flavor.setAvailable(request.available());
        return response(flavor);
    }

    @Transactional
    public void delete(Long id) {
        if (!flavors.existsById(id)) throw new ResponseStatusException(NOT_FOUND, "Flavor not found");
        flavors.deleteById(id);
    }

    private static FlavorResponse response(Flavor flavor) {
        return new FlavorResponse(flavor.getId(), flavor.getName(), flavor.getPrice(), flavor.isAvailable());
    }
}
