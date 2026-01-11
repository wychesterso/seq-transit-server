package com.wychesterso.transit.brisbane_bus.api;

import com.wychesterso.transit.brisbane_bus.dto.RouteAtStopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.dto.StopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.service.StopArrivalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stops")
public class StopArrivalController {

    private final StopArrivalService arrivalService;

    public StopArrivalController(StopArrivalService arrivalService) {
        this.arrivalService = arrivalService;
    }

    @GetMapping("/{stopId}/arrivals")
    public List<StopArrivalDTO> getArrivalsForStop(@PathVariable String stopId) {
        return arrivalService.getNextArrivalsForStop(stopId);
    }

    @GetMapping("/{routeId}/{stopId}/arrivals")
    public List<RouteAtStopArrivalDTO> getArrivalsForRouteAtStop(
            @PathVariable String routeId,
            @PathVariable String stopId) {
        return arrivalService.getNextArrivalsForRouteAtStop(stopId, routeId);
    }
}