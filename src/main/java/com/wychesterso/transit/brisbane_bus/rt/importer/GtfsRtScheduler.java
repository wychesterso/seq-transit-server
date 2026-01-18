package com.wychesterso.transit.brisbane_bus.rt.importer;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.wychesterso.transit.brisbane_bus.rt.service.GtfsRtIndexService;
import com.wychesterso.transit.brisbane_bus.rt.GtfsRtSnapshot;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Responsible for scheduling when to poll GTFS-RT, updating the snapshot and handling failures
 */
@Component
@EnableScheduling
public class GtfsRtScheduler {

    private final GtfsRtFetcher client;
    private final GtfsRtSnapshot snapshot;
    private final GtfsRtIndexService rtIndex;

    public GtfsRtScheduler(
            GtfsRtFetcher client,
            GtfsRtSnapshot snapshot,
            GtfsRtIndexService rtIndex) {
        this.client = client;
        this.snapshot = snapshot;
        this.rtIndex = rtIndex;
    }

    @Scheduled(fixedDelayString = "${gtfs.rt.poll-ms:15000}")
    public void pollTripUpdates() {
        try {
            FeedMessage feed = client.fetchTripUpdates();
            snapshot.update(feed);
            rtIndex.rebuild();
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass())
                    .warn("Failed to poll GTFS-RT TripUpdates", e);
        }
    }
}