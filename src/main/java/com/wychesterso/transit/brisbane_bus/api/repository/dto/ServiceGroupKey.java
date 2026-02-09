package com.wychesterso.transit.brisbane_bus.api.repository.dto;

public record ServiceGroupKey(
        String routeShortName,
        String tripHeadsign,
        Integer directionId
) {}
