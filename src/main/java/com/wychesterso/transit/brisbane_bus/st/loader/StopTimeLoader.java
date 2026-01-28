package com.wychesterso.transit.brisbane_bus.st.loader;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class StopTimeLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(StopTimeLoader.class);

    public StopTimeLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void loadStopTimes(Path gtfsDir) throws Exception {

        Path stopTimesFile = gtfsDir.resolve("stop_times.txt");
        if (!Files.exists(stopTimesFile)) {
            throw new IllegalStateException("stop_times.txt not found in " + gtfsDir);
        }

        long start = System.currentTimeMillis();
        log.info("Starting StopTimeLoader using {}", stopTimesFile);

        try (Connection conn = dataSource.getConnection()) {

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            // drop indexes to speed up bulk insert
            try (Statement st = conn.createStatement()) {
                log.info("Dropping indexes...");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_stop_id");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_trip_id");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_stop_trip");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_stop_arrival");
            }

            // clear staging table
            log.info("Truncating stop_times_raw...");
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE stop_times_raw");
            }

            // copy raw csv to staging
            log.info("Starting COPY stop_times_raw...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(stopTimesFile)) {

                long rows = copy.copyIn("""
                    COPY stop_times_raw (
                        trip_id,
                        arrival_time,
                        departure_time,
                        stop_id,
                        stop_sequence,
                        pickup_type,
                        dropoff_type
                    )
                    FROM STDIN WITH (FORMAT csv, HEADER true)
                """, reader);

                log.info("COPY stop_times_raw finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            // transform staging to actual
            log.info("Starting transform + insert into stop_times...");
            long insertStart = System.currentTimeMillis();

            try (Statement st = conn.createStatement()) {
                st.execute("""
                    TRUNCATE stop_times;
        
                    INSERT INTO stop_times (
                        trip_id,
                        arrival_time,
                        departure_time,
                        stop_id,
                        stop_sequence,
                        pickup_type,
                        dropoff_type
                    )
                    SELECT
                        trip_id,
                        split_part(arrival_time, ':', 1)::int * 3600
                          + split_part(arrival_time, ':', 2)::int * 60
                          + split_part(arrival_time, ':', 3)::int,
                        split_part(departure_time, ':', 1)::int * 3600
                          + split_part(departure_time, ':', 2)::int * 60
                          + split_part(departure_time, ':', 3)::int,
                        stop_id,
                        stop_sequence,
                        pickup_type,
                        dropoff_type
                    FROM stop_times_raw;
                """);

                log.info("Insert finished in {} ms",
                        System.currentTimeMillis() - insertStart);
            }

            // recreate indexes
            try (Statement st = conn.createStatement()) {
                log.info("Recreating indexes...");
                st.execute("""
                    CREATE INDEX IF NOT EXISTS idx_stop_times_stop_id
                    ON stop_times (stop_id);
                    CREATE INDEX IF NOT EXISTS idx_stop_times_trip_id
                    ON stop_times (trip_id);
                    CREATE INDEX IF NOT EXISTS idx_stop_times_stop_trip
                    ON stop_times (stop_id, trip_id);
                    CREATE INDEX IF NOT EXISTS idx_stop_times_stop_arrival
                    ON stop_times (stop_id, arrival_time);
                """);
                log.info("Indexes recreated");
            }

            log.info("StopTimeLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}