package com.wychesterso.transit.brisbane_bus.api.controller.dto;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.model.CoordinatePoint;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.model.ServiceGroup;

import java.util.List;

public record FullServiceResponse(
        ServiceGroup serviceGroup,
        String routeShortName,
        String routeLongName,
        Integer routeType,
        String routeColor,
        String routeTextColor,
        List<CoordinatePoint> shape,
        BriefStopResponse adjacentStop,
        List<ArrivalsAtStopResponse> arrivalsAtStops
) {}
