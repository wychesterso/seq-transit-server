package com.wychesterso.transit.brisbane_bus.api.controller;

import com.wychesterso.transit.brisbane_bus.api.dto.RouteResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.StopResponse;
import com.wychesterso.transit.brisbane_bus.api.service.RouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/{routeShortName}")
    public List<RouteResponse> getRoutesByShortName(
            @PathVariable String routeShortName) {
        return routeService.getRoutesByShortName(routeShortName);
    }

    @GetMapping("/{routeId}/stops")
    public List<StopResponse> getStopsForRoute(
            @PathVariable String routeId) {
        return routeService.getStopsForRoute(routeId);
    }
}
