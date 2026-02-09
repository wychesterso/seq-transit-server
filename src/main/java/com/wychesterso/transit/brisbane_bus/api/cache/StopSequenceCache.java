package com.wychesterso.transit.brisbane_bus.api.cache;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequence;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequenceAndShape;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class StopSequenceCache {

    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redis;

    public StopSequenceCache(
            RedisTemplate<String, Object> redis
    ) {
        this.redis = redis;
    }

    public CanonicalStopSequence getCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        String key = keyForSequence(routeShortName, tripHeadsign, directionId);

        @SuppressWarnings("unchecked")
        CanonicalStopSequence cached = (CanonicalStopSequence) redis.opsForValue().get(key);
        return cached;
    }

    public void cacheCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            CanonicalStopSequence result
    ) {
        String key = keyForSequence(routeShortName, tripHeadsign, directionId);

        redis.opsForValue().set(
                key,
                result,
                TTL);
    }

    private String keyForSequence(String routeShortName, String tripHeadsign, Integer directionId) {
        return "service:" + routeShortName + ":stoplist:" + tripHeadsign + ":" + directionId;
    }

    public CanonicalStopSequenceAndShape getCanonicalStopSequenceAndShape(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        String key = keyForSequenceAndShape(routeShortName, tripHeadsign, directionId);

        @SuppressWarnings("unchecked")
        CanonicalStopSequenceAndShape cached = (CanonicalStopSequenceAndShape) redis.opsForValue().get(key);
        return cached;
    }

    public void cacheCanonicalStopSequenceAndShape(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            CanonicalStopSequenceAndShape result
    ) {
        String key = keyForSequenceAndShape(routeShortName, tripHeadsign, directionId);

        redis.opsForValue().set(
                key,
                result,
                TTL);
    }

    private String keyForSequenceAndShape(String routeShortName, String tripHeadsign, Integer directionId) {
        return "service:" + routeShortName + ":stopListAndShape:" + tripHeadsign + ":" + directionId;
    }
}
