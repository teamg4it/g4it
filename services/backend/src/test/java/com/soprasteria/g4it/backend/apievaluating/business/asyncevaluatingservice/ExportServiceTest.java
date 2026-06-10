/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private LocalFileService localFileService;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private ExportService exportService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                exportService,
                "localWorkingFolder",
                tempDir.toString());
    }

    @Test
    void shouldCreateExportDirectory() {
        Path result = exportService.createExportDirectory(123L);

        assertTrue(Files.exists(result));
        assertTrue(Files.isDirectory(result));
    }

    @Test
    void shouldUploadExportZip() throws IOException  {
        Long taskId = 123L;
        String organization = "ORG";
        String workspaceId = "WS";

        Path exportPath = tempDir.resolve("export").resolve(taskId.toString());
        Files.createDirectories(exportPath);
        Files.writeString(exportPath.resolve("file.txt"), "content");

        File zipFile = exportPath.resolve(taskId + ".zip").toFile();

        when(fileSystem.mount(organization, workspaceId))
                .thenReturn(fileStorage);

        when(localFileService.isEmpty(exportPath))
                .thenReturn(false);

        when(localFileService.createZipFile(
                eq(exportPath),
                anyString()))
                .thenReturn(zipFile);

        exportService.uploadExportZip(taskId, organization, workspaceId);

        verify(fileStorage)
                .upload(
                        zipFile.getAbsolutePath(),
                        FileFolder.EXPORT,
                        taskId + Constants.ZIP);
    }

    @Test
    void shouldNotUploadWhenDirectoryDoesNotExist() throws Exception {
        when(fileSystem.mount("ORG", "WS"))
                .thenReturn(fileStorage);

        exportService.uploadExportZip(123L, "ORG", "WS");

        verifyNoInteractions(localFileService);
        verify(fileStorage, never())
                .upload(anyString(), any(), anyString());
    }

    @Test
    void shouldNotUploadWhenDirectoryIsEmpty() throws Exception {
        Long taskId = 123L;

        Path exportPath = tempDir.resolve("export").resolve(taskId.toString());
        Files.createDirectories(exportPath);

        when(fileSystem.mount("ORG", "WS"))
                .thenReturn(fileStorage);

        when(localFileService.isEmpty(exportPath))
                .thenReturn(true);

        exportService.uploadExportZip(taskId, "ORG", "WS");

        verify(localFileService).isEmpty(exportPath);
        verify(fileStorage, never())
                .upload(anyString(), any(), anyString());
    }


    @Test
    void shouldCleanLocalDirectory() throws Exception {
        Path exportPath = tempDir.resolve("export").resolve("123");
        Files.createDirectories(exportPath);

        exportService.clean(123L);

        assertFalse(Files.exists(exportPath));
    }

    @Test
    void shouldDeleteExportFromStorage() throws Exception {
        when(fileSystem.mount("ORG", "WS"))
                .thenReturn(fileStorage);

        exportService.cleanExport(123L, "ORG", "WS");

        verify(fileStorage)
                .delete(FileFolder.EXPORT, "123.zip");
    }

    @Test
    void shouldThrowAsyncTaskExceptionWhenDeleteFails() throws Exception {
        when(fileSystem.mount("ORG", "WS"))
                .thenReturn(fileStorage);

        doThrow(new IOException())
                .when(fileStorage)
                .delete(FileFolder.EXPORT, "123.zip");

        assertThrows(
                AsyncTaskException.class,
                () -> exportService.cleanExport(123L, "ORG", "WS"));
    }
}
