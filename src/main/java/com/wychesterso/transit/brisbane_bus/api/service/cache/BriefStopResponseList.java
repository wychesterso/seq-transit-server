package com.wychesterso.transit.brisbane_bus.api.service.cache;

import com.wychesterso.transit.brisbane_bus.api.dto.BriefStopResponse;

import java.util.List;

public record BriefStopResponseList(
        List<BriefStopResponse> briefStopResponseList
) {}
