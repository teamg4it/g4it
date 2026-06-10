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
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsvUtilsTest {

    private CSVRecord buildRecord(String csv) throws Exception {
        CSVParser parser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get()
                .parse(new StringReader(csv));

        List<CSVRecord> recordsCsv = parser.getRecords();

        assertFalse(recordsCsv.isEmpty(), "No CSV record created");

        return recordsCsv.getFirst();
    }

    @Test
    void constructorShouldThrowException() throws Exception {
        Constructor<CsvUtils> constructor = CsvUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(
                UnsupportedOperationException.class,
                exception.getCause());

        assertEquals(
                "This is a utility class and cannot be instantiated",
                exception.getCause().getMessage());
    }

    @Test
    void readShouldReturnValue() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                name
                John
                """);

        assertEquals("John", CsvUtils.read(recordCsv, "name"));
    }

    @Test
    void readShouldReturnNullWhenFieldNotMapped() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                name
                John
                """);

        assertNull(CsvUtils.read(recordCsv, "unknown"));
    }

    @Test
    void readShouldReturnNullWhenValueEmpty() {
        CSVRecord recordCsv = mock(CSVRecord.class);

        when(recordCsv.isMapped("name")).thenReturn(true);
        when(recordCsv.get("name")).thenReturn("");

        assertNull(CsvUtils.read(recordCsv, "name"));
    }

    @Test
    void readWithDefaultShouldReturnValue() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                name
                John
                """);

        assertEquals("John", CsvUtils.read(recordCsv, "name", "default"));
    }

    @Test
    void readWithDefaultShouldReturnDefaultWhenFieldNotMapped() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                name
                John
                """);

        assertEquals("default",
                CsvUtils.read(recordCsv, "unknown", "default"));
    }

    @Test
    void readWithDefaultShouldReturnDefaultWhenValueEmpty() {
        CSVRecord recordCsv = mock(CSVRecord.class);

        when(recordCsv.isMapped("name")).thenReturn(true);
        when(recordCsv.get("name")).thenReturn("");

        assertEquals("default",
                CsvUtils.read(recordCsv, "name", "default"));
    }

    @Test
    void readDoubleShouldReturnDouble() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                amount
                12.5
                """);

        assertEquals(12.5,
                CsvUtils.readDouble(recordCsv, "amount"));
    }

    @Test
    void readDoubleShouldReturnNullWhenMissing() {
        CSVRecord recordCsv = mock(CSVRecord.class);

        when(recordCsv.isMapped("amount")).thenReturn(true);
        when(recordCsv.get("amount")).thenReturn("");

        assertNull(CsvUtils.readDouble(recordCsv, "amount"));
    }

    @Test
    void readDoubleWithDefaultShouldReturnValue() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                amount
                10.5
                """);

        assertEquals(10.5,
                CsvUtils.readDouble(recordCsv, "amount", 1.0));
    }

    @Test
    void readDoubleWithDefaultShouldReturnDefault() {
        CSVRecord recordCsv = mock(CSVRecord.class);

        when(recordCsv.isMapped("amount")).thenReturn(true);
        when(recordCsv.get("amount")).thenReturn("");

        assertEquals(1.0,
                CsvUtils.readDouble(recordCsv, "amount", 1.0));
    }

    @Test
    void readBooleanShouldReturnTrue() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                active
                true
                """);

        assertTrue(CsvUtils.readBoolean(recordCsv, "active"));
    }

    @Test
    void readBooleanShouldReturnFalseWhenMissing() {
        CSVRecord recordCsv = mock(CSVRecord.class);

        when(recordCsv.isMapped("active")).thenReturn(true);
        when(recordCsv.get("active")).thenReturn("");

        assertFalse(CsvUtils.readBoolean(recordCsv, "active"));
    }

    @Test
    void readLocalDateShouldParseValidDate() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                date
                2024-01-15
                """);

        LocalDate result = CsvUtils.readLocalDate(recordCsv, "date");

        assertEquals(LocalDate.of(2024, Month.JANUARY, 15), result);
    }

    @Test
    void readLocalDateShouldReturnNullWhenMissing() {
        CSVRecord recordCsv = mock(CSVRecord.class);

        when(recordCsv.isMapped("date")).thenReturn(true);
        when(recordCsv.get("date")).thenReturn("");

        assertNull(CsvUtils.readLocalDate(recordCsv, "date"));
    }

    @Test
    void readLocalDateShouldReturnErrorDateForInvalidFormat() throws Exception {
        CSVRecord recordCsv = buildRecord("""
                date
                invalid-date
                """);

        assertEquals(
                Constants.ERROR_DATE_FORMAT,
                CsvUtils.readLocalDate(recordCsv, "date"));
    }

    @Test
    void printStringShouldReturnValue() {
        assertEquals("test", CsvUtils.print("test"));
    }

    @Test
    void printStringShouldReturnEmptyWhenNull() {
        assertEquals("", CsvUtils.print((String) null));
    }

    @Test
    void printDoubleShouldReturnValue() {
        assertEquals("10.5", CsvUtils.print(10.5));
    }

    @Test
    void printDoubleShouldReturnEmptyWhenNull() {
        assertEquals("", CsvUtils.print((Double) null));
    }

    @Test
    void printLocalDateShouldReturnValue() {
        LocalDate date = LocalDate.of(2024, Month.JANUARY, 15);

        assertEquals("2024-01-15", CsvUtils.print(date));
    }

    @Test
    void printLocalDateShouldReturnEmptyWhenNull() {
        assertEquals("", CsvUtils.print((LocalDate) null));
    }

    @Test
    void printFirstShouldReturnFirstElement() {
        assertEquals("first",
                CsvUtils.printFirst(List.of("first", "second")));
    }

    @Test
    void printFirstShouldReturnEmptyWhenListNull() {
        assertEquals("", CsvUtils.printFirst(null));
    }

    @Test
    void printSecondShouldReturnSecondElement() {
        assertEquals("second",
                CsvUtils.printSecond(List.of("first", "second")));
    }

    @Test
    void printSecondShouldReturnEmptyWhenListNull() {
        assertEquals("", CsvUtils.printSecond(null));
    }
}
