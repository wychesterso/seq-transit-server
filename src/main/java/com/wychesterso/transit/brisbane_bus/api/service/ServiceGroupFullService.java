package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupDTO;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.FullServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ServiceGroup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServiceGroupFullService {

    private final StopSequenceService stopSequenceService;
    private final ServiceGroupService serviceGroupService;
    private final ArrivalsService arrivalsService;
    private final StopService stopService;

    public ServiceGroupFullService(
            StopSequenceService stopSequenceService,
            ServiceGroupService serviceGroupService,
            ArrivalsService arrivalsService,
            StopService stopService
    ) {
        this.stopSequenceService = stopSequenceService;
        this.serviceGroupService = serviceGroupService;
        this.arrivalsService = arrivalsService;
        this.stopService = stopService;
    }

    public FullServiceResponse getFullServiceGroupInfo(
            String routeShortName,
            String tripHeadsign,
            Integer directionId,
            Double lat,
            Double lon
    ) {
        if (lat == null || lon == null ||
                routeShortName == null || tripHeadsign == null || directionId == null) return null;

        // get nearest stop
        BriefStopResponse adjacentStop = stopService.getAdjacentStopForServiceGroup(
                routeShortName, tripHeadsign, directionId,
                lat, lon
        );

        List<ArrivalsAtStopResponse> arrivalsAtStopSequence = new ArrayList<>();

        // iterate in stop sequence order
        for (String stopId :
                stopSequenceService.getCanonicalStopSequence(routeShortName, tripHeadsign, directionId).keySet()
        ) {
            arrivalsAtStopSequence.add(
                    arrivalsService.getNextArrivalsForServiceAtStop(
                            stopId,
                            routeShortName, tripHeadsign, directionId));
        }

        return toResponse(
                serviceGroupService.getServiceInfo(routeShortName, tripHeadsign, directionId),
                adjacentStop,
                arrivalsAtStopSequence);
    }

    private FullServiceResponse toResponse(
            ServiceGroupDTO sg,
            BriefStopResponse adjacentStop,
            List<ArrivalsAtStopResponse> arrivalsAtStopSequence
    ) {
        if (sg == null) return null;
        return new FullServiceResponse(
                new ServiceGroup(
                        sg.routeShortName(),
                        sg.tripHeadsign(),
                        sg.directionId()
                ),
                sg.routeShortName(),
                sg.routeLongName(),
                sg.routeType(),
                sg.routeColor(),
                sg.routeTextColor(),
                adjacentStop,
                arrivalsAtStopSequence
        );
    }
}
