package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.StopCache;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.repository.StopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StopService {

    private static final double LATDELTA = 0.009;
    private static final double QUANTIZATION = 0.001;

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
     * @return a list of stop info
     */
    public List<BriefStopResponse> getAdjacentStops(Double lat, Double lon) {
        if (lat == null || lon == null) return null;

        double quantizedLat = quantize(lat, QUANTIZATION);
        double quantizedLon = quantize(lon, QUANTIZATION);

        List<BriefStopResponse> result = cache.getAdjacentStops(quantizedLat, quantizedLon, LATDELTA);
        if (result != null) return result;

        double lonDelta = getLonDelta(LATDELTA, quantizedLat);
        result = repository.findAdjacentStops(quantizedLat, quantizedLon,
                        quantizedLat - LATDELTA, quantizedLat + LATDELTA,
                        quantizedLon - lonDelta, quantizedLon + lonDelta)
                .stream()
                .map(BriefStopResponse::from)
                .toList();
        cache.cacheAdjacentStops(quantizedLat, quantizedLon, LATDELTA, result);

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
                quantizedLat, quantizedLon, LATDELTA);
        if (response != null) return response;

        double lonDelta = getLonDelta(LATDELTA, quantizedLat);
        response = BriefStopResponse.from(
                repository.findMostAdjacentStopForService(routeShortName, tripHeadsign, directionId,
                        quantizedLat, quantizedLon,
                        quantizedLat - LATDELTA, quantizedLat + LATDELTA,
                        quantizedLon - lonDelta, quantizedLon + lonDelta)
        );
        cache.cacheMostAdjacentStopForServiceGroup(
                routeShortName, tripHeadsign, directionId,
                quantizedLat, quantizedLon, LATDELTA, response
        );

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
