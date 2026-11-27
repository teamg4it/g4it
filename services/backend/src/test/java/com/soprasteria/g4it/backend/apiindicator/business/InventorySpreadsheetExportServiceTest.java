// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

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

    @Test
    void ignoresArchiveDirectoriesAndPreservesAnEmptyCsvAsAnEmptySheet() throws Exception {
        var resource = service.toSpreadsheet(new ByteArrayInputStream(
                zip("artifacts/", "", "artifacts/physical_equipment.csv", "")));

        try (var input = resource.getInputStream(); var workbook = new XSSFWorkbook(input)) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("1 physical_equipment", workbook.getSheetName(0));
            assertEquals(0, workbook.getSheetAt(0).getPhysicalNumberOfRows());
        }
    }

    @Test
    void preservesTheLastAllowedRowAndRejectsAnExtraRow() throws Exception {
        try (var workbook = new SXSSFWorkbook(1);
             var parser = CSVFormat.RFC4180.parse(new StringReader("synthetic value"))) {
            var sheet = workbook.createSheet("boundary");
            var record = parser.iterator().next();
            int lastRow = SpreadsheetVersion.EXCEL2007.getLastRowIndex();

            // Exercise the real row limit without constructing a million CSV records.
            ReflectionTestUtils.invokeMethod(service, "copyRow", sheet, lastRow, record);
            assertEquals("synthetic value", sheet.getRow(lastRow).getCell(0).getStringCellValue());

            var error = assertThrows(G4itRestException.class,
                    () -> ReflectionTestUtils.invokeMethod(service, "copyRow", sheet, lastRow + 1, record));

            assertEquals("422", error.getCode());
            assertEquals(1, sheet.getPhysicalNumberOfRows());
            assertEquals(lastRow, sheet.getLastRowNum());
        }
    }

    @Test
    void preservesTheExportFailureWhenTemporaryFileCleanupAlsoFails(@TempDir Path directory) throws Exception {
        Path output = Files.createFile(directory.resolve("failed-export.xlsx"));
        var cleanupFailure = new IOException("synthetic cleanup failure");
        byte[] archive = zip("unexpected.txt", "synthetic data");

        try (var files = mockStatic(Files.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("createTempFile".equals(method) && "g4it-spreadsheet-".equals(invocation.getArgument(0))) {
                return output;
            }
            if ("deleteIfExists".equals(method) && output.equals(invocation.getArgument(0))) {
                throw cleanupFailure;
            }
            return invocation.callRealMethod();
        })) {
            var error = assertThrows(G4itRestException.class,
                    () -> service.toSpreadsheet(new ByteArrayInputStream(archive)));

            assertEquals("422", error.getCode());
            assertEquals(1, error.getSuppressed().length);
            assertSame(cleanupFailure, error.getSuppressed()[0]);
        }
        assertTrue(Files.exists(output));
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
