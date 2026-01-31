package com.wychesterso.transit.brisbane_bus.api.cache;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequence;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class StopSequenceCache {

    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redis;

    public StopSequenceCache(
            RedisTemplate<String, Object> redis
    ) {
        this.redis = redis;
    }

    public Map<String, Integer> getCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        String key = key(routeShortName, tripHeadsign, directionId);

        @SuppressWarnings("unchecked")
        CanonicalStopSequence cached = (CanonicalStopSequence) redis.opsForValue().get(key);
        if (cached != null) return cached.stopIdToSequenceMap();
        return null;
    }

    public void cacheCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Map<String, Integer> result
    ) {
        String key = key(routeShortName, tripHeadsign, directionId);

        redis.opsForValue().set(
                key,
                new CanonicalStopSequence(result),
                TTL);
    }

    private String key(String routeShortName, String tripHeadsign, Integer directionId) {
        return "service:" + routeShortName + ":stoplist:" + tripHeadsign + ":" + directionId;
    }
}
