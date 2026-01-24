package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.ServiceId;
import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.repository.ServiceGroupRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceGroupService {

    private final ServiceGroupRepository serviceGroupRepository;
    private final StopService stopService;

    public ServiceGroupService(ServiceGroupRepository serviceGroupRepository, StopService stopService) {
        this.serviceGroupRepository = serviceGroupRepository;
        this.stopService = stopService;
    }

    public List<BriefServiceResponse> getServicesByPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();

        return serviceGroupRepository.getServicesByPrefix(prefix)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<BriefServiceResponse> getServicesAtStop(String stopId) {
        return serviceGroupRepository.getServicesAtStop(stopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<BriefServiceResponse> getAdjacentServices(Double lat, Double lon) {
        Map<ServiceId, BriefServiceResponse> unique = new LinkedHashMap<>();

        for (BriefStopResponse stop : stopService.getAdjacentStops(lat, lon)) {
            for (ServiceGroup service : serviceGroupRepository.getServicesAtStop(stop.stopId())) {
                BriefServiceResponse response = toResponse(service);
                unique.putIfAbsent(response.routeGroup(), response);
            }
        }

        return new ArrayList<>(unique.values());
    }

    private BriefServiceResponse toResponse(ServiceGroup serviceGroup) {
        return new BriefServiceResponse(
                new ServiceId(
                        serviceGroup.getRouteShortName(),
                        serviceGroup.getTripHeadsign(),
                        serviceGroup.getDirectionId()
                ),
                serviceGroup.getRouteShortName(),
                serviceGroup.getRouteLongName(),
                null // TODO: arrivalsAtNearestStop
        );
    }
}
