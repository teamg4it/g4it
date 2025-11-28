package com.soprasteria.g4it.backend.common.filesystem.business;

import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDeletionServiceTest {

    @InjectMocks
    private FileDeletionService fileDeletionService;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private FileStorage fileStorage;

    /**
     * helper to build FileDescription objects correctly
     */
    private FileDescription mockFile(String name, OffsetDateTime creationTime) {
        return FileDescription.builder()
                .name(name)
                .metadata(Map.of("creationTime", creationTime.toString()))
                .build();
    }

    @Test
    void deleteFiles_shouldDeleteOlderFiles() throws Exception {
        String org = "org1";
        String workspace = "ws1";
        int retentionDays = 5;
        OffsetDateTime now = OffsetDateTime.now();

        FileDescription oldFile = mockFile("ws1/input/old.csv", now.minusDays(10));     // should be deleted
        FileDescription recentFile = mockFile("ws1/input/recent.csv", now.minusDays(1)); // should NOT be deleted

        when(fileSystem.mount(org, workspace)).thenReturn(fileStorage);
        when(fileStorage.listFiles(FileFolder.INPUT))
                .thenReturn(List.of(oldFile, recentFile));

        // Always return same deleted path no matter how the key looks (/old.csv OR old.csv OR \old.csv)
        when(fileStorage.getFileUrl(eq(FileFolder.INPUT), anyString()))
                .thenReturn("deleted-path/old.csv");

        List<String> result = fileDeletionService.deleteFiles(
                org,
                workspace,
                FileFolder.INPUT,
                retentionDays
        );

        // Assertions
        assertEquals(1, result.size());
        assertEquals("deleted-path/old.csv", result.get(0));

        // Verify delete invoked once for old file only
        verify(fileStorage, times(1)).delete(eq(FileFolder.INPUT), anyString());
        verify(fileStorage, never()).delete(FileFolder.INPUT, "recent.csv");

        // Verify the fileStorage.getFileUrl call happened
        verify(fileStorage, times(1)).getFileUrl(eq(FileFolder.INPUT), anyString());
    }


    @Test
    void deleteFiles_shouldReturnEmpty_whenAllFilesAreRecent() throws Exception {
        String org = "org1";
        String workspace = "ws1";
        int retentionDays = 5;
        OffsetDateTime now = OffsetDateTime.now();

        FileDescription recentFile1 = mockFile("ws1/input/r1.csv", now.minusDays(2));
        FileDescription recentFile2 = mockFile("ws1/input/r2.csv", now.minusDays(1));

        when(fileSystem.mount(org, workspace)).thenReturn(fileStorage);
        when(fileStorage.listFiles(FileFolder.INPUT)).thenReturn(List.of(recentFile1, recentFile2));

        List<String> result = fileDeletionService.deleteFiles(org, workspace, FileFolder.INPUT, retentionDays);

        assertEquals(0, result.size());
        verify(fileStorage, never()).delete(any(), any());
    }

    @Test
    void deleteFiles_shouldNotDeleteAnything_whenListIsEmpty() throws Exception {
        String org = "org1";
        String workspace = "ws1";

        when(fileSystem.mount(org, workspace)).thenReturn(fileStorage);
        when(fileStorage.listFiles(FileFolder.INPUT)).thenReturn(List.of());

        List<String> result = fileDeletionService.deleteFiles(org, workspace, FileFolder.INPUT, 10);

        assertEquals(0, result.size());
        verify(fileStorage, never()).delete(any(), any());
    }

    @Test
    void deleteFiles_shouldNotThrow_whenIOExceptionOccurs() throws Exception {
        String org = "org1";
        String workspace = "ws1";

        when(fileSystem.mount(org, workspace)).thenReturn(fileStorage);
        when(fileStorage.listFiles(FileFolder.INPUT)).thenThrow(new IOException("boom"));

        List<String> result = fileDeletionService.deleteFiles(org, workspace, FileFolder.INPUT, 7);

        assertEquals(0, result.size());
        verify(fileStorage, never()).delete(any(), any());
    }
}