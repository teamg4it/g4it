package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.FileConversionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileLoadingUtilsTest {

    @TempDir
    Path tempDir;

    /* -------------------------------------------------
     * Helper: inject private field
     * ------------------------------------------------- */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    /* -------------------------------------------------
     * getOriginalFilename()
     * ------------------------------------------------- */
    @Test
    void shouldExtractOriginalFilenameUsingExistingEnum() {
        FileLoadingUtils utils = new FileLoadingUtils();

        FileType anyType = FileType.values()[0]; // ✅ safe, always exists
        String prefix = anyType.name();

        String result = utils.getOriginalFilename(
                anyType,
                prefix + "_sample.csv"
        );

        assertEquals("sample.csv", result);
    }

    @Test
    void shouldReturnEmptyWhenFilenameDoesNotMatchPattern() {
        FileLoadingUtils utils = new FileLoadingUtils();

        FileType anyType = FileType.values()[0];

        String result = utils.getOriginalFilename(anyType, "random.csv");

        assertEquals("", result);
    }

    /* -------------------------------------------------
     * computeRejectedFolderPath()
     * ------------------------------------------------- */
    @Test
    void shouldComputeRejectedFolderPath() throws Exception {
        FileLoadingUtils utils = new FileLoadingUtils();
        setField(utils, "localWorkingFolder", tempDir.toString());

        Path path = utils.computeRejectedFolderPath("123");

        assertTrue(path.endsWith("rejected/123"));
    }

    /* -------------------------------------------------
     * mapFileToLoad()
     * ------------------------------------------------- */
    @Test
    void shouldMapFilesForInventory() throws Exception {
        FileLoadingUtils utils = new FileLoadingUtils();
        setField(utils, "localWorkingFolder", tempDir.toString());

        List<FileToLoad> result = utils.mapFileToLoad(
                List.of("TEST_file.csv"),
                true
        );

        assertEquals(1, result.size());
        assertEquals("TEST_file.csv", result.get(0).getFilename());
        assertNotNull(result.get(0).getFileType());
        assertTrue(result.get(0).getFilePath().toString().contains("input"));
    }

    /* -------------------------------------------------
     * convertAllFileToLoad() – safe normalized path
     * ------------------------------------------------- */
    @Test
    void shouldHandleNormalizedPathSafely() throws Exception {
        FileLoadingUtils utils = new FileLoadingUtils();
        FileConversionService conversionService = mock(FileConversionService.class);

        setField(utils, "fileConversionService", conversionService);

        FileToLoad file = new FileToLoad();
        file.setOriginalFileName("ok.csv");
        file.setFilePath(
                tempDir.resolve("input").resolve("..").resolve("ok.csv")
        );

        Context context = Context.builder()
                .workspaceId(1L)
                .filesToLoad(List.of(file))
                .build();

        assertDoesNotThrow(() -> utils.convertAllFileToLoad(context));
    }

    /* -------------------------------------------------
     * convertAllFileToLoad() – IOException wrapped
     * ------------------------------------------------- */
    @Test
    void shouldWrapIOExceptionIntoAsyncTaskException() throws Exception {
        FileLoadingUtils utils = new FileLoadingUtils();
        FileConversionService conversionService = mock(FileConversionService.class);

        setField(utils, "fileConversionService", conversionService);

        when(conversionService.convertFileToCsv(any(), any()))
                .thenThrow(new IOException("boom"));

        FileToLoad file = new FileToLoad();
        file.setOriginalFileName("file.csv");
        file.setFilePath(tempDir.resolve("file.csv"));

        Context context = Context.builder()
                .workspaceId(1L) // REQUIRED to avoid NPE in context.log()
                .filesToLoad(List.of(file))
                .build();

        assertThrows(
                AsyncTaskException.class,
                () -> utils.convertAllFileToLoad(context)
        );
    }
}
