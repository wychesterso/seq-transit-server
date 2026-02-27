package com.wychesterso.transit.seq_transit.api.cache.dto;

import com.wychesterso.transit.seq_transit.api.repository.dto.ServiceGroupAtStop;

public record ServiceGroupAtStopDTO(
        String routeShortName,
        String routeLongName,
        String tripHeadsign,
        Integer directionId,
        Integer routeType,
        String routeColor,
        String routeTextColor,
        String stopId,
        Double stopLat,
        Double stopLon
) {
    public static ServiceGroupAtStopDTO from(ServiceGroupAtStop sg) {
        return new ServiceGroupAtStopDTO(
                sg.getRouteShortName(),
                sg.getRouteLongName(),
                sg.getTripHeadsign(),
                sg.getDirectionId(),
                sg.getRouteType(),
                sg.getRouteColor(),
                sg.getRouteTextColor(),
                sg.getStopId(),
                sg.getStopLat(),
                sg.getStopLon()
        );
    }
}
