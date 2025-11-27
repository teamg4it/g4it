package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;

import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileConversionServiceTest {

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
}
