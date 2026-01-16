package com.wychesterso.transit.brisbane_bus.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


@Component
public class GtfsRtClient {

    private static final Logger log =
            LoggerFactory.getLogger(GtfsRtClient.class);

    private static final URI TRIP_UPDATES_URI =
            URI.create("https://gtfsrt.api.translink.com.au/api/realtime/SEQ/TripUpdates");

    public FeedMessage fetchTripUpdates() throws IOException {
        long start = System.currentTimeMillis();

        try (InputStream in = TRIP_UPDATES_URI.toURL().openStream()) {
            FeedMessage feed = FeedMessage.parseFrom(in);

            log.info(
                    "GTFS-RT TripUpdates fetched: timestamp={}, entities={}, took={}ms",
                    feed.getHeader().getTimestamp(),
                    feed.getEntityCount(),
                    System.currentTimeMillis() - start
            );

            return feed;
        }
    }
}