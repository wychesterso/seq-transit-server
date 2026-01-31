package com.wychesterso.transit.brisbane_bus.api.controller;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.FullServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.service.AdjacentService;
import com.wychesterso.transit.brisbane_bus.api.service.ServiceGroupFullService;
import com.wychesterso.transit.brisbane_bus.api.service.ServiceGroupWithArrivalsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceGroupWithArrivalsService serviceGroupWithArrivalsService;
    private final ServiceGroupFullService serviceGroupFullService;
    private final AdjacentService adjacentService;

    public ServiceController(
            ServiceGroupWithArrivalsService serviceGroupWithArrivalsService,
            ServiceGroupFullService serviceGroupFullService,
            AdjacentService adjacentService) {
        this.serviceGroupWithArrivalsService = serviceGroupWithArrivalsService;
        this.serviceGroupFullService = serviceGroupFullService;
        this.adjacentService = adjacentService;
    }

    @GetMapping("/prefix")
    public List<BriefServiceResponse> getServicesByPrefix(
            @RequestParam(required = true) String prefix,
            @RequestParam(required = true) Double lat,
            @RequestParam(required = true) Double lon) {
        return serviceGroupWithArrivalsService.getServicesByPrefix(prefix, lat, lon);
    }

    @GetMapping("/stop")
    public List<BriefServiceResponse> getServicesAtStop(
            @RequestParam(required = true) String id) {
        return serviceGroupWithArrivalsService.getServicesAtStop(id);
    }

    @GetMapping("/nearest")
    public List<BriefServiceResponse> getNearestServices(
            @RequestParam(required = true) Double lat,
            @RequestParam(required = true) Double lon) {
        return adjacentService.getAdjacentServices(lat, lon);
    }

    @GetMapping("/info")
    public FullServiceResponse getFullServiceInfo(
            @RequestParam(required = true) String route,
            @RequestParam(required = true) String headsign,
            @RequestParam(required = true) Integer dir) {
        return serviceGroupFullService.getFullServiceGroupInfo(route, headsign, dir);
    }
}
