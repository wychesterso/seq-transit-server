package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.ServiceGroupCache;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupAtStopDTO;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupDTO;
import com.wychesterso.transit.brisbane_bus.api.repository.ServiceGroupRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServiceGroupService {

    private final ServiceGroupRepository repository;
    private final ServiceGroupCache cache;

    public ServiceGroupService(
            ServiceGroupRepository repository,
            ServiceGroupCache cache
    ) {
        this.repository = repository;
        this.cache = cache;
    }

    public ServiceGroupDTO getServiceInfo(
            String routeShortName, String tripHeadsign, Integer directionId
    ) {
        if (routeShortName == null || tripHeadsign == null || directionId == null) return null;

        ServiceGroupDTO result = cache.getServiceInfo(routeShortName, tripHeadsign, directionId);
        if (result == null) {
            result = ServiceGroupDTO.from(
                    repository.getServiceInfo(routeShortName, tripHeadsign, directionId));
            cache.cacheServiceInfo(routeShortName, tripHeadsign, directionId, result);
        }

        return result;
    }

    /**
     * Get a list of services, filtered by prefix
     * @param prefix the prefix search term
     * @return list of services
     */
    public List<ServiceGroupDTO> getServicesByPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();

        List<ServiceGroupDTO> result = cache.getServicesByPrefix(prefix);
        if (result == null) {
            result = repository.getServicesByPrefix(prefix)
                    .stream()
                    .map(ServiceGroupDTO::from)
                    .toList();
            cache.cacheServicesByPrefix(prefix, result);
        }

        return result;
    }

    /**
     * Get a list of services at a particular stop
     * @param stopId the stop to query
     * @return list of services
     */
    public List<ServiceGroupAtStopDTO> getServicesAtStop(String stopId) {
        if (stopId == null || stopId.isBlank()) return List.of();

        List<ServiceGroupAtStopDTO> result = cache.getServicesAtStop(stopId);

        if (result == null) {
            result = repository.getServicesAtStop(stopId)
                    .stream()
                    .map(ServiceGroupAtStopDTO::from)
                    .toList();
            cache.cacheServicesAtStop(stopId, result);
        }

        return result;
    }
}
