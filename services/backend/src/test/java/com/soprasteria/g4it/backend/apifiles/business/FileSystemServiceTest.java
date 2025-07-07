/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apifiles.business;

import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileSystemServiceTest {

    @InjectMocks
    FileSystemService fileSystemService;
    @Mock
    MultipartFile file;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private FileStorage fileStorage;

    @Test
    void testCheckFiles() {

        // check empty and null
        ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", List.of());
        ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", (Object) null);

        List<MultipartFile> okFiles = List.of(new MockMultipartFile(
                "goodFile",
                "goodFile.txt",
                "text/csv",
                "goodFile!".getBytes()
        ));
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", okFiles));
    }

    @Test
    void testCheckFiles_failWrongType() {
        List<MultipartFile> failPlainTextFiles = List.of(
                new MockMultipartFile(
                        "goodFile",
                        "goodFile.txt",
                        "text/csv",
                        "goodFile!".getBytes()
                ),
                new MockMultipartFile(
                        "badFile",
                        "badFile.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "badFile!".getBytes()
                ));
        assertThrows(BadRequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(fileSystemService, "checkFiles", failPlainTextFiles);
        });
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
        String subscriber = "user";
        Long orgId = 1L;
        FileFolder folder = FileFolder.INPUT;
        String fileUrl = "url/file.txt";
        String fileName = "file.txt";
        String expectedPath = "/input/file.txt";

        // Spy getFilenameFromUrl if needed
        FileSystemService spyService = Mockito.spy(fileSystemService);
        doReturn(fileName).when(spyService).getFilenameFromUrl(fileUrl, 0);

        when(fileSystem.mount(subscriber, orgId.toString())).thenReturn(fileStorage);
        when(fileStorage.getFileUrl(folder, fileName)).thenReturn(expectedPath);

        String result = spyService.deleteFile(subscriber, orgId, folder, fileUrl);

        assertEquals(expectedPath, result);
        verify(fileStorage).delete(folder, fileName);
    }

    @Test
    void testDeleteFile_OtherException() throws Exception {
        String subscriber = "user";
        Long orgId = 1L;
        FileFolder folder = FileFolder.INPUT;
        String fileUrl = "url/file.txt";
        String fileName = "file.txt";
        String expectedPath = "/input/file.txt";
        final String filePath = String.join("/", subscriber, orgId.toString(), FileFolder.INPUT.getFolderName(), fileName);

        FileSystemService spyService = Mockito.spy(fileSystemService);
        doReturn(fileName).when(spyService).getFilenameFromUrl(fileUrl, 0);

        when(fileSystem.mount(subscriber, orgId.toString())).thenReturn(fileStorage);
        when(fileStorage.getFileUrl(folder, fileName)).thenReturn(expectedPath);
        doThrow(new IOException("IO Error")).when(fileStorage).delete(folder, fileName);

        String result = spyService.deleteFile(subscriber, orgId, folder, fileUrl);

        assertEquals(expectedPath, result);
    }
}

