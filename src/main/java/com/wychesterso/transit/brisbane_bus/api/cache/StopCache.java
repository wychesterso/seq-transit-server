package com.wychesterso.transit.brisbane_bus.api.cache;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.BriefStopResponseList;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class StopCache {

    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redis;

    public StopCache(RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    public BriefStopResponse getStop(String stopId) {
        String key = keyForStop(stopId);

        @SuppressWarnings("unchecked")
        BriefStopResponse cached = (BriefStopResponse) redis.opsForValue().get(key);
        return cached;
    }

    public void cacheStop(String stopId, BriefStopResponse response) {
        String key = keyForStop(stopId);

        redis.opsForValue().set(
                key,
                response,
                TTL);
    }

    private String keyForStop(String stopId) {
        return "stop:" + stopId + ":info";
    }

    public List<BriefStopResponse> getAdjacentStops(
            Double quantizedLat, Double quantizedLon,
            double latDelta
    ) {
        String key = keyForAdjacent(
                quantizedLat,
                quantizedLon,
                latDelta);

        @SuppressWarnings("unchecked")
        BriefStopResponseList cached = (BriefStopResponseList) redis.opsForValue().get(key);
        if (cached != null) return cached.briefStopResponseList();
        return null;
    }

    public void cacheAdjacentStops(
            Double quantizedLat, Double quantizedLon, double latDelta,
            List<BriefStopResponse> result
    ) {
        String key = keyForAdjacent(
                quantizedLat,
                quantizedLon,
                latDelta);

        redis.opsForValue().set(
                key,
                new BriefStopResponseList(result),
                TTL);
    }

    private String keyForAdjacent(Double quantizedLat, Double quantizedLon, double latDelta) {
        return "stops:adjacent:%.3f:%.3f:%.3f".formatted(quantizedLat, quantizedLon, latDelta);
    }

    public BriefStopResponse getMostAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double quantizedLat,
            Double quantizedLon,
            double latDelta
    ) {
        String key = keyForMostAdjacent(
                routeShortName, tripHeadsign, directionId,
                quantizedLat,
                quantizedLon,
                latDelta);

        @SuppressWarnings("unchecked")
        BriefStopResponse cached = (BriefStopResponse) redis.opsForValue().get(key);
        return cached;
    }

    public void cacheMostAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double quantizedLat,
            Double quantizedLon,
            double latDelta,
            BriefStopResponse response
    ) {
        String key = keyForMostAdjacent(
                routeShortName, tripHeadsign, directionId,
                quantizedLat,
                quantizedLon,
                latDelta);

        redis.opsForValue().set(
                key,
                response,
                TTL);
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
}
