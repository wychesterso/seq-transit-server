package com.wychesterso.transit.brisbane_bus.api.controller.dto;

public record BriefServiceResponse(
        ServiceGroup routeGroup,
        String routeShortName,
        String routeLongName,
        ArrivalsAtStopResponse arrivalsAtNearestStop
) {}
