package com.wychesterso.transit.brisbane_bus.api.cache.no_op;

import com.wychesterso.transit.brisbane_bus.api.cache.ServiceGroupCache;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupAtStopDTO;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "spring.redis.enabled",
        havingValue = "false"
)
public class NoOpServiceCache implements ServiceGroupCache {

    public ServiceGroupDTO getServiceInfo(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        return null;
    }

    public void cacheServiceInfo(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            ServiceGroupDTO serviceInfo
    ) {}

    public List<ServiceGroupDTO> getServicesByPrefix(String prefix) {
        return null;
    }

    public void cacheServicesByPrefix(String prefix, List<ServiceGroupDTO> result) {}

    public List<ServiceGroupAtStopDTO> getServicesAtStop(String stopId) {
        return null;
    }

    public void cacheServicesAtStop(String stopId, List<ServiceGroupAtStopDTO> result) {}

    public List<ServiceGroupAtStopDTO> getPickupServicesAtStop(String stopId) {
        return null;
    }

    public void cachePickupServicesAtStop(String stopId, List<ServiceGroupAtStopDTO> result) {}
}
