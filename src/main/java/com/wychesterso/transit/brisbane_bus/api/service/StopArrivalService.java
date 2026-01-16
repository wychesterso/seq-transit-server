package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.StopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.api.dto.StopArrivalResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.StopArrivalResponseList;
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

@Service
public class StopArrivalService {

    private final StopArrivalRepository repository;
    private final RedisTemplate<String, Object> redis;

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public StopArrivalService(
            StopArrivalRepository repository,
            RedisTemplate<String, Object> redis) {
        this.repository = repository;
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

        return arrivals.stream()
                .map(r -> {
                    LocalDateTime arrival =
                            serviceDate.atStartOfDay().plusSeconds(r.getArrivalTimeSeconds());

                    LocalDateTime departure =
                            serviceDate.atStartOfDay().plusSeconds(r.getDepartureTimeSeconds());

                    return new StopArrivalResponse(
                            r.getTripId(),
                            r.getArrivalTimeSeconds(),
                            arrival.format(TIME_FMT),
                            r.getDepartureTimeSeconds(),
                            departure.format(TIME_FMT)
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