package com.wychesterso.transit.brisbane_bus.api.cache.dto;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;

import java.util.List;

public record BriefStopResponseList(
        List<BriefStopResponse> briefStopResponseList
) {}
