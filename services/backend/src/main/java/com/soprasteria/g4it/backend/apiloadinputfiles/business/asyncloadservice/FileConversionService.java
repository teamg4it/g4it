/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jopendocument.dom.spreadsheet.Range;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class FileConversionService {
    private static final List<String> ALLOWED_INPUT_CSV_SEPARATORS = List.of(";", ",");

    /**
     * Converts a supported file (*.csv, *.xlsx or *.ods)
     * to CSV (separated by {@link CsvUtils#DELIMITER}.
     *
     * @param file             the file to convert
     * @param originalFilename the original name of the file to convert
     * @return the converted file (should be deleted after it is used)
     * @throws IOException if an I/O error occurs during the conversion
     */
    public File convertFileToCsv(File file, String originalFilename) throws IOException, RuntimeException {
        String extension = StringUtils.getFilenameExtension(originalFilename == null ? "" : originalFilename).toLowerCase();

        String convertedFileName = "converted_" + file.getName() + Constants.CSV;
        Path convertedFilePath = file.toPath().resolveSibling(convertedFileName);
        File convertedFile = convertedFilePath.toFile();

        log.info("Converting '{}' to '{}' separated csv format", originalFilename, CsvUtils.DELIMITER);

        try (FileWriter convertedFileWriter = new FileWriter(convertedFile);
             CSVPrinter csvPrinter = new CSVPrinter(convertedFileWriter, CSVFormat.RFC4180
                     .builder()
                     .setDelimiter(CsvUtils.DELIMITER)
                     .build())) {
            switch (extension) {
                case "csv":
                    convertCsvFileToCsv(file, csvPrinter);
                    break;
                case "xlsx":
                    convertXlsxFileToCsv(file, csvPrinter);
                    break;
                case "ods":
                    convertOdsFileToCsv(file, csvPrinter);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown file extension '%s' for file '%s'", extension, originalFilename));
            }
        }

        return convertedFile;
    }


    /**
     * Given a valid CSV file, formats its content as CSV separated by {@link CsvUtils#DELIMITER} and outputs the result
     * into a CSVPrinter. If the input file is already properly formatted, its content is copied into the CSVPrinter.
     *
     * @param file       the input CSV file
     * @param csvPrinter the output CSVPrinter
     * @throws IOException if an I/O error occurs
     */
    private void convertCsvFileToCsv(File file, CSVPrinter csvPrinter) throws IOException {
        String separator = CsvUtils.DELIMITER;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            handleBomEncoding(bufferedReader);
            String headerRow = bufferedReader.readLine();
            for (String allowedSeparator : ALLOWED_INPUT_CSV_SEPARATORS) {
                if (headerRow.contains(allowedSeparator)) {
                    separator = allowedSeparator;
                    break;
                }
            }
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
             CSVParser csvParser = new CSVParser(bufferedReader, CSVFormat.RFC4180
                     .builder()
                     .setDelimiter(separator)
                     .build())) {
            handleBomEncoding(bufferedReader);
            Iterator<CSVRecord> it = csvParser.stream().iterator();
            while (it.hasNext()) {
                csvPrinter.printRecord(it.next());
            }
        }
    }

    /**
     * Given a valid XLSX file, formats its content as CSV and outputs the result into a CSVPrinter
     *
     * @param file       the input XLSX file
     * @param csvPrinter the output CSVPrinter
     * @throws IOException if an I/O error occurs during the conversion
     */
    private void convertXlsxFileToCsv(File file, CSVPrinter csvPrinter) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(file))) {
            BaseFormulaEvaluator.evaluateAllFormulaCells(workbook);
            DataFormatter formatter = new DataFormatter();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            List<String> rowData = new ArrayList<>();

            int firstRowIndex = sheet.getFirstRowNum();

            if (firstRowIndex < 0) {
                // No row was found => the sheet is empty
                return;
            }

            int firstColumnIndex = sheet.getRow(firstRowIndex).getFirstCellNum();
            int lastRowIndex = sheet.getRow(firstRowIndex).getLastCellNum();
            for (Row row : sheet) {
                for (int columnIndex = firstColumnIndex; columnIndex <= lastRowIndex; columnIndex++) {
                    rowData.add(formatter.formatCellValue(row.getCell(columnIndex)));
                }
                csvPrinter.printRecord(rowData);
                rowData.clear();
            }
        }
    }

    /**
     * Given a valid ODS file, formats its content as CSV and outputs the result into a CSVPrinter
     *
     * @param file       the input ODS file
     * @param csvPrinter the output CSVPrinter
     * @throws IOException if an I/O error occurs during the conversion
     */
    private void convertOdsFileToCsv(File file, CSVPrinter csvPrinter) throws IOException {
        SpreadSheet spreadSheet = SpreadSheet.createFromFile(file);
        org.jopendocument.dom.spreadsheet.Sheet sheet = spreadSheet.getSheet(0);

        Range headerRange = findOdsFileHeaderRange(sheet);

        if (headerRange == null) {
            return;
        }

        List<String> rowData = new ArrayList<>();

        boolean isRowEmpty;
        String cellValue;
        int firstColumnIndex = headerRange.getStartPoint().x;
        int lastColumnIndex = headerRange.getEndPoint().x;
        int rowIndex = headerRange.getStartPoint().y;

        while (rowIndex < sheet.getRowCount()) {
            isRowEmpty = true;
            for (int columnIndex = firstColumnIndex; columnIndex <= lastColumnIndex; columnIndex++) {
                cellValue = sheet.getCellAt(columnIndex, rowIndex).getElement().getValue();
                rowData.add(cellValue);
                if (cellValue != null && !cellValue.isEmpty()) {
                    isRowEmpty = false;
                }
            }

            if (isRowEmpty) {
                break;
            }

            csvPrinter.printRecord(rowData);
            rowData.clear();

            rowIndex++;
        }
    }

    /**
     * Skips the 'byte order mark' double-word if it is present in the input buffered reader.
     *
     * @param bufferedReader the input buffered reader
     * @throws IOException if an I/O error occurs
     */
    private void handleBomEncoding(BufferedReader bufferedReader) throws IOException {
        bufferedReader.mark(4);
        int bomBytes = bufferedReader.read();
        if (bomBytes != 0xFEFF) {
            bufferedReader.reset();
        }
    }

    /**
     * Try and find the range in which the header sits in an ODS file's sheet.
     * Note: this method is meant to be used instead of {@link org.jopendocument.dom.spreadsheet.Sheet#getUsedRange()},
     * which runs extremely slowly.
     * <p>
     * The first populated cell should be in the range
     * (0,0;128,128)
     *
     * @param sheet the sheet to search in
     * @return the header row range if it was found, {@literal null} otherwise.
     */
    private Range findOdsFileHeaderRange(org.jopendocument.dom.spreadsheet.Sheet sheet) {
        int rowIndex = 0;
        int columnIndex = 0;
        Point startPoint = null;

        // Looks for the top-leftmost populated cell in a reasonable range
        while (rowIndex < 128) {
            columnIndex = 0;
            while (columnIndex < 128) {
                if (!sheet.getCellAt(columnIndex, rowIndex).isEmpty()) {
                    startPoint = new Point(columnIndex, rowIndex);
                    break;
                }
                columnIndex++;
            }
            if (startPoint != null) {
                break;
            }
            rowIndex++;
        }

        if (startPoint == null) {
            return null;
        }

        while (columnIndex < sheet.getColumnCount() && !sheet.getCellAt(columnIndex, rowIndex).isEmpty()) {
            columnIndex++;
        }

        Point endPoint = new Point(columnIndex - 1, rowIndex);

        return new Range("", startPoint, endPoint);
    }
}
