/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.business.local;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocalFileServiceTest {
    private LocalFileService service;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        service = new LocalFileService();
        tempDir = Files.createTempDirectory("lfs-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testCreateZipFile_withFiles() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("a.csv"));
        Path file2 = Files.createFile(tempDir.resolve("b.csv"));
        Files.writeString(file1, "data1");
        Files.writeString(file2, "data2");
        Path zipPath = tempDir.resolve("out.zip");
        File zip = service.createZipFile(tempDir, zipPath.toString());
        assertTrue(zip.exists());
        assertTrue(zip.length() > 0);
    }

    @Test
    void testCreateZipFile_emptyDirectory() {
        Path zipPath = tempDir.resolve("empty.zip");
        File zip = service.createZipFile(tempDir, zipPath.toString());
        assertTrue(zip.exists());
    }

    @Test
    void testCreateZipFile_nonExistentDirectory() {
        Path nonExistent = tempDir.resolve("nope");
        Path zipPath = tempDir.resolve("fail.zip");
        File zip = service.createZipFile(nonExistent, zipPath.toString());
        assertFalse(zip.exists(), "Zip file should not exist if source directory does not exist");
    }

    @Test
    void testWriteFile_success() throws IOException {
        Path file = tempDir.resolve("test.txt");
        List<String> lines = Arrays.asList("one", "two", "three");
        service.writeFile(file, lines);
        List<String> read = Files.readAllLines(file);
        assertEquals(lines, read);
    }

    @Test
    void testWriteFile_fileNotFound() {
        Path dir = tempDir.resolve("notadir");
        List<String> lines = List.of("fail");
        Path badFile = dir.resolve("/bad/file.txt");
        assertThrows(G4itRestException.class, () -> service.writeFile(badFile, lines));
    }

    @Test
    void testIsEmpty_true() throws IOException {
        assertTrue(service.isEmpty(tempDir));
    }

    @Test
    void testIsEmpty_false() throws IOException {
        Files.createFile(tempDir.resolve("file.txt"));
        assertFalse(service.isEmpty(tempDir));
    }

    @Test
    void testIsEmpty_notDirectory() throws IOException {
        Path file = Files.createFile(tempDir.resolve("file.txt"));
        assertFalse(service.isEmpty(file));
    }
}
