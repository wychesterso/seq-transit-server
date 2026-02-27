package com.wychesterso.transit.seq_transit.api.cache;

import com.wychesterso.transit.seq_transit.api.cache.dto.ServiceGroupAtStopDTO;
import com.wychesterso.transit.seq_transit.api.cache.dto.ServiceGroupDTO;
import java.util.List;

public interface ServiceGroupCache {

    ServiceGroupDTO getServiceInfo(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    );

    void cacheServiceInfo(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            ServiceGroupDTO serviceInfo
    );

    List<ServiceGroupDTO> getServicesByPrefix(String prefix);

    void cacheServicesByPrefix(String prefix, List<ServiceGroupDTO> result);

    List<ServiceGroupAtStopDTO> getServicesAtStop(String stopId);

    void cacheServicesAtStop(String stopId, List<ServiceGroupAtStopDTO> result);

    List<ServiceGroupAtStopDTO> getPickupServicesAtStop(String stopId);

    void cachePickupServicesAtStop(String stopId, List<ServiceGroupAtStopDTO> result);
}

