package com.wychesterso.transit.brisbane_bus.core.model;

import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopDelay;

public record RtStopDelay(
        String tripId,
        String stopId,
        Integer effectiveArrivalSeconds,
        Integer effectiveDepartureSeconds,
        boolean cancelled,
        boolean skipped
) {
    public static RtStopDelay from(StopDelay sd) {
        if (sd == null) return null;
        return new RtStopDelay(
                sd.getTripId(),
                sd.getStopId(),
                sd.getEffectiveArrivalSeconds(),
                sd.getEffectiveDepartureSeconds(),
                sd.getCancelled(),
                sd.getSkipped()
        );
    }
}