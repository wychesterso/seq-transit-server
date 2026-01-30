package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.StopCache;
import com.wychesterso.transit.brisbane_bus.api.dto.BriefStopResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StopService {

    private static final double LATDELTA = 0.009;

    private final StopCache cache;

    public StopService(StopCache cache) {
        this.cache = cache;
    }

    public BriefStopResponse getStop(String stopId) {
        if (stopId == null || stopId.isBlank()) return null;

        return cache.getStop(stopId);
    }

    public List<BriefStopResponse> getAdjacentStops(Double lat, Double lon) {
        if (lat == null || lon == null) return null;

        return cache.getAdjacentStops(lat, lon, LATDELTA);
    }

    public BriefStopResponse getAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double lat,
            Double lon
    ) {
        if (routeShortName == null || tripHeadsign == null || directionId == null ||
                lat == null || lon == null) return null;

        return cache.getMostAdjacentStopForServiceGroup(
                routeShortName, tripHeadsign, directionId,
                lat, lon, LATDELTA);
    }
}
