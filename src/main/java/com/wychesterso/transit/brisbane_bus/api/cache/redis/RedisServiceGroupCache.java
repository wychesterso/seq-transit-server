package com.wychesterso.transit.brisbane_bus.api.cache.redis;

import com.wychesterso.transit.brisbane_bus.api.cache.ServiceGroupCache;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupAtStopList;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupDTO;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupList;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupAtStopDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "spring.redis.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RedisServiceGroupCache implements ServiceGroupCache {

    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redis;

    public RedisServiceGroupCache(RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    public ServiceGroupDTO getServiceInfo(
            String routeShortName, String tripHeadsign, Integer directionId
    ) {
        String key = getServiceInfoKey(routeShortName, tripHeadsign, directionId);

        @SuppressWarnings("unchecked")
        ServiceGroupDTO cached = (ServiceGroupDTO) redis.opsForValue().get(key);
        return cached;
    }

    public void cacheServiceInfo(
            String routeShortName, String tripHeadsign, Integer directionId,
            ServiceGroupDTO serviceInfo
    ) {
        String key = getServiceInfoKey(routeShortName, tripHeadsign, directionId);

        redis.opsForValue().set(
                key,
                serviceInfo,
                TTL);
    }

    private String getServiceInfoKey(
            String routeShortName, String tripHeadsign, Integer directionId
    ) {
        return "service:%s:%s:%d:info".formatted(routeShortName, tripHeadsign, directionId);
    }

    public List<ServiceGroupDTO> getServicesByPrefix(String prefix) {
        String key = getServicesByPrefixKey(prefix);

        @SuppressWarnings("unchecked")
        ServiceGroupList cached = (ServiceGroupList) redis.opsForValue().get(key);
        if (cached != null) return cached.serviceGroupList();
        return null;
    }

    public void cacheServicesByPrefix(String prefix, List<ServiceGroupDTO> result) {
        String key = getServicesByPrefixKey(prefix);

        redis.opsForValue().set(
                key,
                new ServiceGroupList(result),
                TTL);
    }

    private String getServicesByPrefixKey(String prefix) {
        return "services:prefix:" + prefix.toLowerCase();
    }

    public List<ServiceGroupAtStopDTO> getServicesAtStop(String stopId) {
        String key = getServicesAtStopKey(stopId);

        @SuppressWarnings("unchecked")
        ServiceGroupAtStopList cached = (ServiceGroupAtStopList) redis.opsForValue().get(key);

        if (cached != null) return cached.serviceGroupAtStopList();
        return null;
    }

    public void cacheServicesAtStop(String stopId, List<ServiceGroupAtStopDTO> result) {
        String key = getServicesAtStopKey(stopId);

        redis.opsForValue().set(
                key,
                new ServiceGroupAtStopList(result),
                TTL);
    }

    private String getServicesAtStopKey(String stopId) {
        return "stop:" + stopId + ":services";
    }

    public List<ServiceGroupAtStopDTO> getPickupServicesAtStop(String stopId) {
        String key = getPickupServicesAtStopKey(stopId);

        @SuppressWarnings("unchecked")
        ServiceGroupAtStopList cached = (ServiceGroupAtStopList) redis.opsForValue().get(key);

        if (cached != null) return cached.serviceGroupAtStopList();
        return null;
    }

    public void cachePickupServicesAtStop(String stopId, List<ServiceGroupAtStopDTO> result) {
        String key = getPickupServicesAtStopKey(stopId);

        redis.opsForValue().set(
                key,
                new ServiceGroupAtStopList(result),
                TTL);
    }

    private String getPickupServicesAtStopKey(String stopId) {
        return "stop:" + stopId + ":services-pickup";
    }
}
