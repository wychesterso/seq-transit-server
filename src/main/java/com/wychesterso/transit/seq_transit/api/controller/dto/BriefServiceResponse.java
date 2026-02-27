package com.wychesterso.transit.seq_transit.api.controller.dto;

import com.wychesterso.transit.seq_transit.api.controller.dto.model.ServiceGroup;

public record BriefServiceResponse(
        ServiceGroup serviceGroup,
        String routeShortName,
        String routeLongName,
        Integer routeType
) {}
