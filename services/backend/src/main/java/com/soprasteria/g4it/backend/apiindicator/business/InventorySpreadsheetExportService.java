// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.zip.ZipInputStream;

@Service
public class InventorySpreadsheetExportService {

    /**
     * Converts stored CSV artifacts without evaluating user text or changing the raw archive.
     * The returned resource deletes its temporary XLSX file when its stream is closed.
     */
    public Resource toSpreadsheet(InputStream source) throws IOException {
        Path output = Files.createTempFile("g4it-spreadsheet-", ".xlsx");
        try {
            try (var zip = new ZipInputStream(source);
                 var workbook = new SXSSFWorkbook(100)) {
                workbook.setCompressTempFiles(true);
                for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (entry.isDirectory()) continue;
                    if (!entry.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
                        throw new G4itRestException("422", "Spreadsheet export requires CSV artifacts");
                    }
                    String name = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                    String sheetName = (workbook.getNumberOfSheets() + 1) + " "
                            + name.substring(0, name.length() - 4);
                    var sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
                    copyCsv(zip, sheet);
                    zip.closeEntry();
                }
                if (workbook.getNumberOfSheets() == 0) {
                    throw new G4itRestException("422", "No CSV artifacts found for spreadsheet export");
                }
                try (var stream = Files.newOutputStream(output)) {
                    workbook.write(stream);
                }
            }
            return new InputStreamResource(Files.newInputStream(output, StandardOpenOption.DELETE_ON_CLOSE));
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(output);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    private void copyCsv(ZipInputStream zip, SXSSFSheet sheet) throws IOException {
        var entryStream = new FilterInputStream(zip) {
            @Override
            public void close() {
                // Closing one CSV parser must not close the shared archive.
            }
        };
        try (var reader = new PushbackReader(new InputStreamReader(entryStream, StandardCharsets.UTF_8), 1)) {
            int first = reader.read();
            if (first != -1 && first != '\uFEFF') reader.unread(first);
            try (var parser = CSVFormat.RFC4180.builder().setDelimiter(CsvUtils.DELIMITER).build().parse(reader)) {
                int rowIndex = 0;
                for (CSVRecord record : parser) {
                    copyRow(sheet, rowIndex++, record);
                }
            }
        }
    }

    private void copyRow(SXSSFSheet sheet, int rowIndex, CSVRecord record) {
        var limits = SpreadsheetVersion.EXCEL2007;
        if (rowIndex >= limits.getMaxRows() || record.size() > limits.getMaxColumns()) {
            throw new G4itRestException("422", "CSV artifact exceeds spreadsheet row or column limits");
        }
        var row = sheet.createRow(rowIndex);
        for (int column = 0; column < record.size(); column++) {
            String value = record.get(column);
            if (value.length() > limits.getMaxTextLength()) {
                throw new G4itRestException("422", "CSV value exceeds spreadsheet cell length limit");
            }
            row.createCell(column, CellType.STRING).setCellValue(value);
        }
    }
}
