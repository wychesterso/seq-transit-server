package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.StopResponse;
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

    public List<StopResponse> getAllStops() {
        List<Stop> stops = repository.getStops();
        return mapDTOToResponse(stops);
    }

    public List<StopResponse> getStop(String stopId) {
        List<Stop> stops = repository.findStopById(stopId);
        return mapDTOToResponse(stops);
    }

    private List<StopResponse> mapDTOToResponse(List<Stop> stops) {
        return stops.stream()
                .map(s -> new StopResponse(
                        s.getStopId(),
                        s.getStopCode(),
                        s.getStopName(),
                        s.getStopLat(),
                        s.getStopLon(),
                        s.getZoneId()
                )).toList();
    }
}
