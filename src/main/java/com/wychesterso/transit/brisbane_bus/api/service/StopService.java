package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.StopResponse;
import com.wychesterso.transit.brisbane_bus.api.exception.NotFoundException;
import com.wychesterso.transit.brisbane_bus.st.model.Stop;
import com.wychesterso.transit.brisbane_bus.st.repository.StopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StopService {

    private final StopRepository repository;

    public StopService(StopRepository repository) {
        this.repository = repository;
    }

    public StopResponse getStop(String stopId) {
        return repository.findStopById(stopId)
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Stop not found: " + stopId));
    }

    public List<StopResponse> getStopsForRoute(String routeId) {
        return repository.findStopsForRoute(routeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StopResponse toResponse(Stop s) {
        return new StopResponse(
                s.getStopId(),
                s.getStopCode(),
                s.getStopName(),
                s.getStopLat(),
                s.getStopLon(),
                s.getZoneId()
        );
    }
}
