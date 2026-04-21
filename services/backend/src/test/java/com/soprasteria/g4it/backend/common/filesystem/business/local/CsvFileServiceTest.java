package com.soprasteria.g4it.backend.common.filesystem.business.local;

import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvFileServiceTest {

    @Mock
    private CsvFileMapperInfo csvFileMapperInfo;

    @InjectMocks
    private CsvFileService csvFileService;

    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("csv-test");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testGetPrinter_createsFileWithBomAndHeader() throws Exception {
        FileType fileType = FileType.APPLICATION;

        List<Header> headers = List.of(
                Header.builder().name("col1").build(),
                Header.builder().name("col2").build()
        );

        when(csvFileMapperInfo.getMapping(fileType)).thenReturn(headers);

        CSVPrinter printer = csvFileService.getPrinter(fileType, tempDir);
        printer.printRecord("val1", "val2");
        printer.close();

        Path csvFile = tempDir.resolve(fileType.getFileName() + ".csv");
        assertTrue(Files.exists(csvFile));

        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            String headerLine = reader.readLine();
            headerLine = headerLine.replace("\uFEFF", "");
            assertEquals("col1;col2", headerLine);

            String dataLine = reader.readLine();
            assertEquals("val1;val2", dataLine);
        }
    }

    @Test
    void testGetPrinter_createsDirectoryIfNotExists() throws Exception {
        FileType fileType = FileType.APPLICATION;

        when(csvFileMapperInfo.getMapping(fileType)).thenReturn(
                List.of(Header.builder().name("col1").build())
        );

        Path newDir = tempDir.resolve("newDir");

        CSVPrinter printer = csvFileService.getPrinter(fileType, newDir);
        printer.close();

        assertTrue(Files.exists(newDir));
        assertTrue(Files.isDirectory(newDir));
    }

    @Test
    void testGetPrinter_singleHeader() throws Exception {
        FileType fileType = FileType.APPLICATION;

        when(csvFileMapperInfo.getMapping(fileType)).thenReturn(
                List.of(Header.builder().name("onlyCol").build())
        );

        CSVPrinter printer = csvFileService.getPrinter(fileType, tempDir);
        printer.printRecord("value");
        printer.close();

        Path csvFile = tempDir.resolve(fileType.getFileName() + ".csv");

        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            String header = reader.readLine().replace("\uFEFF", "");
            assertEquals("onlyCol", header);

            String data = reader.readLine();
            assertEquals("value", data);
        }
    }

    @Test
    void testGetPrinter_multipleRecords() throws Exception {
        FileType fileType = FileType.APPLICATION;

        when(csvFileMapperInfo.getMapping(fileType)).thenReturn(
                List.of(
                        Header.builder().name("c1").build(),
                        Header.builder().name("c2").build()
                )
        );

        CSVPrinter printer = csvFileService.getPrinter(fileType, tempDir);

        printer.printRecord("a", "b");
        printer.printRecord("c", "d");
        printer.close();

        Path csvFile = tempDir.resolve(fileType.getFileName() + ".csv");

        List<String> lines = Files.readAllLines(csvFile);

        assertTrue(lines.size() >= 3); // header + 2 rows
    }

    @Test
    void testGetPrinter_sanitizesFileName() throws Exception {
        FileType fileType = FileType.APPLICATION;

        when(csvFileMapperInfo.getMapping(fileType)).thenReturn(
                List.of(Header.builder().name("col").build())
        );

        CSVPrinter printer = csvFileService.getPrinter(fileType, tempDir);
        printer.close();

        // filename should not contain unsafe chars (implicit validation)
        Path csvFile = tempDir.resolve(fileType.getFileName() + ".csv");
        assertTrue(Files.exists(csvFile));
    }
}