package com.wychesterso.transit.brisbane_bus.api.dto;

public record RouteResponse(
        String routeId,
        String routeShortName,
        String routeLongName,
        String routeColor,
        String routeTextColor
) {}
