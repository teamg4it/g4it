package com.soprasteria.g4it.backend.apiloadinputfiles.mapper;

import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvHeaderNormalizationTest {
    @Test
    void carriageReturnOnlyHeaderKeepsTheFirstDataCharacter() throws Exception {
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader(
                " contexteUsage ; fractionUsage \rBYOD;0.5\r"))) {
            var records = parser.getRecords();
            assertEquals(List.of("contexteUsage", "fractionUsage"), parser.getHeaderNames());
            assertEquals(1, records.size());
            assertEquals("BYOD", records.getFirst().get("contexteUsage"));
            assertEquals("0.5", records.getFirst().get("fractionUsage"));
        }
    }

    @Test
    void carriageReturnAtEndOfHeaderDoesNotCreateADataRow() throws Exception {
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader(
                " contexteUsage ; fractionUsage \r"))) {
            assertEquals(List.of("contexteUsage", "fractionUsage"), parser.getHeaderNames());
            assertTrue(parser.getRecords().isEmpty());
        }
    }

    @Test
    void trimsOnlyHeadersIncludingQuotedHeadersAndCrLf() throws Exception {
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader(
                "\" contexteUsage \"; fractionUsage \r\n BYOD ;0.5\r\n"))) {
            assertEquals(List.of("contexteUsage", "fractionUsage"), parser.getHeaderNames());
            var records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals(" BYOD ", records.getFirst().get("contexteUsage"));
            assertEquals("0.5", records.getFirst().get("fractionUsage"));
        }
    }

    @Test
    void rejectsNormalizedDuplicatesRatherThanSilentlySelectingOne() {
        for (String header : List.of("contexteUsage; contexteUsage ", "fractionUsage;fractionUsage")) {
            var error = assertThrows(CsvUtils.DuplicateHeaderException.class,
                    () -> CsvUtils.parseWithNormalizedHeaders(new StringReader(header + "\nBYOD;COPE\n")));
            assertEquals(1, error.getHeaders().size());
        }
    }

    @Test
    void legacyEmptyHeadersAndQuotedMultilineCellsRemainCompatible() throws Exception {
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader(
                "type;nomEquipementPhysique;; \nLaptop;\"synthetic\nname\";;\n"))) {
            var records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("Laptop", records.getFirst().get("type"));
            assertEquals("synthetic\nname", records.getFirst().get("nomEquipementPhysique"));
        }
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader(""))) {
            assertTrue(parser.getRecords().isEmpty());
        }
    }

    @Test
    void handlesEscapedQuotesAndLineBreaksInHeadersWithoutConsumingRows() throws Exception {
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader(
                "\" label\"\"quoted \";\" multiline\nheader \"\nvalue;other\n"))) {
            var record = parser.iterator().next();
            assertEquals("value", record.get("label\"quoted"));
            assertEquals("other", record.get("multiline\nheader"));
        }
    }
}
