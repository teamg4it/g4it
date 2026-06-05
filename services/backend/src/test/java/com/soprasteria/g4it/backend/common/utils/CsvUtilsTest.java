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
                .build()
                .parse(new StringReader(csv));

        List<CSVRecord> records = parser.getRecords();

        assertFalse(records.isEmpty(), "No CSV record created");

        return records.getFirst();
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
        CSVRecord record = buildRecord("""
                name
                John
                """);

        assertEquals("John", CsvUtils.read(record, "name"));
    }

    @Test
    void readShouldReturnNullWhenFieldNotMapped() throws Exception {
        CSVRecord record = buildRecord("""
                name
                John
                """);

        assertNull(CsvUtils.read(record, "unknown"));
    }

    @Test
    void readShouldReturnNullWhenValueEmpty() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.isMapped("name")).thenReturn(true);
        when(record.get("name")).thenReturn("");

        assertNull(CsvUtils.read(record, "name"));
    }

    @Test
    void readWithDefaultShouldReturnValue() throws Exception {
        CSVRecord record = buildRecord("""
                name
                John
                """);

        assertEquals("John", CsvUtils.read(record, "name", "default"));
    }

    @Test
    void readWithDefaultShouldReturnDefaultWhenFieldNotMapped() throws Exception {
        CSVRecord record = buildRecord("""
                name
                John
                """);

        assertEquals("default",
                CsvUtils.read(record, "unknown", "default"));
    }

    @Test
    void readWithDefaultShouldReturnDefaultWhenValueEmpty() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.isMapped("name")).thenReturn(true);
        when(record.get("name")).thenReturn("");

        assertEquals("default",
                CsvUtils.read(record, "name", "default"));
    }

    @Test
    void readDoubleShouldReturnDouble() throws Exception {
        CSVRecord record = buildRecord("""
                amount
                12.5
                """);

        assertEquals(12.5,
                CsvUtils.readDouble(record, "amount"));
    }

    @Test
    void readDoubleShouldReturnNullWhenMissing() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.isMapped("amount")).thenReturn(true);
        when(record.get("amount")).thenReturn("");

        assertNull(CsvUtils.readDouble(record, "amount"));
    }

    @Test
    void readDoubleWithDefaultShouldReturnValue() throws Exception {
        CSVRecord record = buildRecord("""
                amount
                10.5
                """);

        assertEquals(10.5,
                CsvUtils.readDouble(record, "amount", 1.0));
    }

    @Test
    void readDoubleWithDefaultShouldReturnDefault() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.isMapped("amount")).thenReturn(true);
        when(record.get("amount")).thenReturn("");

        assertEquals(1.0,
                CsvUtils.readDouble(record, "amount", 1.0));
    }

    @Test
    void readBooleanShouldReturnTrue() throws Exception {
        CSVRecord record = buildRecord("""
                active
                true
                """);

        assertTrue(CsvUtils.readBoolean(record, "active"));
    }

    @Test
    void readBooleanShouldReturnFalseWhenMissing() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.isMapped("active")).thenReturn(true);
        when(record.get("active")).thenReturn("");

        assertFalse(CsvUtils.readBoolean(record, "active"));
    }

    @Test
    void readLocalDateShouldParseValidDate() throws Exception {
        CSVRecord record = buildRecord("""
                date
                2024-01-15
                """);

        LocalDate result = CsvUtils.readLocalDate(record, "date");

        assertEquals(LocalDate.of(2024, 1, 15), result);
    }

    @Test
    void readLocalDateShouldReturnNullWhenMissing() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.isMapped("date")).thenReturn(true);
        when(record.get("date")).thenReturn("");

        assertNull(CsvUtils.readLocalDate(record, "date"));
    }

    @Test
    void readLocalDateShouldReturnErrorDateForInvalidFormat() throws Exception {
        CSVRecord record = buildRecord("""
                date
                invalid-date
                """);

        assertEquals(
                Constants.ERROR_DATE_FORMAT,
                CsvUtils.readLocalDate(record, "date"));
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
        LocalDate date = LocalDate.of(2024, 1, 15);

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
