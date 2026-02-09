package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.StopCache;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.model.AdjacentRadius;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.repository.StopRepository;
import com.wychesterso.transit.brisbane_bus.config.H3Utils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StopService {

    private static final double QUANTIZATION = 0.0005;

    private final StopRepository repository;
    private final StopCache cache;

    public StopService(StopRepository repository, StopCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    /**
     * Get the info for a specified stop
     * @param stopId the stop to query
     * @return the stop info
     */
    public BriefStopResponse getStop(String stopId) {
        if (stopId == null || stopId.isBlank()) return null;

        BriefStopResponse response = cache.getStop(stopId);
        if (response != null) return response;

        response = BriefStopResponse.from(repository.findStopById(stopId));
        cache.cacheStop(stopId, response);

        return response;
    }

    /**
     * Get the info for all stops within a certain range
     * @param lat the latitude value to start search from
     * @param lon the longitude value to start search from
     * @param radius the search radius
     * @return a list of stop info
     */
    public List<BriefStopResponse> getAdjacentStops(Double lat, Double lon, AdjacentRadius radius) {
        if (lat == null || lon == null || radius == null) return null;

        double latDelta = radius.toLatDelta();

        double quantizedLat = quantize(lat, QUANTIZATION);
        double quantizedLon = quantize(lon, QUANTIZATION);
        String cellId = H3Utils.latLonToCell(lat, lon, 8);

        List<BriefStopResponse> result = cache.getAdjacentStops(cellId, radius);
        if (result != null) return result;

        double lonDelta = getLonDelta(latDelta, quantizedLat);
        result = repository.findAdjacentStops(quantizedLat, quantizedLon,
                        quantizedLat - latDelta, quantizedLat + latDelta,
                        quantizedLon - lonDelta, quantizedLon + lonDelta)
                .stream()
                .map(BriefStopResponse::from)
                .toList();
        cache.cacheAdjacentStops(cellId, radius, result);

        return result;
    }

    /**
     * Get the info for a stop on the given service group, closest to the given coordinates
     * @param routeShortName service group's route name
     * @param tripHeadsign service group's headsign
     * @param directionId service group's direction identifier
     * @param lat the latitude value to start search from
     * @param lon the longitude value to start search from
     * @return the stop info
     */
    public BriefStopResponse getAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double lat,
            Double lon
    ) {
        if (routeShortName == null || tripHeadsign == null || directionId == null ||
                lat == null || lon == null) return null;

        double quantizedLat = quantize(lat, QUANTIZATION);
        double quantizedLon = quantize(lon, QUANTIZATION);

        BriefStopResponse response = cache.getMostAdjacentStopForServiceGroup(
                routeShortName, tripHeadsign, directionId,
                quantizedLat, quantizedLon);
        if (response != null) return response;

        response = BriefStopResponse.from(
                repository.findMostAdjacentStopForService(routeShortName, tripHeadsign, directionId,
                        quantizedLat, quantizedLon)
        );
        cache.cacheMostAdjacentStopForServiceGroup(
                routeShortName, tripHeadsign, directionId,
                quantizedLat, quantizedLon, response);

        return response;
    }

    // HELPERS

    private double getLonDelta(double latDelta, double lat) {
        return latDelta / Math.cos(Math.toRadians(lat));
    }

    private double quantize(double value, double step) {
        return Math.round(value / step) * step;
    }
}
