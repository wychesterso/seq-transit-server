package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.ServiceId;
import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.repository.ServiceGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceGroupService {

    private final ServiceGroupRepository serviceGroupRepository;
    private final ServiceStopLocator serviceStopLocator;

    public ServiceGroupService(
            ServiceGroupRepository serviceGroupRepository,
            ServiceStopLocator serviceStopLocator) {
        this.serviceGroupRepository = serviceGroupRepository;
        this.serviceStopLocator = serviceStopLocator;
    }

    public List<BriefServiceResponse> getServicesByPrefix(String prefix, double lat, double lon) {
        if (prefix == null || prefix.isBlank()) return List.of();

        return serviceGroupRepository.getServicesByPrefix(prefix)
                .stream()
                .map(sg -> toResponse(sg, lat, lon))
                .toList();
    }

    public List<BriefServiceResponse> getServicesAtStop(String stopId, double lat, double lon) {
        return serviceGroupRepository.getServicesAtStop(stopId)
                .stream()
                .map(sg -> toResponse(sg, lat, lon))
                .toList();
    }

    public List<BriefServiceResponse> getServicesAtStops(List<String> stopIds, double lat, double lon) {
        if (stopIds == null || stopIds.isEmpty()) return List.of();

        return serviceGroupRepository.getServicesAtStops(stopIds.toArray(new String[0]), lat, lon)
                .stream()
                .map(sg -> toResponse(sg, lat, lon))
                .toList();
    }

    private BriefServiceResponse toResponse(
            ServiceGroup serviceGroup,
            double lat,
            double lon) {

        BriefStopResponse adjacentStop = serviceStopLocator.getAdjacentStopForService(
                serviceGroup.getRouteShortName(),
                serviceGroup.getTripHeadsign(),
                serviceGroup.getDirectionId(),
                lat,
                lon
        );
        ArrivalsAtStopResponse arrivalsAtStopResponse = arrivalsService.getNextArrivalsForServiceAtStop(
                adjacentStop.stopId(),
                serviceGroup.getRouteShortName(),
                serviceGroup.getTripHeadsign(),
                serviceGroup.getDirectionId()
        );

        return new BriefServiceResponse(
                new ServiceId(
                        serviceGroup.getRouteShortName(),
                        serviceGroup.getTripHeadsign(),
                        serviceGroup.getDirectionId()
                ),
                serviceGroup.getRouteShortName(),
                serviceGroup.getRouteLongName(),
                arrivalsAtStopResponse
        );
    }
}
