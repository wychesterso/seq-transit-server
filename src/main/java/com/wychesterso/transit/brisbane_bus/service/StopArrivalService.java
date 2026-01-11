package com.wychesterso.transit.brisbane_bus.service;

import com.wychesterso.transit.brisbane_bus.dto.RouteAtStopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.dto.StopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.repository.StopArrivalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class StopArrivalService {

    private final StopArrivalRepository repository;

    public StopArrivalService(StopArrivalRepository repository) {
        this.repository = repository;
    }

    public List<StopArrivalDTO> getNextArrivalsForStop(String stopId) {
        int nowSeconds = LocalTime.now().toSecondOfDay();
        return repository.findNextArrivalsForStop(stopId, nowSeconds);
    }

    public List<RouteAtStopArrivalDTO> getNextArrivalsForRouteAtStop(String stopId, String routeId) {
        int nowSeconds = LocalTime.now().toSecondOfDay();
        return repository.findNextArrivalsForRouteAtStop(stopId, routeId, nowSeconds);
    }
}