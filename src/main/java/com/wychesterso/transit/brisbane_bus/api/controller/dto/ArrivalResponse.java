package com.wychesterso.transit.brisbane_bus.api.controller.dto;

public record ArrivalResponse(
        String tripId,

        int scheduledArrivalSeconds,
        String scheduledArrivalLocal,

        int effectiveArrivalSeconds,
        String effectiveArrivalLocal,
        Integer arrivalDelaySeconds,

        int scheduledDepartureSeconds,
        String scheduledDepartureLocal,

        int effectiveDepartureSeconds,
        String effectiveDepartureLocal,
        Integer departureDelaySeconds,

        boolean realTime,
        boolean cancelled,
        boolean skipped
) {}