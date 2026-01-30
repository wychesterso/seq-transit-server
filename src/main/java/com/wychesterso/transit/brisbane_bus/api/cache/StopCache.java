package com.wychesterso.transit.brisbane_bus.api.cache;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.BriefStopResponseList;
import com.wychesterso.transit.brisbane_bus.api.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.st.repository.StopRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class StopCache {

    private static final Duration TTL = Duration.ofHours(24);
    private static final double QUANTIZATION = 0.001;

    private final StopRepository repository;
    private final RedisTemplate<String, Object> redis;

    public StopCache(
            StopRepository repository,
            RedisTemplate<String, Object> redis
    ) {
        this.repository = repository;
        this.redis = redis;
    }

    /**
     * Get the info for a specified stop
     * @param stopId the stop to query
     * @return the stop info
     */
    public BriefStopResponse getStop(String stopId) {
        String key = keyForStop(stopId);

        @SuppressWarnings("unchecked")
        BriefStopResponse cached = (BriefStopResponse) redis.opsForValue().get(key);
        if (cached != null) return cached;

        BriefStopResponse result = BriefStopResponse.from(repository.findStopById(stopId));

        redis.opsForValue().set(
                key,
                result,
                TTL);

        return result;
    }

    private String keyForStop(String stopId) {
        return "stop:" + stopId + ":info";
    }

    /**
     * Get the info for all stops within a certain range
     * @param lat the latitude value to start search from
     * @param lon the longitude value to start search from
     * @param latDelta controls the range of the search
     * @return a list of stop info
     */
    public List<BriefStopResponse> getAdjacentStops(Double lat, Double lon, double latDelta) {
        String key = keyForAdjacent(
                quantize(lat, QUANTIZATION),
                quantize(lon, QUANTIZATION),
                latDelta);

        @SuppressWarnings("unchecked")
        BriefStopResponseList cached = (BriefStopResponseList) redis.opsForValue().get(key);
        if (cached != null) return cached.briefStopResponseList();

        double lonDelta = getLonDelta(latDelta, lat);
        List<BriefStopResponse> result = repository.findAdjacentStops(lat, lon,
                lat - latDelta, lat + latDelta,
                lon - lonDelta, lon + lonDelta)
                .stream()
                .map(BriefStopResponse::from)
                .toList();

        redis.opsForValue().set(
                key,
                new BriefStopResponseList(result),
                TTL);

        return result;
    }

    private String keyForAdjacent(Double quantizedLat, Double quantizedLon, double latDelta) {
        return "stops:adjacent:%.3f:%.3f:%.3f".formatted(quantizedLat, quantizedLon, latDelta);
    }

    /**
     * Get the info for a stop on the given service group, closest to the given coordinates
     * @param routeShortName service group's route name
     * @param tripHeadsign service group's headsign
     * @param directionId service group's direction identifier
     * @param lat the latitude value to start search from
     * @param lon the longitude value to start search from
     * @param latDelta controls the range of the search
     * @return the stop info
     */
    public BriefStopResponse getMostAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double lat,
            Double lon,
            double latDelta
    ) {
        String key = keyForMostAdjacent(
                routeShortName, tripHeadsign, directionId,
                quantize(lat, QUANTIZATION),
                quantize(lon, QUANTIZATION),
                latDelta);

        @SuppressWarnings("unchecked")
        BriefStopResponse cached = (BriefStopResponse) redis.opsForValue().get(key);
        if (cached != null) return cached;

        double lonDelta = getLonDelta(latDelta, lat);
        BriefStopResponse result = BriefStopResponse.from(
                repository.findMostAdjacentStopForService(routeShortName, tripHeadsign, directionId,
                        lat, lon,
                        lat - latDelta, lat + latDelta,
                        lon - lonDelta, lon + lonDelta)
        );

        redis.opsForValue().set(
                key,
                result,
                TTL);

        return result;
    }

    private String keyForMostAdjacent(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double quantizedLat,
            Double quantizedLon,
            double latDelta
    ) {
        return "stops:adjacent-for-service:%s:%s:%d:%.3f:%.3f:%.3f".formatted(
                routeShortName,
                tripHeadsign,
                directionId,
                quantizedLat,
                quantizedLon,
                latDelta);
    }

    private double getLonDelta(double latDelta, double lat) {
        return latDelta / Math.cos(Math.toRadians(lat));
    }

    private double quantize(double value, double step) {
        return Math.round(value / step) * step;
    }
}
