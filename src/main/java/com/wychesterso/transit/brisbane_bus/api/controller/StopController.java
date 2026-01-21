package com.wychesterso.transit.brisbane_bus.api.controller;

import com.wychesterso.transit.brisbane_bus.api.dto.StopResponse;
import com.wychesterso.transit.brisbane_bus.api.service.StopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stops")
public class StopController {

    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @GetMapping("/{stopId}")
    public StopResponse getStop(
            @PathVariable String stopId) {
        return stopService.getStop(stopId);
    }
}
