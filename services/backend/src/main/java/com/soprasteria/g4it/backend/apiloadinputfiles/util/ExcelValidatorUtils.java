package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import org.apache.poi.openxml4j.util.ZipSecureFile;

public final class ExcelValidatorUtils {

    private static final int MAX_ROWS = 100_000;

    private ExcelValidatorUtils() {
    }

    public static void validateExcelFile(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return;
        }

        files.forEach(ExcelValidatorUtils::validateFile);
    }

    private static void validateFile(MultipartFile multipartFile) {

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
                    throw new BadRequestException(
                            "file",
                            "Excel file does not contain any sheet."
                    );
                }

                try (InputStream sheet = sheets.next()) {

                    parser.parse(new InputSource(sheet));
                }

                if (handler.getRowCount() > MAX_ROWS) {

                    throw new BadRequestException(
                            "file",
                            "The imported file exceeds the number of rows that the calculation system can process in a single import (100,000 rows). Please perform your import in multiple files."
                    );
                }
            }

        } catch (BadRequestException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new BadRequestException(
                    "file",
                    "Unable to read imported Excel file."
            );

        } finally {

            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
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