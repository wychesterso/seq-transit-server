package com.wychesterso.transit.brisbane_bus.importer;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class GtfsZipExtractor {

    public Path extract(Path zipPath, Path extractDir) throws IOException {

        Files.createDirectories(extractDir);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                Path out = extractDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return extractDir;
    }
}