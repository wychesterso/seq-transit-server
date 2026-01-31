package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupAtStopDTO;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupDTO;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroupKey;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServiceGroupWithArrivalsService {

    private final ServiceGroupService serviceGroupService;
    private final ArrivalsService arrivalsService;
    private final StopService stopService;

    public ServiceGroupWithArrivalsService(
            ServiceGroupService serviceGroupService,
            ArrivalsService arrivalsService,
            StopService stopService
    ) {
        this.serviceGroupService = serviceGroupService;
        this.arrivalsService = arrivalsService;
        this.stopService = stopService;
    }

    /**
     * Get a list of services with next 3 arrivals at nearest stop, filtered by prefix
     * @param prefix the prefix search term
     * @return list of services
     */
    public List<BriefServiceResponse> getServicesByPrefix(String prefix, double lat, double lon) {
        if (prefix == null || prefix.isBlank()) return List.of();

        return serviceGroupService.getServicesByPrefix(prefix).stream()
                .map(sg -> toResponseAtNearestStop(sg, lat, lon))
                .toList();
    }

    /**
     * Get a list of services at a particular stop, with next 3 arrivals
     * @param stopId the stop to query
     * @return list of services
     */
    public List<BriefServiceResponse> getServicesAtStop(String stopId) {
        if (stopId == null || stopId.isBlank()) return List.of();

        return serviceGroupService.getServicesAtStop(stopId).stream()
                .map(sg -> toResponseAtGivenStop(sg, stopId))
                .toList();
    }

    /**
     * Get a list of services at a set of stops
     * @param stopIds the stops to query
     * @param lat the origin latitude
     * @param lon the origin longitude
     * @return list of services without duplicates, sorted by adjacency to origin
     */
    public List<BriefServiceResponse> getServicesAtStops(List<String> stopIds, double lat, double lon) {
        if (stopIds == null || stopIds.isEmpty()) return List.of();

        record Candidate(
                ServiceGroupAtStopDTO serviceGroup,
                double distance
        ) {}

        Map<ServiceGroupKey, Candidate> closestPerService = new HashMap<>();

        for (String stopId : new HashSet<>(stopIds)) {
            List<ServiceGroupAtStopDTO> serviceGroupsAtStop = serviceGroupService.getServicesAtStop(stopId);

            for (ServiceGroupAtStopDTO sg : serviceGroupsAtStop) {
                double distance = squaredDistance(
                        lat,
                        lon,
                        sg.stopLat(),
                        sg.stopLon()
                );

                ServiceGroupKey key = new ServiceGroupKey(
                        sg.routeShortName(),
                        sg.tripHeadsign(),
                        sg.directionId()
                );

                closestPerService.merge(
                        key,
                        new Candidate(sg, distance),
                        (a, b) -> a.distance <= b.distance ? a : b
                );
            }
        }

        return closestPerService.values().stream()
                .sorted(
                        Comparator
                                .comparingDouble(Candidate::distance)
                                .thenComparing(cd -> cd.serviceGroup.routeShortName())
                )
                .map(cd -> toResponseAtGivenStop(
                        cd.serviceGroup,
                        cd.serviceGroup.stopId()
                ))
                .toList();
    }

    // HELPERS

    /**
     * Creates a BriefServiceResponse, using next 3 arrivals at the specified stop
     * @param dto the dto to convert to response
     * @param stopId the specified stop's id
     * @return a service response
     */
    private BriefServiceResponse toResponseAtGivenStop(
            ServiceGroupAtStopDTO dto,
            String stopId) {

        ArrivalsAtStopResponse arrivalsAtStopResponse = arrivalsService.getNextArrivalsForServiceAtStop(
                stopId,
                dto.routeShortName(),
                dto.tripHeadsign(),
                dto.directionId()
        );

        return new BriefServiceResponse(
                new ServiceGroup(
                        dto.routeShortName(),
                        dto.tripHeadsign(),
                        dto.directionId()
                ),
                dto.routeShortName(),
                dto.routeLongName(),
                arrivalsAtStopResponse
        );
    }

    /**
     * Creates a BriefServiceResponse, using next 3 arrivals at the nearest stop to given coordinate
     * @param dto the dto to convert to response
     * @param lat the coordinate's latitude
     * @param lon the coordinate's longitude
     * @return a service response
     */
    private BriefServiceResponse toResponseAtNearestStop(
            ServiceGroupDTO dto,
            double lat,
            double lon) {

        BriefStopResponse adjacentStop = stopService.getAdjacentStopForServiceGroup(
                dto.routeShortName(),
                dto.tripHeadsign(),
                dto.directionId(),
                lat,
                lon
        );
        ArrivalsAtStopResponse arrivalsAtStopResponse = arrivalsService.getNextArrivalsForServiceAtStop(
                adjacentStop.stopId(),
                dto.routeShortName(),
                dto.tripHeadsign(),
                dto.directionId()
        );

        return new BriefServiceResponse(
                new ServiceGroup(
                        dto.routeShortName(),
                        dto.tripHeadsign(),
                        dto.directionId()
                ),
                dto.routeShortName(),
                dto.routeLongName(),
                arrivalsAtStopResponse
        );
    }

    private double squaredDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {
        return Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2);
    }
}
