package com.wychesterso.transit.brisbane_bus.rt.model;

public record RtStopDelay(
        String tripId,
        String stopId,
        Integer arrivalDelaySeconds,
        Integer departureDelaySeconds,
        boolean cancelled,
        boolean skipped
) {}