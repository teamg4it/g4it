package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.common.filesystem.model.StoredFile;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public final class FileValidatorUtils {

    private static final int MAX_ROWS = 100_000;

    private FileValidatorUtils() {
    }

    public static void validateFiles(
            Map<?, List<StoredFile>> storedFiles) {

        if (storedFiles == null || storedFiles.isEmpty()) {
            return;
        }

        storedFiles.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(FileValidatorUtils::validateFile);
    }

    private static void validateFile(StoredFile storedFile) {

        String filename = storedFile.getOriginalFilename();

        if (filename == null) {
            return;
        }

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".xlsx")) {
            validateXlsx(storedFile);
        } else if (lowerFilename.endsWith(".csv")) {
            validateCsv(storedFile);
        }
    }

    private static void validateCsv(StoredFile storedFile) {

        try (BufferedReader reader =
                     Files.newBufferedReader(storedFile.getPath())) {

            long rowCount = reader.lines().count();

            if (rowCount > MAX_ROWS) {
                throwMaxRowsException(storedFile.getOriginalFilename());
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Unable to validate CSV file {}",
                    storedFile.getOriginalFilename(),
                    e
            );

            throw new BadRequestException(
                    "file",
                    "Unable to read imported CSV file."
            );
        }
    }

    private static void validateXlsx(StoredFile storedFile) {

        try {

            ZipSecureFile.setMinInflateRatio(0);

            try (OPCPackage opcPackage =
                         OPCPackage.open(
                                 storedFile.getPath().toFile(),
                                 PackageAccess.READ)) {

                XSSFReader reader = new XSSFReader(opcPackage);

                XMLReader parser = SAXParserFactory.newInstance()
                        .newSAXParser()
                        .getXMLReader();

                XSSFReader.SheetIterator sheets =
                        (XSSFReader.SheetIterator)
                                reader.getSheetsData();

                if (!sheets.hasNext()) {
                    throw new BadRequestException(
                            "file",
                            "Excel file does not contain any sheet."
                    );
                }

                while (sheets.hasNext()) {

                    RowCounterHandler handler =
                            new RowCounterHandler();

                    parser.setContentHandler(handler);

                    try (InputStream sheet = sheets.next()) {
                        parser.parse(new InputSource(sheet));
                    }

                    if (handler.getRowCount() > MAX_ROWS) {
                        throwMaxRowsException(
                                storedFile.getOriginalFilename()
                        );
                    }
                }
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {

            log.error(
                    "Unable to validate Excel file {}",
                    storedFile.getOriginalFilename(),
                    e
            );

            throw new BadRequestException(
                    "file",
                    "Unable to read imported Excel file."
            );
        }
    }

    private static void throwMaxRowsException(String filename) {

        throw new BadRequestException(
                "file",
                String.format(
                        "The imported file '%s' exceeds the number of rows that the calculation system can process in a single import (100,000 rows). Please perform your import in multiple files.",
                        filename,
                        MAX_ROWS
                )
        );
    }

    private static class RowCounterHandler
            extends DefaultHandler {

        private int rowCount;

        @Override
        public void startElement(
                String uri,
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