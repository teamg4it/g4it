/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckApplication;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
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
class LoadApplicationMetadataServiceTest {
    @Mock
    private CheckApplicationRepository checkApplicationRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadApplicationMetadataService service;

    @Test
    void testExecute_withValidObjects_savesEntities() {
        Context context = mock(Context.class);
        when(context.getTaskId()).thenReturn(99L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("file.csv");
        InApplicationRest inApplicationRest = mock(InApplicationRest.class);
        when(inApplicationRest.getName()).thenReturn("app1");
        when(inApplicationRest.getEnvironment()).thenReturn("env1");
        when(inApplicationRest.getVirtualEquipmentName()).thenReturn("virt-eq1");
        List<Object> objects = new ArrayList<>();
        objects.add(inApplicationRest);
        service.execute(context, fileToLoad, 0, objects);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CheckApplication>> captor = ArgumentCaptor.forClass(List.class);
        verify(checkApplicationRepository, times(1)).saveAll(captor.capture());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
        List<CheckApplication> saved = captor.getValue();
        assertEquals(1, saved.size());
        CheckApplication app = saved.get(0);
        assertEquals("app1", app.getApplicationName());
        assertEquals("env1", app.getEnvironmentType());
        assertEquals("virt-eq1", app.getVirtualEquipmentName());
        assertEquals("file.csv", app.getFileName());
        assertEquals(99L, app.getTaskId());
        assertEquals(2, app.getLineNumber());
        assertNotNull(app.getCreationDate());
    }

    @Test
    void testExecute_withEmptyObjects_savesNothing() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = Collections.emptyList();

        service.execute(context, fileToLoad, 0, objects);

        verify(checkApplicationRepository, times(1)).saveAll(Collections.emptyList());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
    }

    @Test
    void testExecute_withInvalidObject_throwsClassCastException() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = new ArrayList<>();
        objects.add("not an InApplicationRest");
        assertThrows(ClassCastException.class, () ->
                service.execute(context, fileToLoad, 0, objects)
        );
        verify(checkApplicationRepository, never()).saveAll(any());
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }
}
