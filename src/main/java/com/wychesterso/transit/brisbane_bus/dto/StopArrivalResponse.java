package com.wychesterso.transit.brisbane_bus.dto;

public record StopArrivalResponse(
        String tripId,
        int arrivalTimeSeconds,
        String arrivalTimeLocal,
        int departureTimeSeconds,
        String departureTimeLocal
) {}