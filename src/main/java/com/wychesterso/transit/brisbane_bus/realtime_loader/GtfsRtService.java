package com.wychesterso.transit.brisbane_bus.realtime_loader;

import com.wychesterso.transit.brisbane_bus.api.repository.GtfsRtRepository;
import com.wychesterso.transit.brisbane_bus.core.model.RtStopDelay;
import com.wychesterso.transit.brisbane_bus.api.service.dto.TripStopKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
public class GtfsRtService {

    private static final Logger log =
            LoggerFactory.getLogger(GtfsRtService.class);

    private final GtfsRtRepository repository;

    private volatile Map<TripStopKey, RtStopDelay> index = Map.of();

    public GtfsRtService(GtfsRtRepository repository) {
        this.repository = repository;
    }

    /**
     * Refresh cache from database.
     * Runs inside API service.
     */
    @Scheduled(fixedDelayString = "${gtfs.rt.index-refresh-ms:10000}")
    public void rebuild() {

        long start = System.currentTimeMillis();

        List<RtStopDelay> delays = repository.findAllDelays().stream().map(RtStopDelay::from).toList();

        Map<TripStopKey, RtStopDelay> next = new HashMap<>(delays.size());

        for (RtStopDelay d : delays) {
            next.put(new TripStopKey(d.tripId(), d.stopId()), d);
        }

        index = Map.copyOf(next);

        log.info("GTFS-RT index refreshed from DB: {} rows in {} ms",
                index.size(),
                System.currentTimeMillis() - start);
    }

    /**
     * Returns the latest realtime index.
     */
    public Map<TripStopKey, RtStopDelay> getIndex() {
        return index;
    }
}