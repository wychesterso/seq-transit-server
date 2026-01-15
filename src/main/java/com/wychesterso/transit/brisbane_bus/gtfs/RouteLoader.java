package com.wychesterso.transit.brisbane_bus.gtfs;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class RouteLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(RouteLoader.class);

    public RouteLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public void loadRoutes(Path gtfsDir) throws Exception {

        Path routesFile = gtfsDir.resolve("routes.txt");
        if (!Files.exists(routesFile)) {
            throw new IllegalStateException("routes.txt not found in " + gtfsDir);
        }

        long start = System.currentTimeMillis();
        log.info("Starting RouteLoader...");

        try (Connection conn = dataSource.getConnection()) {

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            // clear table
            log.info("Truncating routes...");
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE routes");
            }

            // copy raw csv to staging
            log.info("Starting COPY routes...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(routesFile)) {

                long rows = copy.copyIn("""
                            COPY routes (
                                route_id,
                                route_short_name,
                                route_long_name,
                                route_desc,
                                route_type,
                                route_url,
                                route_color,
                                route_text_color
                            )
                            FROM STDIN WITH (FORMAT csv, HEADER true)
                        """, reader);

                log.info("COPY routes finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            log.info("RouteLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}