package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.st.model.StopArrival;
import com.wychesterso.transit.brisbane_bus.api.dto.ArrivalResponse;
import com.wychesterso.transit.brisbane_bus.rt.model.RtStopDelay;
import com.wychesterso.transit.brisbane_bus.rt.model.TripStopKey;
import com.wychesterso.transit.brisbane_bus.rt.service.GtfsRtIndexService;
import com.wychesterso.transit.brisbane_bus.st.repository.ArrivalsRepository;
import com.wychesterso.transit.brisbane_bus.api.service.time.ServiceClock;
import com.wychesterso.transit.brisbane_bus.api.service.time.ServiceTimeHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ArrivalsService {

    private final ArrivalsRepository repository;

    private final StopService stopService;
    private final StopSequenceService stopSequenceService;

    private final GtfsRtIndexService rtIndex;
    private final RedisTemplate<String, Object> redis;

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ArrivalsService(
            ArrivalsRepository repository,
            StopService stopService,
            StopSequenceService stopSequenceService,
            GtfsRtIndexService rtIndex,
            RedisTemplate<String, Object> redis) {
        this.repository = repository;
        this.stopService = stopService;
        this.stopSequenceService = stopSequenceService;
        this.rtIndex = rtIndex;
        this.redis = redis;
    }

    public ArrivalsAtStopResponse getNextArrivalsForServiceAtStop(
            String stopId,
            String routeShortName,
            String tripHeadsign,
            Integer directionId) {
        ServiceClock clock = ServiceTimeHelper.now();
        int nowSeconds = clock.serviceSeconds();
        LocalDate serviceDate = clock.serviceDate();

        String key = "stop:arrivals-for-service:%s:%s:%s:%d:%s"
                .formatted(stopId, routeShortName, tripHeadsign, directionId, serviceDate);

        @SuppressWarnings("unchecked")
        ArrivalsAtStopResponse cached = (ArrivalsAtStopResponse) redis.opsForValue().get(key);

        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            return cached;
        }

        ArrivalsAtStopResponse result = mapDTOtoResponse(
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

        redis.opsForValue().set(
                key,
                result,
                Duration.ofSeconds(30) // TTL
        );

        return result;
    }

    private ArrivalsAtStopResponse mapDTOtoResponse(
            String stopId,
            List<StopArrival> arrivals,
            Map<String, Integer> canonicalStopList,
            LocalDate serviceDate) {

        Map<TripStopKey, RtStopDelay> rt = rtIndex.getIndex();

        return new ArrivalsAtStopResponse(
                stopService.getStopInfo(stopId),
                canonicalStopList.get(stopId),
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