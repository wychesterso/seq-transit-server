package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.StopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.api.dto.StopArrivalResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.StopArrivalResponseList;
import com.wychesterso.transit.brisbane_bus.rt.model.RtStopDelay;
import com.wychesterso.transit.brisbane_bus.rt.model.TripStopKey;
import com.wychesterso.transit.brisbane_bus.rt.service.GtfsRtIndexService;
import com.wychesterso.transit.brisbane_bus.st.repository.StopArrivalRepository;
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
public class StopArrivalService {

    private final StopArrivalRepository repository;
    private final GtfsRtIndexService rtIndex;
    private final RedisTemplate<String, Object> redis;

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public StopArrivalService(
            StopArrivalRepository repository,
            GtfsRtIndexService rtIndex,
            RedisTemplate<String, Object> redis) {
        this.repository = repository;
        this.rtIndex = rtIndex;
        this.redis = redis;
    }

    public List<StopArrivalResponse> getNextArrivalsForStop(String stopId) {
        ServiceClock clock = ServiceTimeHelper.now();
        int nowSeconds = clock.serviceSeconds();
        LocalDate serviceDate = clock.serviceDate();

        return mapDTOtoResponse(
                repository.findNextArrivalsForStop(stopId, nowSeconds),
                serviceDate
        );
    }

    public List<StopArrivalResponse> getNextArrivalsForRouteAtStop(String stopId, String routeId) {
        ServiceClock clock = ServiceTimeHelper.now();
        int nowSeconds = clock.serviceSeconds();
        LocalDate serviceDate = clock.serviceDate();

        String key = "stop:%s:route:%s:%s"
                .formatted(stopId, routeId, serviceDate);

        @SuppressWarnings("unchecked")
        StopArrivalResponseList cached = (StopArrivalResponseList) redis.opsForValue().get(key);

        if (cached != null) {
            System.out.println("Using cached Redis result");
            return cached.getArrivals();
        }

        List<StopArrivalResponse> result = mapDTOtoResponse(
                repository.findNextArrivalsForRouteAtStop(
                        stopId,
                        routeId,
                        serviceDateToInt(serviceDate),
                        nowSeconds),
                serviceDate
        );

        redis.opsForValue().set(
                key,
                new StopArrivalResponseList(result),
                Duration.ofSeconds(30) // TTL
        );

        System.out.println("stopId = " + stopId);
        System.out.println("routeId = " + routeId);
        System.out.println("serviceDate = " + serviceDate);
        System.out.println("nowSeconds = " + nowSeconds);
        return result;
    }

    private List<StopArrivalResponse> mapDTOtoResponse(List<StopArrivalDTO> arrivals, LocalDate serviceDate) {

        Map<TripStopKey, RtStopDelay> rt = rtIndex.getIndex();

        return arrivals.stream()
                .map(r -> {
                    LocalDateTime scheduledArr = serviceDate.atStartOfDay().plusSeconds(r.getArrivalTimeSeconds());
                    LocalDateTime scheduledDep = serviceDate.atStartOfDay().plusSeconds(r.getDepartureTimeSeconds());

                    RtStopDelay delayInfo = rt.get(new TripStopKey(r.getTripId(), r.getStopId()));

                    boolean hasRt = delayInfo != null;

                    boolean cancelled = hasRt && delayInfo.cancelled();
                    boolean skipped = hasRt && delayInfo.skipped();

                    Integer arrDelay = hasRt ? delayInfo.arrivalDelaySeconds() : null;
                    Integer depDelay = hasRt ? delayInfo.departureDelaySeconds() : null;

                    int effectiveArrSeconds = r.getArrivalTimeSeconds() + (arrDelay != null ? arrDelay : 0);
                    int effectiveDepSeconds = r.getDepartureTimeSeconds() + (depDelay != null ? depDelay : 0);

                    LocalDateTime effectiveArr = serviceDate.atStartOfDay().plusSeconds(effectiveArrSeconds);
                    LocalDateTime effectiveDep = serviceDate.atStartOfDay().plusSeconds(effectiveDepSeconds);

                    return new StopArrivalResponse(
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
                .toList();
    }

    private int serviceDateToInt(LocalDate serviceDate) {
        return serviceDate.getYear() * 10000
                + serviceDate.getMonthValue() * 100
                + serviceDate.getDayOfMonth();
    }
}