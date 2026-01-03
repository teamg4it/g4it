package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;

import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileConversionServiceTest {

    private final FileConversionService fileConversionService = new FileConversionService();

    @TempDir
    Path tempDir;

    @Test
    void testConvertCsv_semicolonSeparator() throws Exception {
        File csv = tempDir.resolve("input.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("A;B\n1;2");
        }

        File result = fileConversionService.convertFileToCsv(csv, "input.csv");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertEquals("A" + CsvUtils.DELIMITER + "B", lines.get(0));
        assertEquals("1" + CsvUtils.DELIMITER + "2", lines.get(1));
    }

    @Test
    void testConvertCsv_commaSeparator() throws Exception {
        File csv = tempDir.resolve("input.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("X,Y\n3,4");
        }

        File result = fileConversionService.convertFileToCsv(csv, "input.csv");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertEquals("X" + CsvUtils.DELIMITER + "Y", lines.get(0));
        assertEquals("3" + CsvUtils.DELIMITER + "4", lines.get(1));
    }

    @Test
    void testConvertXlsxToCsv() throws Exception {
        File xlsx = tempDir.resolve("input.xlsx").toFile();

        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            var sheet = wb.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("A");
            sheet.getRow(0).createCell(1).setCellValue("B");
            sheet.createRow(1).createCell(0).setCellValue("10");
            sheet.getRow(1).createCell(1).setCellValue("20");

            try (FileOutputStream fos = new FileOutputStream(xlsx)) {
                wb.write(fos);
            }
        }

        File result = fileConversionService.convertFileToCsv(xlsx, "input.xlsx");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertEquals("A;B;", lines.get(0));
        assertEquals("10;20;", lines.get(1));
    }

    @Test
    void testConvertOdsToCsv() throws Exception {
        File ods = tempDir.resolve("input.ods").toFile();

        javax.swing.JTable jTable = new javax.swing.JTable(2, 2);
        SpreadSheet spreadsheet = SpreadSheet.createEmpty(jTable.getModel());
        org.jopendocument.dom.spreadsheet.Sheet sheet = spreadsheet.getSheet(0);

        sheet.getCellAt(0, 0).setValue("H1");
        sheet.getCellAt(1, 0).setValue("H2");
        sheet.getCellAt(0, 1).setValue("5");
        sheet.getCellAt(1, 1).setValue("6");

        spreadsheet.saveAs(ods);

        File result = fileConversionService.convertFileToCsv(ods, "input.ods");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertTrue(lines.get(0).contains("H1"));
        assertTrue(lines.get(0).contains("H2"));
        assertTrue(lines.get(1).contains("5"));
        assertTrue(lines.get(1).contains("6"));
    }


    @Test
    void testUnsupportedExtensionThrowsError() {
        File pdf = tempDir.resolve("file.pdf").toFile();
        assertThrows(IllegalArgumentException.class,
                () -> fileConversionService.convertFileToCsv(pdf, "file.pdf"));
    }

    @Test
    void testPathTraversalIsIgnoredAndHandledSafely() throws Exception {
        File csv = tempDir.resolve("input.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("A,B\n1,2");
        }
        String maliciousFilename = "../wrongfile.csv";
        File result = fileConversionService.convertFileToCsv(csv, maliciousFilename);
        assertNotNull(result);
        assertTrue(result.exists());
    }

    @Test
    void testFileWithoutParentIsHandledSafely() throws Exception {
        File csv = tempDir.resolve("input.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("A,B\n1,2");
        }

        File result = fileConversionService.convertFileToCsv(csv, "input.csv");

        assertNotNull(result);
        assertTrue(result.exists());
    }

    @Test
    void testEmptyCsvFileIsNotSupported() throws Exception {
        File csv = tempDir.resolve("empty.csv").toFile();
        csv.createNewFile();

        assertThrows(
                NullPointerException.class,
                () -> fileConversionService.convertFileToCsv(csv, "empty.csv")
        );
    }

    @Test
    void testConvertCsv_semicolonSeparator2() throws Exception {
        File csv = tempDir.resolve("input.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("A;B\n1;2");
        }

        File result = fileConversionService.convertFileToCsv(csv, "input.csv");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertEquals("A" + CsvUtils.DELIMITER + "B", lines.get(0));
        assertEquals("1" + CsvUtils.DELIMITER + "2", lines.get(1));
    }

    @Test
    void testConvertCsv_commaSeparator2() throws Exception {
        File csv = tempDir.resolve("input.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("X,Y\n3,4");
        }

        File result = fileConversionService.convertFileToCsv(csv, "input.csv");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertEquals("X" + CsvUtils.DELIMITER + "Y", lines.get(0));
        assertEquals("3" + CsvUtils.DELIMITER + "4", lines.get(1));
    }

    @Test
    void testCsvWithNoDelimiterStillProcesses() throws Exception {
        File csv = tempDir.resolve("singleColumn.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("ONLYONECOLUMN");
        }

        File result = fileConversionService.convertFileToCsv(csv, "singleColumn.csv");
        assertTrue(result.exists());
    }

    @Test
    void testCsvWithBomEncoding() throws Exception {
        File csv = tempDir.resolve("bom.csv").toFile();
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(csv), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF' + "A,B\n1,2");
        }

        File result = fileConversionService.convertFileToCsv(csv, "bom.csv");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertFalse(lines.isEmpty());
    }

    @Test
    void testEmptyCsvFileThrowsException() throws Exception {
        File csv = tempDir.resolve("empty.csv").toFile();
        csv.createNewFile();

        assertThrows(
                NullPointerException.class,
                () -> fileConversionService.convertFileToCsv(csv, "empty.csv")
        );
    }

    /* -------------------------------------------------
     * XLSX conversion
     * ------------------------------------------------- */

    @Test
    void testConvertXlsxToCsv2() throws Exception {
        File xlsx = tempDir.resolve("input.xlsx").toFile();

        try (org.apache.poi.ss.usermodel.Workbook wb =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            var sheet = wb.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("A");
            sheet.getRow(0).createCell(1).setCellValue("B");
            sheet.createRow(1).createCell(0).setCellValue("10");
            sheet.getRow(1).createCell(1).setCellValue("20");

            try (FileOutputStream fos = new FileOutputStream(xlsx)) {
                wb.write(fos);
            }
        }

        File result = fileConversionService.convertFileToCsv(xlsx, "input.xlsx");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertEquals("A;B;", lines.get(0));
        assertEquals("10;20;", lines.get(1));
    }

    @Test
    void testEmptyXlsxDoesNotFail() throws Exception {
        File xlsx = tempDir.resolve("empty.xlsx").toFile();

        try (org.apache.poi.ss.usermodel.Workbook wb =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            wb.createSheet();
            try (FileOutputStream fos = new FileOutputStream(xlsx)) {
                wb.write(fos);
            }
        }

        File result = fileConversionService.convertFileToCsv(xlsx, "empty.xlsx");
        assertTrue(result.exists());
    }

    /* -------------------------------------------------
     * ODS conversion
     * ------------------------------------------------- */

    @Test
    void testConvertOdsToCsv2() throws Exception {
        File ods = tempDir.resolve("input.ods").toFile();

        javax.swing.JTable jTable = new javax.swing.JTable(2, 2);
        SpreadSheet spreadsheet = SpreadSheet.createEmpty(jTable.getModel());
        var sheet = spreadsheet.getSheet(0);

        sheet.getCellAt(0, 0).setValue("H1");
        sheet.getCellAt(1, 0).setValue("H2");
        sheet.getCellAt(0, 1).setValue("5");
        sheet.getCellAt(1, 1).setValue("6");

        spreadsheet.saveAs(ods);

        File result = fileConversionService.convertFileToCsv(ods, "input.ods");
        List<String> lines = java.nio.file.Files.readAllLines(result.toPath());

        assertTrue(lines.get(0).contains("H1"));
        assertTrue(lines.get(1).contains("5"));
    }

    @Test
    void testEmptyOdsFileReturnsEmptyCsv() throws Exception {
        File ods = tempDir.resolve("empty.ods").toFile();

        // TableModel is REQUIRED by jOpenDocument
        javax.swing.JTable table = new javax.swing.JTable(1, 1);

        // create empty ODS safely
        SpreadSheet.createEmpty(table.getModel()).saveAs(ods);

        File result = fileConversionService.convertFileToCsv(ods, "empty.ods");

        assertNotNull(result);
        assertTrue(result.exists());
    }


    /* -------------------------------------------------
     * Security & validation
     * ------------------------------------------------- */

    @Test
    void testUnsupportedExtensionThrowsError2() {
        File pdf = tempDir.resolve("file.pdf").toFile();
        assertThrows(
                IllegalArgumentException.class,
                () -> fileConversionService.convertFileToCsv(pdf, "file.pdf")
        );
    }

    @Test
    void testFileNotFoundExceptionWhenConvertedPathEscapesParent() {
        File maliciousFile = new File("../../evil.csv");

        assertThrows(FileNotFoundException.class, () ->
                fileConversionService.convertFileToCsv(maliciousFile, "evil.csv")
        );
    }


    @Test
    void testPathTraversalIsHandledSafely() throws Exception {
        File csv = tempDir.resolve("input.csv").toFile();
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("A,B\n1,2");
        }

        File result = fileConversionService.convertFileToCsv(csv, "../safe.csv");
        assertNotNull(result);
        assertTrue(result.exists());
    }

    @Test
    void shouldThrowSecurityExceptionWhenConvertedPathIsOutsideParent() throws Exception {

        File file = mock(File.class);
        Path filePath = mock(Path.class);

        // real path (do NOT mock this)
        Path maliciousPath = Paths.get("/tmp/evil/converted_file.csv");

        when(file.getName()).thenReturn("test.txt");
        when(file.getParent()).thenReturn("/safe/dir");
        when(file.toPath()).thenReturn(filePath);

        // resolveSibling returns a path outside parent
        when(filePath.resolveSibling(anyString())).thenReturn(maliciousPath);

        assertThrows(
                SecurityException.class,
                () -> fileConversionService.convertFileToCsv(file, "test.txt")
        );
    }


}