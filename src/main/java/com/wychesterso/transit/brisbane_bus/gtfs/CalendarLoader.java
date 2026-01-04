package com.wychesterso.transit.brisbane_bus.gtfs;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.wychesterso.transit.brisbane_bus.model.Calendar;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarLoader {
    public static List<Calendar> loadCalendar() throws IOException, CsvValidationException {
        List<Calendar> calendars = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/static/SEQ_GTFS/calendar.txt"))) {

            reader.readNext();
            String[] row;

            while ((row = reader.readNext()) != null) {
                calendars.add(new Calendar(
                        row[0],
                        LoadHelper.parseBoolean(row[1]),
                        LoadHelper.parseBoolean(row[2]),
                        LoadHelper.parseBoolean(row[3]),
                        LoadHelper.parseBoolean(row[4]),
                        LoadHelper.parseBoolean(row[5]),
                        LoadHelper.parseBoolean(row[6]),
                        LoadHelper.parseBoolean(row[7]),
                        LocalDate.parse(row[8], DateTimeFormatter.BASIC_ISO_DATE),
                        LocalDate.parse(row[9], DateTimeFormatter.BASIC_ISO_DATE)
                ));
            }
        }

        return calendars;
    }
}
