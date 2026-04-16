/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvFileServiceTest {
    @Mock
    private CsvFileMapperInfo csvFileMapperInfo;
    @InjectMocks
    private CsvFileService csvFileService;
    private AutoCloseable closeable;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        tempDir = Files.createTempDirectory("csv-test");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testGetPrinter_createsFileWithBomAndHeader() throws Exception {
        FileType fileType = FileType.APPLICATION;
        List<Header> headers = List.of(Header.builder().name("col1").build(), Header.builder().name("col2").build());
        when(csvFileMapperInfo.getMapping(fileType)).thenReturn(headers);

        CSVPrinter printer = csvFileService.getPrinter(fileType, tempDir);
        assertNotNull(printer);
        printer.printRecord("val1", "val2");
        printer.close();

        Path csvFile = tempDir.resolve(fileType.getFileName() + ".csv");
        assertTrue(Files.exists(csvFile));
        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            String headerLine = reader.readLine();
            // Remove BOM if present
            if (headerLine != null && headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            assertEquals("col1;col2", headerLine);
            String dataLine = reader.readLine();
            assertEquals("val1;val2", dataLine);
        }
    }

    @Test
    void testGetPrinter_throwsIOExceptionForInvalidDir() {
        FileType fileType = FileType.APPLICATION;
        Path invalidDir = tempDir.resolve("not_exist");
        assertThrows(IOException.class, () -> csvFileService.getPrinter(fileType, invalidDir));
    }
}
