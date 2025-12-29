package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.FileConversionService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class FileLoadingUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFailWhenFilePathEscapesBaseDirectory() throws Exception {

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

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> utils.convertAllFileToLoad(context)
        );

        assertTrue(exception.getMessage().contains("Invalid file path"));
        verifyNoInteractions(conversionService);
    }
}
