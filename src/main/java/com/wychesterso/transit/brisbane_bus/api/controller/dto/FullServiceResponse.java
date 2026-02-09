package com.wychesterso.transit.brisbane_bus.api.controller.dto;

import java.util.List;

public record FullServiceResponse(
        ServiceGroup serviceGroup,
        String routeShortName,
        String routeLongName,
        Integer routeType,
        String routeColor,
        String routeTextColor,
        BriefStopResponse adjacentStop,
        List<ArrivalsAtStopResponse> arrivalsAtStops
) {}
