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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncLoadMetadataServiceTest {

    @InjectMocks
    private AsyncLoadMetadataService asyncLoadMetadataService;

    @Mock
    private LoadMetadataService loadMetadataService;

    @Mock
    private Context context;

    @Mock
    private FileToLoad fileToLoad1;
    @Mock
    private FileToLoad fileToLoad2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadInputMetadata_Success() {
        when(context.log()).thenReturn("context-log");
        when(context.getFilesToLoad()).thenReturn(Arrays.asList(fileToLoad1, fileToLoad2));
        when(fileToLoad1.getFilename()).thenReturn("file1.csv");
        when(fileToLoad2.getFilename()).thenReturn("file2.csv");
        doNothing().when(loadMetadataService).loadMetadataFile(any(), eq(context));

        asyncLoadMetadataService.loadInputMetadata(context);
        verify(loadMetadataService, atLeastOnce()).loadMetadataFile(any(), eq(context));
    }

    @Test
    void testLoadInputMetadata_ExceptionHandled() {
        when(context.log()).thenReturn("context-log");
        when(context.getFilesToLoad()).thenReturn(Collections.singletonList(fileToLoad1));
        when(fileToLoad1.getFilename()).thenReturn("file1.csv");
        doThrow(new RuntimeException("fail")).when(loadMetadataService).loadMetadataFile(any(), eq(context));

        asyncLoadMetadataService.loadInputMetadata(context);
        verify(loadMetadataService, atLeastOnce()).loadMetadataFile(any(), eq(context));
    }

    @Test
    void testLoadInputMetadata_NoFiles() {
        when(context.log()).thenReturn("context-log");
        when(context.getFilesToLoad()).thenReturn(Collections.emptyList());
        asyncLoadMetadataService.loadInputMetadata(context);
        verify(loadMetadataService, never()).loadMetadataFile(any(), any());
    }
}
