package com.wychesterso.transit.brisbane_bus.api.cache;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

@Component
public class ArrivalsCache {

    private static final Duration TTL = Duration.ofSeconds(30);

    private final RedisTemplate<String, Object> redis;

    public ArrivalsCache(RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    public ArrivalsAtStopResponse getNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            LocalDate serviceDate
    ) {
        String key = keyForNextArrivalsForServiceAtStop(
                stopId, routeShortName, tripHeadsign, directionId, serviceDate);

        @SuppressWarnings("unchecked")
        ArrivalsAtStopResponse cached = (ArrivalsAtStopResponse) redis.opsForValue().get(key);
        return cached;
    }

    public void cacheNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            LocalDate serviceDate,
            ArrivalsAtStopResponse response
    ) {
        String key = keyForNextArrivalsForServiceAtStop(
                stopId, routeShortName, tripHeadsign, directionId, serviceDate);

        redis.opsForValue().set(
                key,
                response,
                TTL);
    }

    private String keyForNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            LocalDate serviceDate
    ) {
        return "stop:arrivals-for-service:%s:%s:%s:%d:%s"
                .formatted(stopId, routeShortName, tripHeadsign, directionId, serviceDate);
    }
}
