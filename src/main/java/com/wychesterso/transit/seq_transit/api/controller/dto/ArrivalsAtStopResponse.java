package com.wychesterso.transit.seq_transit.api.controller.dto;

import java.util.List;

public record ArrivalsAtStopResponse(
        BriefStopResponse stop,
        Integer stopSequence,
        Boolean isFirstStop,
        Boolean isLastStop,
        List<ArrivalResponse> nextThreeArrivals
) {}
