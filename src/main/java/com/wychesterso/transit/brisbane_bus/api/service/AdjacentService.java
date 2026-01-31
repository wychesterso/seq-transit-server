package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.loader.RouteLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdjacentService {

    private final StopService stopService;
    private final ServiceGroupWithArrivalsService serviceGroupWithArrivalsService;

    private final RedisTemplate<String, Object> redis;
    private static final Logger log = LoggerFactory.getLogger(RouteLoader.class);

    public AdjacentService(
            StopService stopService,
            ServiceGroupWithArrivalsService serviceGroupWithArrivalsService,
            RedisTemplate<String, Object> redis) {
        this.stopService = stopService;
        this.serviceGroupWithArrivalsService = serviceGroupWithArrivalsService;
        this.redis = redis;
    }

    public List<BriefServiceResponse> getAdjacentServices(Double lat, Double lon) {

        long start = System.currentTimeMillis();
        log.info("Starting getAdjacentStops...");
        List<String> stopIds = stopService.getAdjacentStops(lat, lon)
                .stream()
                .map(BriefStopResponse::stopId)
                .toList();
        log.info("getAdjacentStops returned {} stops in {} ms",
                stopIds.size(), System.currentTimeMillis() - start);

        Map<ServiceGroup, BriefServiceResponse> unique = new LinkedHashMap<>();

        start = System.currentTimeMillis();
        log.info("Starting getServicesAtStops...");
        List<BriefServiceResponse> services = serviceGroupWithArrivalsService.getServicesAtStops(stopIds, lat, lon);
        log.info("getServicesAtStops returned {} services for {} stops in {} ms",
                services.size(), stopIds.size(), System.currentTimeMillis() - start);

        for (BriefServiceResponse service : services) {
            unique.putIfAbsent(service.routeGroup(), service);
        }

        return new ArrayList<>(unique.values());
    }
}
