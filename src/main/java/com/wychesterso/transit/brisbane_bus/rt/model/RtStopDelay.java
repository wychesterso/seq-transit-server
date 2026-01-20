package com.wychesterso.transit.brisbane_bus.rt.model;

public record RtStopDelay(
        String tripId,
        String stopId,
        Integer effectiveArrivalSeconds,
        Integer effectiveDepartureSeconds,
        boolean cancelled,
        boolean skipped
) {}