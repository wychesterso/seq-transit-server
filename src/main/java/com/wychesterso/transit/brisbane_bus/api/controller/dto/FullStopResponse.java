package com.wychesterso.transit.brisbane_bus.api.controller.dto;

import java.util.List;

public record FullStopResponse(
        BriefStopResponse stopInfo,
        List<BriefServiceResponse> services
) {}
