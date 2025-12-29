package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.FileConversionService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileLoadingUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldHandleNormalizedPathSafely() throws Exception {

        FileLoadingUtils utils = new FileLoadingUtils();
        FileConversionService conversionService = mock(FileConversionService.class);

        Field conversionField =
                FileLoadingUtils.class.getDeclaredField("fileConversionService");
        conversionField.setAccessible(true);
        conversionField.set(utils, conversionService);

        FileToLoad fileToLoad = new FileToLoad();
        fileToLoad.setOriginalFileName("badfile.csv");

        fileToLoad.setFilePath(
                tempDir.resolve("input").resolve("..").resolve("badfile.csv")
        );

        Context context = Context.builder()
                .filesToLoad(List.of(fileToLoad))
                .build();

        // should NOT throw
        assertDoesNotThrow(() -> utils.convertAllFileToLoad(context));
    }

    @Test
    void shouldWrapIOExceptionIntoAsyncTaskException() throws Exception {

        FileLoadingUtils utils = new FileLoadingUtils();
        FileConversionService conversionService = mock(FileConversionService.class);

        Field field = FileLoadingUtils.class.getDeclaredField("fileConversionService");
        field.setAccessible(true);
        field.set(utils, conversionService);

        FileToLoad fileToLoad = new FileToLoad();
        fileToLoad.setOriginalFileName("file.csv");
        fileToLoad.setFilePath(tempDir.resolve("file.csv"));

        when(conversionService.convertFileToCsv(any(), any()))
                .thenThrow(new IOException("boom"));

        Context context = Context.builder()
                .filesToLoad(List.of(fileToLoad))
                .build();

        assertThrows(
                AsyncTaskException.class,
                () -> utils.convertAllFileToLoad(context)
        );
    }

}