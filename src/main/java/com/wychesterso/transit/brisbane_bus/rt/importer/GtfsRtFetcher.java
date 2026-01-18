package com.wychesterso.transit.brisbane_bus.rt.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

/**
 * Responsible for communicating with the external GTFS-RT HTTP endpoint
 */
@Component
public class GtfsRtFetcher {

    private static final Logger log =
            LoggerFactory.getLogger(GtfsRtFetcher.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gtfs.rt.url}")
    private String tripUpdatesUrl;

    public FeedMessage fetchTripUpdates() {
        try {
            log.info("Fetching GTFS-RT TripUpdates from {}", tripUpdatesUrl);

            byte[] bytes = restTemplate.getForObject(tripUpdatesUrl, byte[].class);
            FeedMessage feed = FeedMessage.parseFrom(bytes);

            log.info("GTFS-RT feed received: {} entities, timestamp={}, age={}",
                    feed.getEntityCount(),
                    feed.getHeader().getTimestamp(),
                    Instant.now().getEpochSecond() - feed.getHeader().getTimestamp());

            return feed;

        } catch (Exception e) {
            log.error("Failed to fetch GTFS-RT TripUpdates", e);
            throw new IllegalStateException("GTFS-RT fetch failed", e);
        }
    }
}