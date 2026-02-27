package com.wychesterso.transit.seq_transit.api.cache.no_op;

import com.wychesterso.transit.seq_transit.api.cache.StopSequenceCache;
import com.wychesterso.transit.seq_transit.api.cache.dto.CanonicalStopSequence;
import com.wychesterso.transit.seq_transit.api.cache.dto.CanonicalStopSequenceAndShape;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "spring.redis.enabled",
        havingValue = "false"
)
public class NoOpStopSequenceCache implements StopSequenceCache {

    public CanonicalStopSequence getCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        return null;
    }

    public void cacheCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            CanonicalStopSequence result
    ) {}

    public CanonicalStopSequenceAndShape getCanonicalStopSequenceAndShape(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        return null;
    }

    public void cacheCanonicalStopSequenceAndShape(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            CanonicalStopSequenceAndShape result
    ) {}
}
