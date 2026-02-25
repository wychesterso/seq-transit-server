package com.wychesterso.transit.brisbane_bus.realtime_loader.loader;

import com.wychesterso.transit.brisbane_bus.core.model.RtStopDelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Component
public class GtfsRtLoader {

    private static final int BATCH_SIZE = 1000;

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(GtfsRtLoader.class);

    public GtfsRtLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void replaceAll(Collection<RtStopDelay> values) throws Exception {

        long start = System.currentTimeMillis();
        log.info("Starting GtfsRtLoader UPSERT ({} records)...", values.size());

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            String sql = """
                INSERT INTO gtfs_rt_stop_delay (
                    trip_id,
                    stop_id,
                    effective_arrival_seconds,
                    effective_departure_seconds,
                    cancelled,
                    skipped,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, now())
                ON CONFLICT (trip_id, stop_id)
                DO UPDATE SET
                    effective_arrival_seconds = EXCLUDED.effective_arrival_seconds,
                    effective_departure_seconds = EXCLUDED.effective_departure_seconds,
                    cancelled = EXCLUDED.cancelled,
                    skipped = EXCLUDED.skipped,
                    updated_at = EXCLUDED.updated_at
                """;

            int count = 0;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                for (RtStopDelay d : values) {

                    ps.setString(1, d.tripId());
                    ps.setString(2, d.stopId());
                    ps.setInt(3, d.effectiveArrivalSeconds());
                    ps.setInt(4, d.effectiveDepartureSeconds());
                    ps.setBoolean(5, d.cancelled());
                    ps.setBoolean(6, d.skipped());

                    ps.addBatch();
                    count++;

                    if (count % BATCH_SIZE == 0) {
                        ps.executeBatch();
                    }
                }

                ps.executeBatch();
            }

            // remove stale trips
            cleanupStaleRows(conn);

            conn.commit();
        }

        log.info("GtfsRtLoader UPSERT finished in {} ms",
                System.currentTimeMillis() - start);
    }

    /**
     * Removes rows without recent refreshes, preventing table growth as more trips finish.
     */
    private void cleanupStaleRows(Connection conn) throws Exception {

        String deleteSql = """
            DELETE FROM gtfs_rt_stop_delay
            WHERE updated_at < now() - interval '2 minutes'
        """;

        try (Statement st = conn.createStatement()) {
            int removed = st.executeUpdate(deleteSql);
            if (removed > 0) {
                log.info("Removed {} stale realtime rows", removed);
            }
        }
    }
}
