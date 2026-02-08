package com.wychesterso.transit.brisbane_bus.api.controller;

import com.wychesterso.transit.brisbane_bus.api.controller.dto.AdjacentRadius;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ServiceResponse;
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

    /**
     * Retrieves a list of service groups, filtered by a prefix search term. <br>
     * Each service group response contains: <br>
     * - Route info <br>
     * - The nearest stop that this service stops at (if in range) <br>
     * - This service's next 3 arrivals at the nearest stop (if in range) <br>
     * The results are sorted based on the nearest stop's proximity to the query coordinates. <br>
     * Each service group can only appear once in the result.
     * @param prefix the prefix search term
     * @return a list of service group responses
     */
    @GetMapping("/prefix")
    public List<BriefServiceResponse> getServicesByPrefix(
            @RequestParam(required = true) String prefix) {
        return serviceGroupWithArrivalsService.getServicesByPrefix(prefix);
    }

    /**
     * Retrieves a list of service groups that stop at a particular stop. <br>
     * Each service group response contains: <br>
     * - Route info <br>
     * - This service's next 3 arrivals at the queried stop <br>
     * Each service group can only appear once in the result.
     * @param id the stop id to query
     * @return a list of service group responses
     */
    @GetMapping("/stop")
    public List<ServiceResponse> getServicesAtStop(
            @RequestParam(required = true) String id) {
        return serviceGroupWithArrivalsService.getServicesAtStop(id);
    }

    /**
     * Retrieves a list of service groups with a stop within a certain range of a query coordinate. <br>
     * Each service group response contains: <br>
     * - Route info <br>
     * - The nearest stop that this service stops at <br>
     * - This service's next 3 arrivals at the nearest stop <br>
     * The results are sorted based on the nearest stop's proximity to the query coordinates. <br>
     * Each service group can only appear once in the result.
     * @param lat the query latitude
     * @param lon the query longitude
     * @param radius the search radius
     * @return a list of service group responses
     */
    @GetMapping("/nearest")
    public List<ServiceResponse> getNearestServices(
            @RequestParam(required = true) Double lat,
            @RequestParam(required = true) Double lon,
            @RequestParam(required = true) AdjacentRadius radius) {
        return adjacentService.getAdjacentServices(lat, lon, radius);
    }

    /**
     * Retrieves service group info, stops and next 3 arrivals at each stop of this service. <br>
     * The arrival results are sorted in stop sequence order.
     * @param route the route number or short name
     * @param headsign the service's headsign
     * @param dir the service's direction id
     * @return a full service response containing service info, stop sequence and arrivals
     */
    @GetMapping("/info")
    public FullServiceResponse getFullServiceInfo(
            @RequestParam(required = true) String route,
            @RequestParam(required = true) String headsign,
            @RequestParam(required = true) Integer dir,
            @RequestParam(required = true) Double lat,
            @RequestParam(required = true) Double lon) {
        return serviceGroupFullService.getFullServiceGroupInfo(route, headsign, dir, lat, lon);
    }
}
