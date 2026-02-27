package com.wychesterso.transit.seq_transit.api.cache.dto;

import com.wychesterso.transit.seq_transit.api.controller.dto.BriefStopResponse;

import java.util.List;

public record BriefStopResponseList(
        List<BriefStopResponse> briefStopResponseList
) {}
