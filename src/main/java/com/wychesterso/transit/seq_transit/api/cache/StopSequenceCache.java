package com.wychesterso.transit.seq_transit.api.cache;

import com.wychesterso.transit.seq_transit.api.cache.dto.CanonicalStopSequence;
import com.wychesterso.transit.seq_transit.api.cache.dto.CanonicalStopSequenceAndShape;

public interface StopSequenceCache {

    CanonicalStopSequence getCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    );

    void cacheCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            CanonicalStopSequence result
    );

    CanonicalStopSequenceAndShape getCanonicalStopSequenceAndShape(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    );

    void cacheCanonicalStopSequenceAndShape(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            CanonicalStopSequenceAndShape result
    );
}
