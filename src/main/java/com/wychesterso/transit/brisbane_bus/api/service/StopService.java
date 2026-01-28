package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.service.cache.BriefStopResponseList;
import com.wychesterso.transit.brisbane_bus.st.model.Stop;
import com.wychesterso.transit.brisbane_bus.st.repository.StopRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class StopService {

    private static final double LATDELTA = 0.009;

    private final StopRepository repository;
    private final RedisTemplate<String, Object> redis;

    public StopService(
            StopRepository repository,
            RedisTemplate<String, Object> redis) {
        this.repository = repository;
        this.redis = redis;
    }

    public BriefStopResponse getStopInfo(String stopId) {
        String key = "stop:%s:info".formatted(stopId);

        @SuppressWarnings("unchecked")
        BriefStopResponse cached = (BriefStopResponse) redis.opsForValue().get(key);
        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            return cached;
        }

        BriefStopResponse result = toResponse(repository.findStopById(stopId));

        redis.opsForValue().set(
                key,
                result,
                Duration.ofSeconds(86400) // TTL
        );

        return result;
    }

    public List<BriefStopResponse> getAdjacentStops(Double lat, Double lon) {
        String key = "stops:adjacent:%.5f:%.5f".formatted(lat, lon);

        @SuppressWarnings("unchecked")
        BriefStopResponseList cached = (BriefStopResponseList) redis.opsForValue().get(key);
        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            return cached.briefStopResponseList();
        }

        double lonDelta = LATDELTA / Math.cos(Math.toRadians(lat));

        List<Stop> adjacentStops = repository.findAdjacentStops(
                lat,
                lon,
                lat - LATDELTA,
                lon - lonDelta,
                lat + LATDELTA,
                lon + lonDelta
        );

        List<BriefStopResponse> result = adjacentStops.stream().map(this::toResponse).toList();

        redis.opsForValue().set(
                key,
                new BriefStopResponseList(result),
                Duration.ofSeconds(86400) // TTL
        );

        return result;
    }

    public BriefStopResponse getAdjacentStopForService(
            String routeShortName,
            String tripHeadsign,
            int directionId,
            Double lat,
            Double lon) {

        String key = "stops:adjacent-for-service:%s:%s:%d:%.5f:%.5f".formatted(
                routeShortName,
                tripHeadsign,
                directionId,
                lat,
                lon);

        @SuppressWarnings("unchecked")
        BriefStopResponse cached = (BriefStopResponse) redis.opsForValue().get(key);
        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            return cached;
        }

        double lonDelta = LATDELTA / Math.cos(Math.toRadians(lat));

        BriefStopResponse result = toResponse(repository.findMostAdjacentStopForService(
                routeShortName,
                tripHeadsign,
                directionId,
                lat,
                lon,
                lat - LATDELTA,
                lon - lonDelta,
                lat + LATDELTA,
                lon + lonDelta
        ));

        redis.opsForValue().set(
                key,
                result,
                Duration.ofSeconds(86400) // TTL
        );

        return result;
    }

    private BriefStopResponse toResponse(Stop s) {
        return new BriefStopResponse(
                s.getStopId(),
                s.getStopCode(),
                s.getStopName(),
                s.getStopLat(),
                s.getStopLon(),
                s.getZoneId()
        );
    }
}
