package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.FileConversionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileLoadingUtilsTest {

    @TempDir
    Path tempDir;

    @Mock
    private FileSystemService fileSystemService;

    @InjectMocks
    private FileLoadingUtils loadFileService;

    @Mock
    private Context context;

    @Mock
    private FileToLoad fileToLoad;

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

        context = Context.builder()
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

        context = Context.builder()
                .workspaceId(1L) // REQUIRED to avoid NPE in context.log()
                .filesToLoad(List.of(file))
                .build();

        assertThrows(
                AsyncTaskException.class,
                () -> utils.convertAllFileToLoad(context)
        );
    }

    @Test
    void downloadAllFileToLoad_success_writesFile() throws Exception {
        Path dest = tempDir.resolve("downloaded.csv");
        // ensure parent exists
        Files.createDirectories(dest.getParent());

        when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));
        when(fileToLoad.getFilename()).thenReturn("downloaded.csv");
        when(fileToLoad.getFilePath()).thenReturn(dest);

        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(bytes);

        // match any org/workspace/filefolder since those are provided from context in real code
        when(fileSystemService.downloadFile(any(), any(), any(), eq("downloaded.csv"))).thenReturn(is);

        loadFileService.downloadAllFileToLoad(context);

        assertTrue(Files.exists(dest), "Downloaded file must exist");
        String content = Files.readString(dest, StandardCharsets.UTF_8);
        assertEquals("hello", content);
    }

    @Test
    void downloadAllFileToLoad_whenDownloadFails_throwsAsyncTaskException() throws Exception {
        when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));

        // Only stub methods that are called before the exception is thrown
        when(fileSystemService.downloadFile(any(), any(), any(), anyString()))
                .thenThrow(new IOException("download error"));

        AsyncTaskException ex = assertThrows(AsyncTaskException.class,
                () -> loadFileService.downloadAllFileToLoad(context));

        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof Exception);
        assertTrue(ex.getMessage().contains("Cannot download file"));
    }

    @Test
    void cleanConvertedFiles_shouldDeleteConvertedFiles() throws Exception {
        FileLoadingUtils utils = new FileLoadingUtils();

        // Create a temp file to act as the converted file
        Path convertedFile = Files.createTempFile("converted", ".csv");
        fileToLoad = new FileToLoad();
        fileToLoad.setOriginalFileName("converted.csv");
        fileToLoad.setConvertedFile(convertedFile.toFile());

        context = Context.builder()
                .filesToLoad(List.of(fileToLoad))
                .build();

        utils.cleanConvertedFiles(context);

        assertFalse(Files.exists(convertedFile), "Converted file should be deleted");
    }

    @Test
    void cleanConvertedFiles_shouldThrowAsyncTaskExceptionOnFailure() {
        FileLoadingUtils utils = new FileLoadingUtils();

        // Create a mock file that cannot be deleted (simulate by using a non-existent file)
        fileToLoad = new FileToLoad();
        fileToLoad.setOriginalFileName("fail.csv");
        fileToLoad.setConvertedFile(new java.io.File("non_existent_file.csv"));

        context = Context.builder()
                .workspaceId(1L) // Set required field to avoid NPE in context.log()
                .filesToLoad(List.of(fileToLoad))
                .build();

        assertThrows(AsyncTaskException.class, () -> utils.cleanConvertedFiles(context));
    }

}
