package com.wychesterso.transit.brisbane_bus.api.controller.dto;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.model.ServiceGroup;

public record BriefServiceResponse(
        ServiceGroup serviceGroup,
        String routeShortName,
        String routeLongName,
        Integer routeType
) {}
