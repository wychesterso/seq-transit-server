package com.wychesterso.transit.seq_transit.api.controller;

import com.wychesterso.transit.seq_transit.api.controller.dto.model.AdjacentRadius;
import com.wychesterso.transit.seq_transit.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.seq_transit.api.service.StopService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stops")
public class StopController {

    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    /**
     * Retrieves a list of stops within a certain range of a query coordinate. <br>
     * The results are sorted based on the stop's proximity to the query coordinates.
     * @param lat the query latitude
     * @param lon the query longitude
     * @param radius the search radius
     * @return a list of stop responses
     */
    @GetMapping("/nearest")
    public List<BriefStopResponse> getNearestStops(
            @RequestParam(required = true) Double lat,
            @RequestParam(required = true) Double lon,
            @RequestParam(required = true) AdjacentRadius radius) {
        return stopService.getAdjacentStops(lat, lon, radius);
    }
}
