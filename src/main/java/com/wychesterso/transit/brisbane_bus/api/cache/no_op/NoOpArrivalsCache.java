package com.wychesterso.transit.brisbane_bus.api.cache.no_op;

import com.wychesterso.transit.brisbane_bus.api.cache.ArrivalsCache;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@ConditionalOnProperty(
        name = "spring.redis.enabled",
        havingValue = "false"
)
public class NoOpArrivalsCache implements ArrivalsCache {

    @Override
    public ArrivalsAtStopResponse getNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            LocalDate serviceDate
    ) {
        return null; // always miss
    }

    @Override
    public void cacheNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            LocalDate serviceDate,
            ArrivalsAtStopResponse response
    ) {
        // do nothing
    }
}
