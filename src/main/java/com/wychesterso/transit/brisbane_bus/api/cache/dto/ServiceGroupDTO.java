package com.wychesterso.transit.brisbane_bus.api.cache.dto;

import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;

public record ServiceGroupDTO(
        String routeShortName,
        String routeLongName,
        String tripHeadsign,
        Integer directionId,
        Integer routeType,
        String routeColor,
        String routeTextColor
) {
    public static ServiceGroupDTO from(ServiceGroup sg) {
        if (sg == null) return null;
        return new ServiceGroupDTO(
                sg.getRouteShortName(),
                sg.getRouteLongName(),
                sg.getTripHeadsign(),
                sg.getDirectionId(),
                sg.getRouteType(),
                sg.getRouteColor(),
                sg.getRouteTextColor()
        );
    }
}
