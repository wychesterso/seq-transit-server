package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequenceAndShape;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ServiceGroupDTO;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.FullServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.model.CoordinatePoint;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.model.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.ShapePoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        CanonicalStopSequenceAndShape sequenceAndShape =
                stopSequenceService.getCanonicalStopSequenceAndShape(routeShortName, tripHeadsign, directionId);

        // iterate in stop sequence order
        for (String stopId : sequenceAndShape.stopIdToSequenceMap().keySet()) {
            arrivalsAtStopSequence.add(
                    arrivalsService.getNextArrivalsForServiceAtStop(
                            stopId,
                            routeShortName, tripHeadsign, directionId));
        }

        return toResponse(
                serviceGroupService.getServiceInfo(routeShortName, tripHeadsign, directionId),
                sequenceAndShape.shape(),
                adjacentStop,
                arrivalsAtStopSequence);
    }

    private FullServiceResponse toResponse(
            ServiceGroupDTO sg,
            List<ShapePoint> shapePoints,
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
                shapePointsToShape(shapePoints),
                adjacentStop,
                arrivalsAtStopSequence
        );
    }

    private List<CoordinatePoint> shapePointsToShape(List<ShapePoint> shapePoints) {
        return shapePoints.stream()
                .sorted(Comparator.comparingInt(ShapePoint::getShapePtSequence))
                .map(s -> new CoordinatePoint(s.getShapePtLat(), s.getShapePtLon())).toList();
    }
}
