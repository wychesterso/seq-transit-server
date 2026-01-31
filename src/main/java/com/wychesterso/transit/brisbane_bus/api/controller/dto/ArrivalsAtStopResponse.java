package com.wychesterso.transit.brisbane_bus.api.controller.dto;

import java.util.List;

public record ArrivalsAtStopResponse(
        BriefStopResponse stop,
        Integer stopSequence,
        List<ArrivalResponse> nextThreeArrivals
) {}
