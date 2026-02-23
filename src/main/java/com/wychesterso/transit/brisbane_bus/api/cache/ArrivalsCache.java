package com.wychesterso.transit.brisbane_bus.api.cache;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;

import java.time.LocalDate;

public interface ArrivalsCache {

    ArrivalsAtStopResponse getNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            LocalDate serviceDate
    );

    void cacheNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            LocalDate serviceDate,
            ArrivalsAtStopResponse response
    );
}
