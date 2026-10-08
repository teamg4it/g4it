package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.common.filesystem.model.StoredFile;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public final class FileValidatorUtils {



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

        try {

            long rowCount = CsvReaderUtils.execute(
                    storedFile.getPath(),
                    reader -> {
                        try {
                            long count = 0;

                            while (reader.readLine() != null) {
                                count++;

                                if (count > Constants.MAX_ROWS) {
                                    break;
                                }
                            }

                            return count;

                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

            if (rowCount > Constants.MAX_ROWS) {
                throwMaxRowsException(storedFile.getOriginalFilename());
            }

        } catch (BadRequestException e) {
            throw e;

        } catch (Exception e) {

            log.error(
                    Constants.VALIDATION_IMPORT_CSV_ERROR + "{}",
                    storedFile.getOriginalFilename(),
                    e
            );

            throw new BadRequestException(
                    "file",
                    Constants.READ_CSV_ERROR
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

                XMLReader parser = createSecureXmlReader();

                XSSFReader.SheetIterator sheets =
                        (XSSFReader.SheetIterator)
                                reader.getSheetsData();

                if (!sheets.hasNext()) {
                    throw new BadRequestException(
                            "file",
                            Constants.BLANK_EXCEL_MSG
                    );
                }

                while (sheets.hasNext()) {

                    RowCounterHandler handler =
                            new RowCounterHandler();

                    parser.setContentHandler(handler);

                    try (InputStream sheet = sheets.next()) {
                        parser.parse(new InputSource(sheet));
                    }

                    if (handler.getRowCount() > Constants.MAX_ROWS) {
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
                    Constants.VALIDATION_IMPORT_EXCEL_ERROR + "{}",
                    storedFile.getOriginalFilename(),
                    e
            );

            throw new BadRequestException(
                    "file",
                    Constants.READ_EXCEL_ERROR
            );
        }
    }

    /**
     * Creates a {@link XMLReader} hardened against XXE (XML External Entity) attacks.
     * Disables DTD processing and external general/parameter entities so that
     * untrusted XML content (e.g. xlsx sheet XML) cannot trigger loading of
     * external resources or entity expansion.
     *
     * @return a securely configured XMLReader
     * @throws ParserConfigurationException if the underlying parser cannot be configured
     * @throws SAXException                 if the underlying parser does not support a feature
     */
    private static XMLReader createSecureXmlReader()
            throws ParserConfigurationException, SAXException {

        SAXParserFactory factory = SAXParserFactory.newInstance();

        // Disable DTDs entirely - the primary defense against XXE.
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        // Defense in depth: disable external entities even if DTDs were allowed.
        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );
        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setXIncludeAware(false);
        factory.setNamespaceAware(true);

        XMLReader xmlReader = factory.newSAXParser().getXMLReader();

        // Ensure no external entity resolution occurs at the reader level too.
        xmlReader.setEntityResolver(
                (publicId, systemId) -> new InputSource(new StringReader(""))
        );

        return xmlReader;
    }

    private static void throwMaxRowsException(String filename) {

        throw new BadRequestException(
                "file",
                String.format(
                        "%s" + Constants.VALIDATION_MSG,
                        filename
                )
        );
    }

    @Getter
    private static class RowCounterHandler extends DefaultHandler {

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
    }
}