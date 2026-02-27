package com.wychesterso.transit.seq_transit.api.cache.no_op;

import com.wychesterso.transit.seq_transit.api.cache.StopCache;
import com.wychesterso.transit.seq_transit.api.controller.dto.model.AdjacentRadius;
import com.wychesterso.transit.seq_transit.api.controller.dto.BriefStopResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "spring.redis.enabled",
        havingValue = "false"
)
public class NoOpStopCache implements StopCache {

    public BriefStopResponse getStop(String stopId) {
        return null;
    }

    public void cacheStop(String stopId, BriefStopResponse response) {}

    public List<BriefStopResponse> getAdjacentStops(String cellId, AdjacentRadius radius) {
        return null;
    }

    public void cacheAdjacentStops(
            String cellId, AdjacentRadius radius,
            List<BriefStopResponse> result
    ) {}

    public BriefStopResponse getMostAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double quantizedLat,
            Double quantizedLon
    ) {
        return null;
    }

    public void cacheMostAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double quantizedLat,
            Double quantizedLon,
            BriefStopResponse response
    ) {}
}

