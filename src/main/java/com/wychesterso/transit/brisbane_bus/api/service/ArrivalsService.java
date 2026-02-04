package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.ArrivalsCache;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.st.model.StopArrival;
import com.wychesterso.transit.brisbane_bus.api.controller.dto.ArrivalResponse;
import com.wychesterso.transit.brisbane_bus.rt.model.RtStopDelay;
import com.wychesterso.transit.brisbane_bus.rt.model.TripStopKey;
import com.wychesterso.transit.brisbane_bus.rt.service.GtfsRtIndexService;
import com.wychesterso.transit.brisbane_bus.api.repository.ArrivalsRepository;
import com.wychesterso.transit.brisbane_bus.api.service.time.ServiceClock;
import com.wychesterso.transit.brisbane_bus.api.service.time.ServiceTimeHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArrivalsService {

    private final ArrivalsRepository repository;
    private final ArrivalsCache cache;

    private final StopService stopService;
    private final StopSequenceService stopSequenceService;

    private final GtfsRtIndexService rtIndex;

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ArrivalsService(
            ArrivalsRepository repository,
            ArrivalsCache cache,
            StopService stopService,
            StopSequenceService stopSequenceService,
            GtfsRtIndexService rtIndex
    ) {
        this.repository = repository;
        this.cache = cache;
        this.stopService = stopService;
        this.stopSequenceService = stopSequenceService;
        this.rtIndex = rtIndex;
    }

    public ArrivalsAtStopResponse getNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId
    ) {
        ServiceClock clock = ServiceTimeHelper.now();
        int nowSeconds = clock.serviceSeconds();
        LocalDate serviceDate = clock.serviceDate();

        @SuppressWarnings("unchecked")
        ArrivalsAtStopResponse response = cache.getNextArrivalsForServiceAtStop(
                stopId, routeShortName, tripHeadsign, directionId, serviceDate);
        if (response != null) return response;

        response = toArrivalsAtStopResponse(
                stopId,
                repository.findNextArrivalsForServiceAtStop(
                        stopId,
                        routeShortName,
                        tripHeadsign,
                        directionId,
                        serviceDateToInt(serviceDate),
                        nowSeconds),
                stopSequenceService.getCanonicalStopSequence(
                        routeShortName,
                        tripHeadsign,
                        directionId),
                serviceDate
        );
        cache.cacheNextArrivalsForServiceAtStop(
                stopId, routeShortName, tripHeadsign, directionId, serviceDate, response);

        return response;
    }

    private ArrivalsAtStopResponse toArrivalsAtStopResponse(
            String stopId,
            List<StopArrival> arrivals,
            Map<String, Integer> canonicalStopList,
            LocalDate serviceDate) {

        Map<TripStopKey, RtStopDelay> rt = rtIndex.getIndex();
        List<String> stopsInOrder = new ArrayList<>(canonicalStopList.keySet());

        return new ArrivalsAtStopResponse(
                stopService.getStop(stopId),
                canonicalStopList.get(stopId),
                stopsInOrder.get(0).equals(stopId),
                stopsInOrder.get(stopsInOrder.size() - 1).equals(stopId),
                arrivals
                        .stream()
                        .map(r -> {
                            LocalDateTime scheduledArr = serviceDate.atStartOfDay().plusSeconds(r.getArrivalTimeSeconds());
                            LocalDateTime scheduledDep = serviceDate.atStartOfDay().plusSeconds(r.getDepartureTimeSeconds());

                            RtStopDelay delayInfo = rt.get(new TripStopKey(r.getTripId(), r.getStopId()));

                            boolean hasRt = delayInfo != null;

                            boolean cancelled = hasRt && delayInfo.cancelled();
                            boolean skipped = hasRt && delayInfo.skipped();

                            int scheduledArrSeconds = r.getArrivalTimeSeconds();
                            int scheduledDepSeconds = r.getDepartureTimeSeconds();

                            Integer rtArrSeconds = hasRt ? delayInfo.effectiveArrivalSeconds() : null;
                            Integer rtDepSeconds = hasRt ? delayInfo.effectiveDepartureSeconds() : null;

                            int effectiveArrSeconds = rtArrSeconds != null
                                    ? rtArrSeconds
                                    : scheduledArrSeconds;
                            int effectiveDepSeconds = rtDepSeconds != null
                                    ? rtDepSeconds
                                    : scheduledDepSeconds;

                            Integer arrDelay = rtArrSeconds != null
                                    ? effectiveArrSeconds - scheduledArrSeconds
                                    : null;
                            Integer depDelay = rtDepSeconds != null
                                    ? effectiveDepSeconds - scheduledDepSeconds
                                    : null;

                            LocalDateTime effectiveArr = serviceDate.atStartOfDay().plusSeconds(effectiveArrSeconds);
                            LocalDateTime effectiveDep = serviceDate.atStartOfDay().plusSeconds(effectiveDepSeconds);

                            return new ArrivalResponse(
                                    r.getTripId(),
                                    r.getArrivalTimeSeconds(),
                                    scheduledArr.format(TIME_FMT),
                                    effectiveArrSeconds,
                                    effectiveArr.format(TIME_FMT),
                                    arrDelay,
                                    r.getDepartureTimeSeconds(),
                                    scheduledDep.format(TIME_FMT),
                                    effectiveDepSeconds,
                                    effectiveDep.format(TIME_FMT),
                                    depDelay,
                                    hasRt,
                                    cancelled,
                                    skipped
                            );
                        })
                        .toList()
        );
    }

    private int serviceDateToInt(LocalDate serviceDate) {
        return serviceDate.getYear() * 10000
                + serviceDate.getMonthValue() * 100
                + serviceDate.getDayOfMonth();
    }
}