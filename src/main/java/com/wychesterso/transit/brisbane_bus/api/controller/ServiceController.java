package com.wychesterso.transit.brisbane_bus.api.controller;

import com.wychesterso.transit.brisbane_bus.api.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.service.ServiceGroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceGroupService serviceGroupService;

    public ServiceController(
            ServiceGroupService serviceGroupService) {
        this.serviceGroupService = serviceGroupService;
    }

    @GetMapping("")
    public List<BriefServiceResponse> getServicesByPrefix(
            @RequestParam(required = true) String prefix) {
        return serviceGroupService.getServicesByPrefix(prefix);
    }

    @GetMapping("/nearest")
    public List<BriefServiceResponse> getNearestServices(
            @RequestParam(required = true) Double lat,
            @RequestParam(required = true) Double lon) {
        return serviceGroupService.getAdjacentServices(lat, lon);
    }
}
