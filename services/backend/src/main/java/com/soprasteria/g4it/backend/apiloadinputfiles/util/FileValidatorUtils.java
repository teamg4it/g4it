package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class FileValidatorUtils {

    private static final int MAX_ROWS = 100_000;

    private FileValidatorUtils() {
    }

    public static void validateFile(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return;
        }

        files.forEach(FileValidatorUtils::validateSingleFile);
    }

    private static void validateSingleFile(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw invalidFileException();
        }

        String lowerCaseFilename = filename.toLowerCase();

        if (lowerCaseFilename.endsWith(".csv")) {

            validateCsvFile(file);

        } else if (lowerCaseFilename.endsWith(".xlsx")) {

            validateXlsxFile(file);

        } else {

            throw new BadRequestException(
                    "file",
                    "Unsupported file format. Only CSV and XLSX files are allowed."
            );
        }
    }

    private static void validateCsvFile(MultipartFile file) {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            long rowCount = reader.lines().count();

            if (rowCount > MAX_ROWS) {
                throw maxRowsException();
            }

        } catch (IOException e) {
            throw invalidFileException();
        }
    }

    private static void validateXlsxFile(MultipartFile multipartFile) {

        File tempFile = null;

        try {

            ZipSecureFile.setMinInflateRatio(0);

            tempFile = File.createTempFile("g4it-import-", ".xlsx");

            multipartFile.transferTo(tempFile);

            try (OPCPackage opcPackage =
                         OPCPackage.open(tempFile, PackageAccess.READ)) {

                XSSFReader reader = new XSSFReader(opcPackage);

                XMLReader parser = SAXParserFactory.newInstance()
                        .newSAXParser()
                        .getXMLReader();

                RowCounterHandler handler = new RowCounterHandler();

                parser.setContentHandler(handler);

                XSSFReader.SheetIterator sheets =
                        (XSSFReader.SheetIterator) reader.getSheetsData();

                if (!sheets.hasNext()) {
                    throw invalidFileException();
                }

                try (InputStream sheet = sheets.next()) {
                    parser.parse(new InputSource(sheet));
                }

                if (handler.getRowCount() > MAX_ROWS) {
                    throw maxRowsException();
                }
            }

        } catch (BadRequestException e) {

            throw e;

        } catch (Exception e) {

            throw invalidFileException();

        } finally {

            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private static BadRequestException maxRowsException() {

        return new BadRequestException(
                "file",
                "The imported file exceeds the number of rows that the calculation system can process in a single import (100,000 rows). Please perform your import in multiple files."
        );
    }

    private static BadRequestException invalidFileException() {

        return new BadRequestException(
                "file",
                "Unable to read imported file."
        );
    }

    private static class RowCounterHandler extends DefaultHandler {

        private int rowCount = 0;

        @Override
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) {

            if ("row".equals(qName)) {
                rowCount++;
            }
        }

        public int getRowCount() {
            return rowCount;
        }
    }
}