/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CsvUtils {

    private CsvUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String DELIMITER = ";";

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
