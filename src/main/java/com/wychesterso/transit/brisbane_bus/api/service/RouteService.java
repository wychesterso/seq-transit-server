package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.RouteResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.StopResponse;
import com.wychesterso.transit.brisbane_bus.st.model.Route;
import com.wychesterso.transit.brisbane_bus.st.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository repository;
    private final StopService stopService;

    public RouteService(RouteRepository repository, StopService stopService) {
        this.repository = repository;
        this.stopService = stopService;
    }

    public List<RouteResponse> getRoutesByShortName(String routeShortName) {
        return repository.findRoutesByShortName(routeShortName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StopResponse> getStopsForRoute(String routeId) {
        return stopService.getStopsForRoute(routeId);
    }

    private RouteResponse toResponse(Route r) {
        return new RouteResponse(
                r.getRouteId(),
                r.getRouteShortName(),
                r.getRouteLongName(),
                r.getRouteColor(),
                r.getRouteTextColor()
        );
    }
}
