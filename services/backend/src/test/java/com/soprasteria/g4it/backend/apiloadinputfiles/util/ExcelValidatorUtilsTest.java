/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.util;


import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelValidatorUtilsTest {

    @Test
    void validateExcelFile_shouldThrowException_whenWorkbookHasNoSheet() throws Exception {

        byte[] workbookContent;

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // intentionally no sheet

            workbook.write(bos);
            workbookContent = bos.toByteArray();
        }

        MultipartFile file = new MockMultipartFile(
                "file",
                "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbookContent
        );

        List<MultipartFile> files = List.of(file);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> ExcelValidatorUtils.validateExcelFile(files)
        );

        assertEquals("file", exception.getField());

        assertEquals(
                "Excel file does not contain any sheet.",
                exception.getError()
        );
    }

    @Test
    void validateExcelFile_shouldThrowException_whenRowLimitExceeded() throws Exception {

        byte[] workbookContent;

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            var sheet = workbook.createSheet("Sheet1");

            // creates 100001 rows
            for (int i = 0; i <= 100_000; i++) {
                sheet.createRow(i);
            }

            workbook.write(bos);
            workbookContent = bos.toByteArray();
        }

        MultipartFile file = new MockMultipartFile(
                "file",
                "large.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbookContent
        );

        List<MultipartFile> files = List.of(file);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> ExcelValidatorUtils.validateExcelFile(files)
        );

        assertEquals("file", exception.getField());

        assertEquals(
                "The imported file exceeds the number of rows that the calculation system can process in a single import (100,000 rows). Please perform your import in multiple files.",
                exception.getError()
        );
    }

    @Test
    void validateExcelFile_shouldThrowException_whenFileIsInvalid() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "invalid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "invalid-content".getBytes()
        );

        List<MultipartFile> files = List.of(file);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> ExcelValidatorUtils.validateExcelFile(files)
        );

        assertEquals("file", exception.getField());

        assertEquals(
                "Unable to read imported Excel file.",
                exception.getError()
        );
    }

    @Test
    void validateExcelFile_shouldDoNothing_whenFilesAreNull() {

        assertDoesNotThrow(() ->
                ExcelValidatorUtils.validateExcelFile(null)
        );
    }

    @Test
    void validateExcelFile_shouldDoNothing_whenFilesAreEmpty() {

        assertDoesNotThrow(() ->
                ExcelValidatorUtils.validateExcelFile(List.of())
        );
    }
}