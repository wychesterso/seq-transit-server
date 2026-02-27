package com.wychesterso.transit.seq_transit.api.cache;

import com.wychesterso.transit.seq_transit.api.controller.dto.ArrivalsAtStopResponse;

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
