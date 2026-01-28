package com.wychesterso.transit.brisbane_bus.api.service.cache;

public record ServiceGroupDTO(
        String routeShortName,
        String routeLongName,
        String tripHeadsign,
        Integer directionId,
        String routeColor,
        String routeTextColor
) {}
