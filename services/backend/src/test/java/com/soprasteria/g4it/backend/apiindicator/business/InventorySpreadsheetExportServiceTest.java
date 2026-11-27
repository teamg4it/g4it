// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class InventorySpreadsheetExportServiceTest {

    private final InventorySpreadsheetExportService service = new InventorySpreadsheetExportService();

    @Test
    void preservesFormulaLikeAndOrdinaryValuesAsTextAcrossArtifacts() throws Exception {
        var values = List.of("=1+1", "+1+1", "-1+1", "@SUM(1)", "\t=1+1", "\r=1+1",
                " =1+1", "0.5000", "0.57", "BYOD", "", "quoted;\"text\"\nnext line");
        var csv = new StringWriter();
        try (var printer = new CSVPrinter(csv, CSVFormat.RFC4180.builder().setDelimiter(';').build())) {
            printer.printRecord(values);
        }
        byte[] archive = zip("physical_equipment.csv", "\uFEFF" + csv, "ind_physical_equipment.csv", csv.toString());
        byte[] original = archive.clone();

        var resource = service.toSpreadsheet(new ByteArrayInputStream(archive));

        try (var input = resource.getInputStream(); var workbook = new XSSFWorkbook(input)) {
            assertEquals(2, workbook.getNumberOfSheets());
            for (var sheet : workbook) {
                var row = sheet.getRow(0);
                assertEquals(values.size(), row.getLastCellNum());
                for (int column = 0; column < values.size(); column++) {
                    var cell = row.getCell(column);
                    assertEquals(CellType.STRING, cell.getCellType());
                    assertEquals(values.get(column), cell.getStringCellValue());
                    assertNull(cell.getHyperlink());
                }
            }
            assertTrue(workbook.getExternalLinksTable().isEmpty());
        }
        assertArrayEquals(original, archive);
    }

    @Test
    void keepsAllRowsAfterTheStreamingWindowAndUsesUniqueSafeSheetNames() throws Exception {
        String csv = "contexteUsage;fractionUsage\r\n" + "COPE;0.8500\r\n".repeat(150);
        var resource = service.toSpreadsheet(new ByteArrayInputStream(zip(
                "one/a-long-artifact-name-that-would-be-truncated.csv", csv,
                "two/a-long-artifact-name-that-would-be-truncated.csv", csv)));

        try (var input = resource.getInputStream(); var workbook = new XSSFWorkbook(input)) {
            assertNotEquals(workbook.getSheetName(0), workbook.getSheetName(1));
            for (var sheet : workbook) {
                assertEquals(151, sheet.getPhysicalNumberOfRows());
                assertEquals("0.8500", sheet.getRow(150).getCell(1).getStringCellValue());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not a ZIP"})
    void rejectsArchivesWithoutCsvArtifacts(String archive) {
        var error = assertThrows(G4itRestException.class,
                () -> service.toSpreadsheet(new ByteArrayInputStream(archive.getBytes(StandardCharsets.UTF_8))));

        assertEquals("422", error.getCode());
    }

    @Test
    void rejectsUnsupportedArtifactsAndOversizedCellsWithoutTruncation() throws Exception {
        for (byte[] archive : List.of(zip("unexpected.txt", "data"),
                zip("physical_equipment.csv", "x".repeat(32768)))) {
            var error = assertThrows(G4itRestException.class,
                    () -> service.toSpreadsheet(new ByteArrayInputStream(archive)));
            assertEquals("422", error.getCode());
        }
    }

    @Test
    void preservesMaximumLengthCellAndRejectsAnExtraColumn() throws Exception {
        String maximumCell = "x".repeat(32767);
        var resource = service.toSpreadsheet(new ByteArrayInputStream(zip("physical_equipment.csv", maximumCell)));
        try (var input = resource.getInputStream(); var workbook = new XSSFWorkbook(input)) {
            assertEquals(maximumCell, workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }

        var error = assertThrows(G4itRestException.class,
                () -> service.toSpreadsheet(new ByteArrayInputStream(
                        zip("physical_equipment.csv", "x;".repeat(16384) + "x"))));
        assertEquals("422", error.getCode());
    }

    private static byte[] zip(String... namesAndContents) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(bytes)) {
            for (int index = 0; index < namesAndContents.length; index += 2) {
                zip.putNextEntry(new ZipEntry(namesAndContents[index]));
                zip.write(namesAndContents[index + 1].getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
