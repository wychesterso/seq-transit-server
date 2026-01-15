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
public class CalendarLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(CalendarLoader.class);

    public CalendarLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void loadCalendar(Path gtfsDir) throws Exception {

        Path calendarFile = gtfsDir.resolve("calendar.txt");
        if (!Files.exists(calendarFile)) {
            throw new IllegalStateException("calendar.txt not found in " + gtfsDir);
        }

        long start = System.currentTimeMillis();
        log.info("Starting CalendarLoader...");

        try (Connection conn = dataSource.getConnection()) {

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            // clear staging table
            log.info("Truncating calendar_raw...");
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE calendar_raw");
            }

            // copy raw csv to staging
            log.info("Starting COPY calendar_raw...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(calendarFile)) {

                long rows = copy.copyIn("""
                    COPY calendar_raw (
                        service_id,
                        monday,
                        tuesday,
                        wednesday,
                        thursday,
                        friday,
                        saturday,
                        sunday,
                        start_date,
                        end_date
                    )
                    FROM STDIN WITH (FORMAT csv, HEADER true)
                """, reader);

                log.info("COPY calendar_raw finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            // transform staging to actual
            log.info("Starting transform + insert into calendar...");
            long insertStart = System.currentTimeMillis();

            try (Statement st = conn.createStatement()) {
                st.execute("""
                    TRUNCATE calendar;
        
                    INSERT INTO calendar (
                        service_id,
                        monday,
                        tuesday,
                        wednesday,
                        thursday,
                        friday,
                        saturday,
                        sunday,
                        start_date,
                        end_date
                    )
                    SELECT
                        service_id,
                        monday = '1',
                        tuesday = '1',
                        wednesday = '1',
                        thursday = '1',
                        friday = '1',
                        saturday = '1',
                        sunday = '1',
                        to_date(start_date, 'YYYYMMDD'),
                        to_date(end_date, 'YYYYMMDD')
                    FROM calendar_raw;
                """);

                log.info("Insert finished in {} ms",
                        System.currentTimeMillis() - insertStart);
            }

            log.info("CalendarLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}
