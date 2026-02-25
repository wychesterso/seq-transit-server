package com.wychesterso.transit.brisbane_bus.realtime_loader.importer;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.wychesterso.transit.brisbane_bus.api.service.time.ServiceClock;
import com.wychesterso.transit.brisbane_bus.api.service.time.ServiceTimeHelper;
import com.wychesterso.transit.brisbane_bus.realtime_loader.TripUpdateExtractor;
import com.wychesterso.transit.brisbane_bus.realtime_loader.loader.GtfsRtLoader;
import com.wychesterso.transit.brisbane_bus.core.model.RtStopDelay;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Responsible for scheduling when to poll GTFS-RT, updating the snapshot and handling failures
 */
@Component
@EnableScheduling
@ConditionalOnProperty(
        name = "app.rt.worker",
        havingValue = "true"
)
public class GtfsRtScheduler {

    private final GtfsRtFetcher client;
    private final GtfsRtLoader loader;
    private final TripUpdateExtractor extractor;

    public GtfsRtScheduler(
            GtfsRtFetcher client,
            GtfsRtLoader loader,
            TripUpdateExtractor extractor) {
        this.client = client;
        this.loader = loader;
        this.extractor = extractor;
    }

    @Scheduled(fixedDelayString = "${gtfs.rt.poll-ms:15000}")
    public void pollTripUpdates() {
        try {
            FeedMessage feed = client.fetchTripUpdates();

            ServiceClock clock = ServiceTimeHelper.now();
            LocalDate serviceDate = clock.serviceDate();

            List<RtStopDelay> delays = extractor.extract(feed, serviceDate);

            loader.replaceAll(delays);

        } catch (Exception e) {
            LoggerFactory.getLogger(getClass())
                    .warn("Failed to poll GTFS-RT TripUpdates", e);
        }
    }
}