package com.scoop.api.service;

import com.scoop.api.dto.ApiDtos.StopRequest;
import com.scoop.api.dto.ApiDtos.StopResponse;
import com.scoop.api.entity.TruckStop;
import com.scoop.api.repository.StopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StopService {
    private final StopRepository stops;

    public StopService(StopRepository stops) {
        this.stops = stops;
    }

    @Transactional
    public StopResponse start(StopRequest request) {
        if (stops.findFirstByEndedAtIsNullOrderByStartedAtDesc().isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "End the current stop before starting another");
        }
        TruckStop stop = new TruckStop();
        stop.setName(request.name().trim());
        stop.setLatitude(request.latitude());
        stop.setLongitude(request.longitude());
        stop.setStartedAt(LocalDateTime.now());
        return response(stops.save(stop));
    }

    @Transactional
    public StopResponse end() {
        TruckStop stop = stops.findFirstByEndedAtIsNullOrderByStartedAtDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no active stop"));
        stop.setEndedAt(LocalDateTime.now());
        return response(stop);
    }

    @Transactional(readOnly = true)
    public StopResponse current() {
        return stops.findFirstByEndedAtIsNullOrderByStartedAtDesc().map(StopService::response).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<StopResponse> history() {
        return stops.findAll().stream().map(StopService::response).toList();
    }

    private static StopResponse response(TruckStop stop) {
        return new StopResponse(stop.getId(), stop.getName(), stop.getLatitude(), stop.getLongitude(),
                stop.getStartedAt(), stop.getEndedAt(), stop.getEndedAt() == null);
    }
}
