/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckDatacenter;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadDatacenterMetadataServiceTest {
    @Mock
    private CheckDatacenterRepository checkDatacenterRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadDatacenterMetadataService service;

    @Test
    void testExecute_withValidObjects_savesEntities() {
        Context context = mock(Context.class);
        when(context.getTaskId()).thenReturn(123L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("file.csv");
        InDatacenterRest inDatacenterRest = mock(InDatacenterRest.class);
        when(inDatacenterRest.getName()).thenReturn("dc1");
        List<Object> objects = new ArrayList<>();
        objects.add(inDatacenterRest);
        service.execute(context, fileToLoad, 0, objects);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CheckDatacenter>> captor = ArgumentCaptor.forClass(List.class);
        verify(checkDatacenterRepository, times(1)).saveAll(captor.capture());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
        List<CheckDatacenter> saved = captor.getValue();
        assertEquals(1, saved.size());
        CheckDatacenter dc = saved.get(0);
        assertEquals("dc1", dc.getDatacenterName());
        assertEquals("file.csv", dc.getFileName());
        assertEquals(123L, dc.getTaskId());
        assertEquals(2, dc.getLineNumber());
        assertNotNull(dc.getCreationDate());
    }

    @Test
    void testExecute_withEmptyObjects_savesNothing() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = Collections.emptyList();

        service.execute(context, fileToLoad, 0, objects);

        verify(checkDatacenterRepository, times(1)).saveAll(Collections.emptyList());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
    }

    @Test
    void testExecute_withInvalidObject_throwsClassCastException() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = new ArrayList<>();
        objects.add("not an InDatacenterRest");
        assertThrows(ClassCastException.class, () ->
                service.execute(context, fileToLoad, 0, objects)
        );
        verify(checkDatacenterRepository, never()).saveAll(any());
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }
}
