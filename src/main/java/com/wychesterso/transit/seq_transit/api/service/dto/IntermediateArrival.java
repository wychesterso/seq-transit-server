package com.wychesterso.transit.seq_transit.api.service.dto;

import com.wychesterso.transit.seq_transit.api.repository.dto.StopArrival;

public record IntermediateArrival(
        StopArrival stopArrival,
        int scheduledArrSeconds,
        int effectiveArrSeconds,
        int scheduledDepSeconds,
        int effectiveDepSeconds,
        boolean hasRt,
        boolean cancelled,
        boolean skipped
) {}
