package com.wychesterso.transit.seq_transit.api.service;

import com.wychesterso.transit.seq_transit.api.cache.ArrivalsCache;
import com.wychesterso.transit.seq_transit.api.controller.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.seq_transit.api.repository.dto.StopArrival;
import com.wychesterso.transit.seq_transit.api.controller.dto.ArrivalResponse;
import com.wychesterso.transit.seq_transit.api.service.dto.IntermediateArrival;
import com.wychesterso.transit.seq_transit.core.model.RtStopDelay;
import com.wychesterso.transit.seq_transit.api.service.dto.TripStopKey;
import com.wychesterso.transit.seq_transit.api.repository.ArrivalsRepository;
import com.wychesterso.transit.seq_transit.api.service.time.ServiceClock;
import com.wychesterso.transit.seq_transit.api.service.time.ServiceTimeHelper;
import com.wychesterso.transit.seq_transit.rt_loader.cache.RtIndexCache;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ArrivalsService {

    private final ArrivalsRepository repository;
    private final ArrivalsCache cache;

    private final StopService stopService;
    private final StopSequenceService stopSequenceService;

    private final RtIndexCache rtIndexCache;

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ArrivalsService(
            ArrivalsRepository repository,
            ArrivalsCache cache,
            StopService stopService,
            StopSequenceService stopSequenceService,
            RtIndexCache rtIndexCache
    ) {
        this.repository = repository;
        this.cache = cache;
        this.stopService = stopService;
        this.stopSequenceService = stopSequenceService;
        this.rtIndexCache = rtIndexCache;
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
                serviceDate,
                nowSeconds
        );
        cache.cacheNextArrivalsForServiceAtStop(
                stopId, routeShortName, tripHeadsign, directionId, serviceDate, response);

        return response;
    }

    private ArrivalsAtStopResponse toArrivalsAtStopResponse(
            String stopId,
            List<StopArrival> arrivals,
            Map<String, Integer> canonicalStopList,
            LocalDate serviceDate,
            int nowSeconds) {

        Map<TripStopKey, RtStopDelay> rt = rtIndexCache.getIndex();
        List<String> stopsInOrder = new ArrayList<>(canonicalStopList.keySet());

        return new ArrivalsAtStopResponse(
                stopService.getStop(stopId),
                canonicalStopList.get(stopId),
                stopsInOrder.get(0).equals(stopId),
                stopsInOrder.get(stopsInOrder.size() - 1).equals(stopId),
                arrivals
                        .stream()
                        .map(r -> {
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

                            return new IntermediateArrival(
                                    r,
                                    scheduledArrSeconds,
                                    effectiveArrSeconds,
                                    scheduledDepSeconds,
                                    effectiveDepSeconds,
                                    hasRt,
                                    cancelled,
                                    skipped
                            );
                        })
                        // remove cancelled/skipped
                        .filter(a -> !a.cancelled() && !a.skipped())
                        // remove arrivals in the past
                        .filter(a -> a.effectiveArrSeconds() >= nowSeconds)
                        // sort by effective arrival time
                        .sorted(Comparator.comparingInt(a -> a.effectiveArrSeconds()))
                        // take 3
                        .limit(3)
                        // convert to response
                        .map(a -> {
                            StopArrival r = a.stopArrival();

                            LocalDateTime scheduledArr =
                                    serviceDate.atStartOfDay().plusSeconds(a.scheduledArrSeconds());
                            LocalDateTime scheduledDep =
                                    serviceDate.atStartOfDay().plusSeconds(a.scheduledDepSeconds());

                            LocalDateTime effectiveArr =
                                    serviceDate.atStartOfDay().plusSeconds(a.effectiveArrSeconds());
                            LocalDateTime effectiveDep =
                                    serviceDate.atStartOfDay().plusSeconds(a.effectiveDepSeconds());

                            Integer arrDelay =
                                    a.effectiveArrSeconds() - a.scheduledArrSeconds();
                            Integer depDelay =
                                    a.effectiveDepSeconds() - a.scheduledDepSeconds();

                            return new ArrivalResponse(
                                    r.getTripId(),
                                    a.scheduledArrSeconds(),
                                    scheduledArr.format(TIME_FMT),
                                    a.effectiveArrSeconds(),
                                    effectiveArr.format(TIME_FMT),
                                    arrDelay,
                                    a.scheduledDepSeconds(),
                                    scheduledDep.format(TIME_FMT),
                                    a.effectiveDepSeconds(),
                                    effectiveDep.format(TIME_FMT),
                                    depDelay,
                                    a.hasRt(),
                                    false,
                                    false
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