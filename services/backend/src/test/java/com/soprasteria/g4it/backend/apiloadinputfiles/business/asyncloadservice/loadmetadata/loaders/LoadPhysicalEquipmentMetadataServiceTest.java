/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckPhysicalEquipment;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
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
class LoadPhysicalEquipmentMetadataServiceTest {
    @Mock
    private CheckPhysicalEquipmentRepository checkPhysicalEquipmentRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadPhysicalEquipmentMetadataService service;


    @Test
    void testExecute_withValidObjects_savesEntities() {
        Context context = mock(Context.class);
        when(context.getTaskId()).thenReturn(42L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("file.csv");
        InPhysicalEquipmentRest inPhysicalEquipmentRest = mock(InPhysicalEquipmentRest.class);
        when(inPhysicalEquipmentRest.getName()).thenReturn("phys-eq");
        when(inPhysicalEquipmentRest.getType()).thenReturn("type1");
        when(inPhysicalEquipmentRest.getDatacenterName()).thenReturn("dc1");
        List<Object> objects = new ArrayList<>();
        objects.add(inPhysicalEquipmentRest);
        service.execute(context, fileToLoad, 0, objects);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CheckPhysicalEquipment>> captor = ArgumentCaptor.forClass(List.class);
        verify(checkPhysicalEquipmentRepository, times(1)).saveAll(captor.capture());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
        List<CheckPhysicalEquipment> saved = captor.getValue();
        assertEquals(1, saved.size());
        CheckPhysicalEquipment eq = saved.get(0);
        assertEquals("phys-eq", eq.getPhysicalEquipmentName());
        assertEquals("type1", eq.getType());
        assertEquals("dc1", eq.getDatacenterName());
        assertEquals("file.csv", eq.getFileName());
        assertEquals(42L, eq.getTaskId());
        assertEquals(2, eq.getLineNumber());
        assertNotNull(eq.getCreationDate());
    }

    @Test
    void testExecute_withEmptyObjects_savesNothing() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = Collections.emptyList();

        service.execute(context, fileToLoad, 0, objects);

        verify(checkPhysicalEquipmentRepository, times(1)).saveAll(Collections.emptyList());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
    }

    @Test
    void testExecute_withInvalidObject_throwsClassCastException() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = new ArrayList<>();
        objects.add("not an InPhysicalEquipmentRest");
        assertThrows(ClassCastException.class, () ->
                service.execute(context, fileToLoad, 0, objects)
        );
        verify(checkPhysicalEquipmentRepository, never()).saveAll(any());
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }
}
