/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncLoadMetadataServiceTest {

    @InjectMocks
    private AsyncLoadMetadataService asyncLoadMetadataService;

    @Mock
    private LoadMetadataService loadMetadataService;

    @Mock
    private TaskExecutor metadataTaskExecutor;

    @Mock
    private Context context;

    @Mock
    private FileToLoad fileToLoad1;
    @Mock
    private FileToLoad fileToLoad2;

    @Test
    void shouldLoadMetadataForEachFile() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(metadataTaskExecutor).execute(any(Runnable.class));

        when(context.log()).thenReturn("context-log");
        when(context.getFilesToLoad()).thenReturn(Arrays.asList(fileToLoad1, fileToLoad2));
        when(fileToLoad1.getFilename()).thenReturn("file1.csv");
        when(fileToLoad2.getFilename()).thenReturn("file2.csv");

        asyncLoadMetadataService.loadInputMetadata(context);

        verify(loadMetadataService, times(1)).loadMetadataFile(fileToLoad1, context);
        verify(loadMetadataService, times(1)).loadMetadataFile(fileToLoad2, context);
    }

    @Test
    void shouldPropagateAsyncTaskExceptionWhenOneFileFails() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(metadataTaskExecutor).execute(any(Runnable.class));

        when(context.log()).thenReturn("context-log");
        when(context.getFilesToLoad()).thenReturn(Arrays.asList(fileToLoad1, fileToLoad2));
        when(fileToLoad1.getFilename()).thenReturn("file1.csv");
        when(fileToLoad2.getFilename()).thenReturn("file2.csv");
        doAnswer(invocation -> {
            final FileToLoad fileToLoad = invocation.getArgument(0);
            if (fileToLoad == fileToLoad1) {
                throw new AsyncTaskException("fail");
            }
            return null;
        }).when(loadMetadataService).loadMetadataFile(any(FileToLoad.class), eq(context));

        assertThrows(AsyncTaskException.class, () -> asyncLoadMetadataService.loadInputMetadata(context));

        verify(loadMetadataService, times(1)).loadMetadataFile(fileToLoad1, context);
        verify(loadMetadataService, times(1)).loadMetadataFile(fileToLoad2, context);
    }

    @Test
    void shouldNotLoadMetadataWhenNoFiles() {
        when(context.log()).thenReturn("context-log");
        when(context.getFilesToLoad()).thenReturn(Collections.emptyList());

        asyncLoadMetadataService.loadInputMetadata(context);

        verify(loadMetadataService, never()).loadMetadataFile(any(), any());
    }

    @Test
    void shouldPreserveInterruptedFlagWhenThreadIsInterruptedDuringAwaitTermination() {
        when(context.log()).thenReturn("context-log");
        when(context.getFilesToLoad()).thenReturn(Collections.emptyList());

        Thread.currentThread().interrupt();
        try {
            asyncLoadMetadataService.loadInputMetadata(context);
            assertTrue(Thread.currentThread().isInterrupted());
            verify(loadMetadataService, never()).loadMetadataFile(any(), any());
        } finally {
            // Clear interrupted status so this test does not affect following tests.
            final boolean interruptedStatusCleared = Thread.interrupted();
            assertTrue(interruptedStatusCleared || !Thread.currentThread().isInterrupted());
        }
    }
}
