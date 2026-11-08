package com.scoop.api.controller;

import com.scoop.api.dto.ApiDtos.StopRequest;
import com.scoop.api.dto.ApiDtos.StopResponse;
import com.scoop.api.service.StopService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
public class StopController {
    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @GetMapping("/current")
    public StopResponse current() {
        return stopService.current();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_OPERATOR')")
    public List<StopResponse> history() {
        return stopService.history();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_OPERATOR')")
    public StopResponse start(@Valid @RequestBody StopRequest request) {
        return stopService.start(request);
    }

    @PostMapping("/end")
    @PreAuthorize("hasAuthority('ROLE_OPERATOR')")
    public StopResponse end() {
        return stopService.end();
    }
}
