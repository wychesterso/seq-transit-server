package com.wychesterso.transit.brisbane_bus.gtfs;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class CalendarDateLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(CalendarDateLoader.class);

    public CalendarDateLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void loadCalendarDates(Path gtfsDir) throws Exception {

        Path calendarDatesFile = gtfsDir.resolve("calendar_dates.txt");
        if (!Files.exists(calendarDatesFile)) {
            throw new IllegalStateException("calendar_dates.txt not found in " + gtfsDir);
        }

        long start = System.currentTimeMillis();
        log.info("Starting CalendarDateLoader...");

        try (Connection conn = dataSource.getConnection()) {

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            // clear staging table
            log.info("Truncating calendar_dates_raw...");
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE calendar_dates_raw");
            }

            // copy raw csv to staging
            log.info("Starting COPY calendar_dates_raw...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(calendarDatesFile)) {

                long rows = copy.copyIn("""
                    COPY calendar_dates_raw (
                        service_id,
                        date,
                        exception_type
                    )
                    FROM STDIN WITH (FORMAT csv, HEADER true)
                """, reader);

                log.info("COPY calendar_dates_raw finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            // transform staging to actual
            log.info("Starting transform + insert into calendar_dates...");
            long insertStart = System.currentTimeMillis();

            try (Statement st = conn.createStatement()) {
                st.execute("""
                    TRUNCATE calendar_dates;
        
                    INSERT INTO calendar_dates (
                        service_id,
                        date,
                        exception_type
                    )
                    SELECT
                        service_id,
                        to_date(date, 'YYYYMMDD'),
                        exception_type::smallint
                    FROM calendar_dates_raw;
                """);

                log.info("Insert finished in {} ms",
                        System.currentTimeMillis() - insertStart);
            }

            log.info("CalendarDateLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}
