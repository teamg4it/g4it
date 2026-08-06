/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apifiles.business;

import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.mapper.FileDescriptionRestMapper;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.soprasteria.g4it.backend.common.filesystem.model.StoredFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FileSystemServiceTest {

    @InjectMocks
    FileSystemService fileSystemService;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private FileStorage fileStorage;
    @Mock
    private FileDescriptionRestMapper fileDescriptionRestMapper;

    @Test
    void testCheckFiles() {

        ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", List.of());
        ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", (Object) null);

        StoredFile file = mock(StoredFile.class);
        when(file.getContentType()).thenReturn("text/csv");

        assertDoesNotThrow(() ->
                ReflectionTestUtils.invokeMethod(
                        fileSystemService,
                        "checkFiles",
                        List.of(file)
                ));
    }

    @Test
    void testCheckFiles_failWrongType() {

        StoredFile goodFile = mock(StoredFile.class);
        when(goodFile.getContentType()).thenReturn("text/csv");

        StoredFile badFile = mock(StoredFile.class);
        when(badFile.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
        when(badFile.getOriginalFilename()).thenReturn("badFile.txt");

        List<StoredFile> files = List.of(goodFile, badFile);

        assertThrows(
                BadRequestException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        fileSystemService,
                        "checkFiles",
                        files
                )
        );
    }

    @Test
    void testGetFilenameFromUrl_allCases() {
        assertEquals("file.txt", fileSystemService.getFilenameFromUrl("/path/to/file.txt", 0));
        assertEquals("to/file.txt", fileSystemService.getFilenameFromUrl("/path/to/file.txt", 1));
        assertEquals("/path/to/file.txt", fileSystemService.getFilenameFromUrl("/path/to/file.txt", 2));
        assertEquals("/path/to/file.txt", fileSystemService.getFilenameFromUrl("/path/to/file.txt", 3));
        assertEquals("/path/to/file.txt", fileSystemService.getFilenameFromUrl("/path/to/file.txt", -1));

        assertEquals("file.txt", fileSystemService.getFilenameFromUrl("file.txt", 0));
        assertEquals("file.txt", fileSystemService.getFilenameFromUrl("file.txt", 1));
    }


    @Test
    void testDeleteFile_Success() throws Exception {
        String organization = "user";
        Long workId = 1L;
        FileFolder folder = FileFolder.INPUT;
        String fileUrl = "url/file.txt";
        String fileName = "file.txt";
        String expectedPath = "/input/file.txt";

        // Spy getFilenameFromUrl if needed
        FileSystemService spyService = spy(fileSystemService);
        doReturn(fileName).when(spyService).getFilenameFromUrl(fileUrl, 0);

        when(fileSystem.mount(organization, workId.toString())).thenReturn(fileStorage);
        when(fileStorage.getFileUrl(folder, fileName)).thenReturn(expectedPath);

        String result = spyService.deleteFile(organization, workId, folder, fileUrl);

        assertEquals(expectedPath, result);
        verify(fileStorage).delete(folder, fileName);
    }

    @Test
    void testDeleteFile_FileNotFound() throws Exception {
        String organization = "user";
        Long workId = 1L;
        FileFolder folder = FileFolder.INPUT;
        String fileUrl = "url/file.txt";
        String fileName = "file.txt";
        String expectedPath = "/input/file.txt";
        HttpResponse mockResponse = mock(HttpResponse.class);

        FileSystemService spyService = spy(fileSystemService);
        doReturn(fileName).when(spyService).getFilenameFromUrl(fileUrl, 0);

        when(fileSystem.mount(organization, workId.toString())).thenReturn(fileStorage);
        when(fileStorage.getFileUrl(folder, fileName)).thenReturn(expectedPath);

        when(mockResponse.getStatusCode()).thenReturn(404);
        doThrow(new BlobStorageException("Not found", mockResponse, null)).when(fileStorage).delete(folder, fileName);

        String result = spyService.deleteFile(organization, workId, folder, fileUrl);

        assertEquals(expectedPath, result);
    }

    @Test
    void testDeleteFile_OtherException() throws Exception {
        String organization = "user";
        Long workId = 1L;
        FileFolder folder = FileFolder.INPUT;
        String fileUrl = "url/file.txt";
        String fileName = "file.txt";
        String expectedPath = "/input/file.txt";

        FileSystemService spyService = spy(fileSystemService);
        doReturn(fileName).when(spyService).getFilenameFromUrl(fileUrl, 0);

        when(fileSystem.mount(organization, workId.toString())).thenReturn(fileStorage);
        when(fileStorage.getFileUrl(folder, fileName)).thenReturn(expectedPath);
        doThrow(new IOException("IO Error")).when(fileStorage).delete(folder, fileName);

        String result = spyService.deleteFile(organization, workId, folder, fileUrl);

        assertEquals(expectedPath, result);
    }

    @Test
    void testGetBufferedReader_Utf8Ok() throws Exception {

        Path tempFile = Files.createTempFile("utf8", ".csv");
        Files.writeString(tempFile, "hello world", StandardCharsets.UTF_8);

        StoredFile storedFile = mock(StoredFile.class);
        when(storedFile.getPath()).thenReturn(tempFile);

        BufferedReader reader =
                ReflectionTestUtils.invokeMethod(
                        FileSystemService.class,
                        "getBufferedReader",
                        storedFile);

        assertNotNull(reader);
        assertEquals("hello world", reader.readLine());

        Files.deleteIfExists(tempFile);
    }

    @Test
    void testListFiles_defaultFolder() throws Exception {
        ReflectionTestUtils.setField(fileSystemService, "fileDescriptionRestMapper", fileDescriptionRestMapper);

        when(fileSystem.mount("org", "1")).thenReturn(fileStorage);
        when(fileStorage.listFiles(FileFolder.INPUT)).thenReturn(List.of());
        when(fileDescriptionRestMapper.toDto(List.of())).thenReturn(List.of());

        List<FileDescriptionRest> result = fileSystemService.listFiles("org", 1L);

        assertNotNull(result);
        verify(fileSystem).mount("org", "1");
        verify(fileStorage).listFiles(FileFolder.INPUT);
    }

    @Test
    void testListFiles_customFolder() throws Exception {
        ReflectionTestUtils.setField(fileSystemService, "fileDescriptionRestMapper", fileDescriptionRestMapper);

        when(fileSystem.mount("org", "1")).thenReturn(fileStorage);
        when(fileStorage.listFiles(FileFolder.OUTPUT)).thenReturn(List.of());
        when(fileDescriptionRestMapper.toDto(List.of())).thenReturn(List.of());

        List<FileDescriptionRest> result =
                fileSystemService.listFiles("org", 1L, FileFolder.OUTPUT);

        assertNotNull(result);
        verify(fileSystem).mount("org", "1");
        verify(fileStorage).listFiles(FileFolder.OUTPUT);
    }

    @Test
    void testListTemplatesFiles() throws Exception {
        ReflectionTestUtils.setField(fileSystemService, "fileDescriptionRestMapper", fileDescriptionRestMapper);

        when(fileSystem.mount(Constants.INTERNAL_ORGANIZATION, String.valueOf(Constants.INTERNAL_WORKSPACE)))
                .thenReturn(fileStorage);

        when(fileStorage.listFiles(FileFolder.TEMPLATES)).thenReturn(List.of());
        when(fileDescriptionRestMapper.toDto(List.of())).thenReturn(List.of());

        List<FileDescriptionRest> result = fileSystemService.listTemplatesFiles();

        assertNotNull(result);
        verify(fileStorage).listFiles(FileFolder.TEMPLATES);
    }

    @Test
    void testFetchStorage_success() {
        when(fileSystem.mount("org", "1")).thenReturn(fileStorage);

        FileStorage storage = ReflectionTestUtils.invokeMethod(
                fileSystemService, "fetchStorage", "org", "1");

        assertEquals(fileStorage, storage);
    }

    @Test
    void testFetchStorage_notFound() throws Exception {
        when(fileSystem.mount("org", "1")).thenReturn(null);

        Method method = FileSystemService.class.getDeclaredMethod(
                "fetchStorage", String.class, String.class
        );
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(fileSystemService, "org", "1")
        );

        // unwrap the real exception thrown by the private method
        assertTrue(exception.getCause() instanceof ResponseStatusException);
    }

    @Test
    void testManageFilesAndRename_nullFiles() {
        List<String> result = fileSystemService.manageFilesAndRename(
                "org", 1L, null, List.of(), true);

        assertTrue(result.isEmpty());
    }

    @Test
    void testManageFilesAndRename_success() throws Exception {

        String tempDir = System.getProperty("java.io.tmpdir");

        ReflectionTestUtils.setField(
                fileSystemService,
                "localWorkingFolder",
                tempDir);

        Files.createDirectories(
                Path.of(tempDir, "input", "inventory"));

        Path csvFile = Files.createTempFile("inventory", ".csv");

        Files.writeString(
                csvFile,
                "col1,col2\nv1,v2",
                StandardCharsets.UTF_8);

        StoredFile storedFile = mock(StoredFile.class);

        when(storedFile.getOriginalFilename()).thenReturn("a.csv");
        when(storedFile.getContentType()).thenReturn("text/csv");
        when(storedFile.getPath()).thenReturn(csvFile);

        when(fileSystem.mount("org", "1"))
                .thenReturn(fileStorage);

        when(fileStorage.upload(
                eq(FileFolder.INPUT),
                eq("newname.csv"),
                eq("a.csv"),
                any(InputStream.class)
        )).thenReturn("uploaded.csv");

        List<String> result =
                fileSystemService.manageFilesAndRename(
                        "org",
                        1L,
                        List.of(storedFile),
                        List.of("newname.csv"),
                        true);

        assertEquals(1, result.size());
        assertEquals("uploaded.csv", result.get(0));

        verify(fileStorage).upload(
                eq(FileFolder.INPUT),
                eq("newname.csv"),
                eq("a.csv"),
                any(InputStream.class)
        );

        Files.deleteIfExists(csvFile);
    }

    @Test
    void testManageFilesAndRename_xlsx() throws Exception {

        String tempDir = System.getProperty("java.io.tmpdir");

        ReflectionTestUtils.setField(
                fileSystemService,
                "localWorkingFolder",
                tempDir);

        Files.createDirectories(
                Path.of(tempDir, "input", "inventory"));

        Path xlsxFile = Files.createTempFile("inventory", ".xlsx");

        Files.write(xlsxFile, "dummy".getBytes());

        StoredFile storedFile = mock(StoredFile.class);

        when(storedFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(storedFile.getContentType()).thenReturn(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        when(storedFile.getPath()).thenReturn(xlsxFile);

        when(fileSystem.mount("org", "1"))
                .thenReturn(fileStorage);

        when(fileStorage.upload(
                eq(FileFolder.INPUT),
                eq("renamed.xlsx"),
                eq("test.xlsx"),
                any(InputStream.class)
        )).thenReturn("uploaded.xlsx");

        List<String> result =
                fileSystemService.manageFilesAndRename(
                        "org",
                        1L,
                        List.of(storedFile),
                        List.of("renamed.xlsx"),
                        true);

        assertEquals(List.of("uploaded.xlsx"), result);

        Files.deleteIfExists(xlsxFile);
    }


}