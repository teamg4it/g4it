/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.common.filesystem.model.StoredFile;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileValidatorUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnWhenFilesAreNull() {
        assertDoesNotThrow(() ->
                FileValidatorUtils.validateFiles(null));
    }

    @Test
    void shouldReturnWhenFilesAreEmpty() {
        assertDoesNotThrow(() ->
                FileValidatorUtils.validateFiles(Map.of()));
    }

    @Test
    void shouldIgnoreFileWithNullFilename() {

        StoredFile file = new StoredFile(
                tempDir.resolve("file.tmp"),
                null,
                "text/plain"
        );

        assertDoesNotThrow(() ->
                FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))));
    }

    @Test
    void shouldIgnoreUnsupportedExtension() throws Exception {

        Path txt = tempDir.resolve("test.txt");
        Files.writeString(txt, "content");

        StoredFile file = new StoredFile(
                txt,
                "test.txt",
                "text/plain"
        );

        assertDoesNotThrow(() ->
                FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))));
    }

    @Test
    void shouldValidateCsvFile() throws Exception {

        Path csv = tempDir.resolve("test.csv");

        Files.writeString(csv,
                """
                header
                value1
                value2
                """
        );

        StoredFile file = new StoredFile(
                csv,
                "test.csv",
                "text/csv"
        );

        assertDoesNotThrow(() ->
                FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))));
    }

    @Test
    void shouldThrowExceptionWhenCsvExceedsMaxRows() throws Exception {

        Path csv = tempDir.resolve("large.csv");

        StringBuilder content = new StringBuilder();

        for (int i = 0; i <= Constants.MAX_ROWS; i++) {
            content.append("row").append(i).append('\n');
        }

        Files.writeString(csv, content);

        StoredFile file = new StoredFile(
                csv,
                "large.csv",
                "text/csv"
        );

        Map<String, List<StoredFile>> files =
                Map.of("files", List.of(file));

        assertThrows(
                BadRequestException.class,
                () -> FileValidatorUtils.validateFiles(files)
        );
    }

    @Test
    void shouldThrowReadCsvErrorWhenCsvCannotBeRead() {

        Path missingFile = tempDir.resolve("missing.csv");

        StoredFile file = new StoredFile(
                missingFile,
                "missing.csv",
                "text/csv"
        );

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))
                )
        );

        assertEquals(
                Constants.READ_CSV_ERROR,
                ex.getError()
        );
    }

    @Test
    void shouldValidateXlsxFile() throws Exception {

        Path xlsx = tempDir.resolve("test.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            workbook.createSheet("Sheet1")
                    .createRow(0)
                    .createCell(0)
                    .setCellValue("value");

            try (var outputStream =
                         Files.newOutputStream(xlsx)) {
                workbook.write(outputStream);
            }
        }

        StoredFile file = new StoredFile(
                xlsx,
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        assertDoesNotThrow(() ->
                FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))));
    }

    @Test
    void shouldThrowExceptionWhenXlsxIsCorrupted() throws Exception {

        Path xlsx = tempDir.resolve("corrupted.xlsx");

        Files.writeString(
                xlsx,
                "this is not a valid excel file"
        );

        StoredFile file = new StoredFile(
                xlsx,
                "corrupted.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        Map<String, List<StoredFile>> files =
                Map.of("files", List.of(file));

        assertThrows(
                BadRequestException.class,
                () -> FileValidatorUtils.validateFiles(files)
        );
    }

    @Test
    void shouldValidateMultipleFiles() throws Exception {

        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "header\nvalue");

        Path xlsx = tempDir.resolve("test.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            workbook.createSheet("Sheet1")
                    .createRow(0)
                    .createCell(0)
                    .setCellValue("value");

            try (var outputStream =
                         Files.newOutputStream(xlsx)) {
                workbook.write(outputStream);
            }
        }

        StoredFile csvFile = new StoredFile(
                csv,
                "test.csv",
                "text/csv"
        );

        StoredFile xlsxFile = new StoredFile(
                xlsx,
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        assertDoesNotThrow(() ->
                FileValidatorUtils.validateFiles(
                        Map.of(
                                "batch",
                                List.of(csvFile, xlsxFile)
                        )));
    }
    @Test
    void shouldRethrowBadRequestWhenCsvExceedsMaxRows() throws Exception {

        Path csv = tempDir.resolve("large.csv");

        StringBuilder content = new StringBuilder();

        for (int i = 0; i <= Constants.MAX_ROWS; i++) {
            content.append("row").append(i).append('\n');
        }

        Files.writeString(csv, content);

        StoredFile file = new StoredFile(
                csv,
                "large.csv",
                "text/csv"
        );

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))
                )
        );

        assertTrue(ex.getError().contains("large.csv"));
    }

    @Test
    void shouldThrowBadRequestForUtf32Csv() throws Exception {

        Path csv = tempDir.resolve("utf32.csv");

        Files.write(
                csv,
                new byte[]{
                        (byte) 0xFF,
                        (byte) 0xFE,
                        0x00,
                        0x00
                }
        );

        StoredFile file = new StoredFile(
                csv,
                "utf32.csv",
                "text/csv"
        );

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))
                )
        );

        assertEquals(
                Constants.UNSUPPORTED_UTF32_ERROR,
                ex.getError()
        );
    }

    @Test
    void shouldThrowExceptionWhenXlsxExceedsMaxRows() throws Exception {

        Path xlsx = tempDir.resolve("large.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            var sheet = workbook.createSheet("Sheet1");

            for (int i = 0; i <= Constants.MAX_ROWS; i++) {
                sheet.createRow(i)
                        .createCell(0)
                        .setCellValue("value");
            }

            try (var os = Files.newOutputStream(xlsx)) {
                workbook.write(os);
            }
        }

        StoredFile file = new StoredFile(
                xlsx,
                "large.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> FileValidatorUtils.validateFiles(
                        Map.of("files", List.of(file))
                )
        );

        assertTrue(ex.getError().contains("large.xlsx"));
    }
}