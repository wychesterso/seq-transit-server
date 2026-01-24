package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.exception.NotFoundException;
import com.wychesterso.transit.brisbane_bus.st.model.Stop;
import com.wychesterso.transit.brisbane_bus.st.repository.StopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StopService {

    private static final double LATDELTA = 0.009;

    private final StopRepository repository;

    public StopService(StopRepository repository) {
        this.repository = repository;
    }

    public List<BriefStopResponse> getAdjacentStops(Double lat, Double lon) {
        double lonDelta = 0.009 / Math.cos(Math.toRadians(lat));

        List<Stop> adjacentStops = repository.findAdjacentStops(
                lat,
                lon,
                lat - LATDELTA,
                lon - lonDelta,
                lat + LATDELTA,
                lon + lonDelta
        );

        return adjacentStops.stream().map(this::toResponse).toList();
    }

    public BriefStopResponse getStop(String stopId) {
        return repository.findStopById(stopId)
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Stop not found: " + stopId));
    }

    private BriefStopResponse toResponse(Stop s) {
        return new BriefStopResponse(
                s.getStopId(),
                s.getStopCode(),
                s.getStopName(),
                s.getStopLat(),
                s.getStopLon(),
                s.getZoneId()
        );
    }
}
