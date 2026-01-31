package com.wychesterso.transit.brisbane_bus.api.controller.dto;

import java.util.List;

public record FullServiceResponse(
        ServiceGroup serviceGroup,
        String routeShortName,
        String routeLongName,
        String routeColor,
        String routeTextColor,
        List<ArrivalsAtStopResponse> arrivalsAtStops
) {}
