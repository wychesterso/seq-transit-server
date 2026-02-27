package com.wychesterso.transit.seq_transit.api.cache;

import com.wychesterso.transit.seq_transit.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.seq_transit.api.controller.dto.model.AdjacentRadius;

import java.util.List;

public interface StopCache {

    BriefStopResponse getStop(String stopId);

    void cacheStop(String stopId, BriefStopResponse response);

    List<BriefStopResponse> getAdjacentStops(String cellId, AdjacentRadius radius);

    void cacheAdjacentStops(
            String cellId, AdjacentRadius radius,
            List<BriefStopResponse> result
    );

    BriefStopResponse getMostAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double quantizedLat,
            Double quantizedLon
    );

    void cacheMostAdjacentStopForServiceGroup(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double quantizedLat,
            Double quantizedLon,
            BriefStopResponse response
    );
}
