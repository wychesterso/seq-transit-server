package com.wychesterso.transit.seq_transit.rt_loader.service;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.wychesterso.transit.seq_transit.api.service.dto.TripStopKey;
import com.wychesterso.transit.seq_transit.api.service.time.ServiceClock;
import com.wychesterso.transit.seq_transit.api.service.time.ServiceTimeHelper;
import com.wychesterso.transit.seq_transit.core.model.RtStopDelay;
import com.wychesterso.transit.seq_transit.rt_loader.RtTripUpdateExtractor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RtIndexBuilder {

    private final RtTripUpdateExtractor extractor;

    public RtIndexBuilder(RtTripUpdateExtractor extractor) {
        this.extractor = extractor;
    }

    /**
     * Rebuilds the realtime index from the latest GTFS-RT snapshot.
     */
    public Map<TripStopKey, RtStopDelay> build(FeedMessage feed) {

        ServiceClock clock = ServiceTimeHelper.now();
        LocalDate serviceDate = clock.serviceDate();

        List<RtStopDelay> delays = extractor.extract(feed, serviceDate);

        Map<TripStopKey, RtStopDelay> map = new HashMap<>();
        for (RtStopDelay d : delays) {
            map.put(new TripStopKey(d.tripId(), d.stopId()), d);
        }

        return Map.copyOf(map);
    }
}