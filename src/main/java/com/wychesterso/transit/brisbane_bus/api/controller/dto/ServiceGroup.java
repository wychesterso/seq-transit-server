package com.wychesterso.transit.brisbane_bus.api.controller.dto;

public record ServiceGroup(
        String routeShortName,
        String tripHeadsign,
        int directionId
) {}
