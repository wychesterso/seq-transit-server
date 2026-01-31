package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupDTO;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;
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

    public ServiceGroupFullService(
            StopSequenceService stopSequenceService,
            ServiceGroupService serviceGroupService,
            ArrivalsService arrivalsService
    ) {
        this.stopSequenceService = stopSequenceService;
        this.serviceGroupService = serviceGroupService;
        this.arrivalsService = arrivalsService;
    }

    public FullServiceResponse getFullServiceGroupInfo(
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        if (routeShortName == null || tripHeadsign == null || directionId == null) return null;

        List<ArrivalsAtStopResponse> arrivalsAtStopSequence = new ArrayList<>();

        // iterate in stop sequence order
        for (String stopId :
                stopSequenceService.getCanonicalStopSequence(routeShortName, tripHeadsign, directionId)
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .toList()
        ) {
            arrivalsAtStopSequence.add(
                    arrivalsService.getNextArrivalsForServiceAtStop(
                            stopId,
                            routeShortName, tripHeadsign, directionId));
        }

        return toResponse(
                serviceGroupService.getServiceInfo(routeShortName, tripHeadsign, directionId),
                arrivalsAtStopSequence);
    }

    private FullServiceResponse toResponse(
            ServiceGroupDTO sg,
            List<ArrivalsAtStopResponse> arrivalsAtStopSequence
    ) {
        return new FullServiceResponse(
                new ServiceGroup(
                        sg.routeShortName(),
                        sg.tripHeadsign(),
                        sg.directionId()
                ),
                sg.routeShortName(),
                sg.routeLongName(),
                sg.routeColor(),
                sg.routeTextColor(),
                arrivalsAtStopSequence
        );
    }
}
