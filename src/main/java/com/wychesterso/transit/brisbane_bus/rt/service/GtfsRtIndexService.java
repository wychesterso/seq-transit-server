package com.wychesterso.transit.brisbane_bus.rt.service;

import com.wychesterso.transit.brisbane_bus.rt.GtfsRtSnapshot;
import com.wychesterso.transit.brisbane_bus.rt.TripUpdateExtractor;
import com.wychesterso.transit.brisbane_bus.rt.model.RtStopDelay;
import com.wychesterso.transit.brisbane_bus.rt.model.TripStopKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GtfsRtIndexService {

    private static final Logger log =
            LoggerFactory.getLogger(GtfsRtIndexService.class);

    private final GtfsRtSnapshot snapshot;
    private final TripUpdateExtractor extractor;

    private volatile Map<TripStopKey, RtStopDelay> index = Map.of();

    public GtfsRtIndexService(
            GtfsRtSnapshot snapshot,
            TripUpdateExtractor extractor) {
        this.snapshot = snapshot;
        this.extractor = extractor;
    }

    /**
     * Rebuilds the realtime index from the latest GTFS-RT snapshot.
     */
    public void rebuild() {
        snapshot.get().ifPresentOrElse(feed -> {

            List<RtStopDelay> delays = extractor.extract(feed);

            Map<TripStopKey, RtStopDelay> next = new HashMap<>();

            for (RtStopDelay d : delays) {
                next.put(new TripStopKey(d.tripId(), d.stopId()), d);
            }

            index = Map.copyOf(next);

            log.info("GTFS-RT index rebuilt: {} stop updates",
                    index.size());

        }, () -> {
            log.warn("No GTFS-RT snapshot available; keeping existing index");
        });
    }

    /**
     * Returns the latest realtime index.
     */
    public Map<TripStopKey, RtStopDelay> getIndex() {
        return index;
    }
}