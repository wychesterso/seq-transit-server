package com.wychesterso.transit.brisbane_bus.rt;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class GtfsRtScheduler {

    private final GtfsRtClient client;
    private final GtfsRtSnapshot snapshot;

    public GtfsRtScheduler(
            GtfsRtClient client,
            GtfsRtSnapshot snapshot) {
        this.client = client;
        this.snapshot = snapshot;
    }

    @Scheduled(fixedDelayString = "${gtfs.rt.poll-ms:15000}")
    public void pollTripUpdates() {
        try {
            FeedMessage feed = client.fetchTripUpdates();
            snapshot.update(feed);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass())
                    .warn("Failed to poll GTFS-RT TripUpdates", e);
        }
    }
}