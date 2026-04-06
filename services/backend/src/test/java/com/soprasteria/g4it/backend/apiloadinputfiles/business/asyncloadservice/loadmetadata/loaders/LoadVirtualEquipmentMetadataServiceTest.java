/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckVirtualEquipment;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
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
class LoadVirtualEquipmentMetadataServiceTest {
    @Mock
    private CheckVirtualEquipmentRepository checkVirtualEquipmentRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadVirtualEquipmentMetadataService service;


    @Test
    void testExecute_withValidObjects_savesEntities() {
        Context context = mock(Context.class);
        when(context.getTaskId()).thenReturn(24L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("file.csv");
        InVirtualEquipmentRest inVirtualEquipmentRest = mock(InVirtualEquipmentRest.class);
        when(inVirtualEquipmentRest.getName()).thenReturn("virt-eq");
        when(inVirtualEquipmentRest.getPhysicalEquipmentName()).thenReturn("phys-eq");
        when(inVirtualEquipmentRest.getInfrastructureType()).thenReturn("infra");
        List<Object> objects = new ArrayList<>();
        objects.add(inVirtualEquipmentRest);
        service.execute(context, fileToLoad, 0, objects);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CheckVirtualEquipment>> captor = ArgumentCaptor.forClass(List.class);
        verify(checkVirtualEquipmentRepository, times(1)).saveAll(captor.capture());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
        List<CheckVirtualEquipment> saved = captor.getValue();
        assertEquals(1, saved.size());
        CheckVirtualEquipment eq = saved.get(0);
        assertEquals("virt-eq", eq.getVirtualEquipmentName());
        assertEquals("phys-eq", eq.getPhysicalEquipmentName());
        assertEquals("infra", eq.getInfrastructureType());
        assertEquals("file.csv", eq.getFileName());
        assertEquals(24L, eq.getTaskId()); // Fixed: compare Long to Long
        assertEquals(2, eq.getLineNumber());
        assertNotNull(eq.getCreationDate());
    }

    @Test
    void testExecute_withEmptyObjects_savesNothing() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = Collections.emptyList();

        service.execute(context, fileToLoad, 0, objects);

        verify(checkVirtualEquipmentRepository, times(1)).saveAll(Collections.emptyList());
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
    }

    @Test
    void testExecute_withInvalidObject_throwsClassCastException() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<Object> objects = new ArrayList<>();
        objects.add("not an InVirtualEquipmentRest");
        assertThrows(ClassCastException.class, () ->
                service.execute(context, fileToLoad, 0, objects)
        );
        verify(checkVirtualEquipmentRepository, never()).saveAll(any());
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }
}
