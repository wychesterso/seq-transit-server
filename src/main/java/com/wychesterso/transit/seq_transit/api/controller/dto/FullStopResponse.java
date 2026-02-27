package com.wychesterso.transit.seq_transit.api.controller.dto;

import java.util.List;

public record FullStopResponse(
        BriefStopResponse stopInfo,
        List<ServiceResponse> services
) {}
