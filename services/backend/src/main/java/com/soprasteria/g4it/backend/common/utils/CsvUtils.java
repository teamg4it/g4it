/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public class CsvUtils {

    private CsvUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String DELIMITER = ";";

    /**
     * Normalize only header keys, never cell values. Empty trailing columns remain
     * compatible, but non-empty names that collide after trimming are rejected.
     */
    public static CSVParser parseWithNormalizedHeaders(Reader reader) throws IOException {
        PushbackReader input = new PushbackReader(reader, 1);
        String header = readHeader(input);
        List<String> names;
        try (CSVParser headerParser = CSVFormat.RFC4180.builder()
                .setDelimiter(DELIMITER).setHeader().setAllowMissingColumnNames(true)
                .build().parse(new StringReader(header))) {
            names = headerParser.getHeaderNames().stream().map(String::trim).toList();
        }
        Set<String> seen = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (String name : names) {
            if (!name.isEmpty() && !seen.add(name)) duplicates.add(name);
        }
        if (!duplicates.isEmpty()) throw new DuplicateHeaderException(List.copyOf(duplicates));
        return CSVFormat.RFC4180.builder()
                .setDelimiter(DELIMITER).setHeader(names.toArray(String[]::new))
                .setAllowMissingColumnNames(true).setSkipHeaderRecord(false)
                .build().parse(input);
    }

    private static String readHeader(PushbackReader reader) throws IOException {
        StringBuilder header = new StringBuilder();
        boolean quoted = false;
        int character;
        while ((character = reader.read()) != -1) {
            header.append((char) character);
            if (character == '"') quoted = !quoted;
            if (!quoted && (character == '\n' || character == '\r')) {
                if (character == '\r') {
                    int next = reader.read();
                    if (next == '\n') header.append('\n');
                    else if (next != -1) reader.unread(next);
                }
                break;
            }
        }
        return header.toString();
    }

    public static final class DuplicateHeaderException extends IllegalArgumentException {
        private final List<String> headers;

        private DuplicateHeaderException(List<String> headers) {
            super("Duplicate normalized CSV headers");
            this.headers = headers;
        }

        public List<String> getHeaders() {
            return headers;
        }
    }

    public static String read(CSVRecord csvRecord, String field) {
        if (!csvRecord.isMapped(field)) return null;

        final String value = csvRecord.get(field);
        if ("".equals(value)) return null;
        return value;
    }

    public static String read(CSVRecord csvRecord, String field, String defaultValue) {
        if (!csvRecord.isMapped(field)) return defaultValue;

        final String value = csvRecord.get(field);
        if ("".equals(value)) return defaultValue;
        return value;
    }

    public static Double readDouble(CSVRecord csvRecord, String field) {
        final String value = read(csvRecord, field);
        if (value == null) return null;
        return Double.parseDouble(value);
    }

    public static Double readDouble(CSVRecord csvRecord, String field, Double defaultValue) {
        final String value = read(csvRecord, field);
        if (value == null) return defaultValue;
        return Double.parseDouble(value);
    }

    public static Boolean readBoolean(CSVRecord csvRecord, String field) {
        final String value = read(csvRecord, field);
        if (value == null) return false;
        return Boolean.parseBoolean(value);
    }

    public static LocalDate readLocalDate(CSVRecord csvRecord, String field) {
        final String value = read(csvRecord, field);
        if (value == null) return null;
        for (DateTimeFormatter dateTimeFormatter : Constants.LOCAL_DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, dateTimeFormatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return Constants.ERROR_DATE_FORMAT;
    }

    public static String print(String value) {
        if (value == null) return "";
        return value;
    }

    public static String print(Double value) {
        if (value == null) return "";
        return value.toString();
    }

    public static String print(LocalDate value) {
        if (value == null) return "";
        return value.toString();
    }

    public static String printFirst(List<String> list) {
        if (list == null) return "";
        return list.getFirst();
    }

    public static String printSecond(List<String> list) {
        if (list == null) return "";
        return list.get(1);
    }
}
