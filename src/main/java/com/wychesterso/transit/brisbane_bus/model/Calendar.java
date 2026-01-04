package com.wychesterso.transit.brisbane_bus.model;

import java.time.LocalDate;

public record Calendar(
        String serviceId,
        Boolean monday,
        Boolean tuesday,
        Boolean wednesday,
        Boolean thursday,
        Boolean friday,
        Boolean saturday,
        Boolean sunday,
        LocalDate startDate,
        LocalDate endDate
) {}