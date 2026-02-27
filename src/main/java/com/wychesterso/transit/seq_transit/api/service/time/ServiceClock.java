package com.wychesterso.transit.seq_transit.api.service.time;

import java.time.LocalDate;

public record ServiceClock(
        LocalDate serviceDate,
        int serviceSeconds
) {}