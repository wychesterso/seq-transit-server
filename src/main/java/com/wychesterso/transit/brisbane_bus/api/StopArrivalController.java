package com.wychesterso.transit.brisbane_bus.api;

import com.wychesterso.transit.brisbane_bus.dto.StopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.dto.StopArrivalResponse;
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
    public List<StopArrivalResponse> getArrivalsForRouteAtStop(
            @PathVariable String stopId,
            @RequestParam(required = false) String routeId) {
        if (routeId != null) {
            return arrivalService.getNextArrivalsForStop(stopId);
        }
        return arrivalService.getNextArrivalsForRouteAtStop(stopId, routeId);
    }
}