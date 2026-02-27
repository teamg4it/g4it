/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckPhysicalEquipmentService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadPhysicalEquipmentServiceTest {
    @Mock
    private CheckPhysicalEquipmentService checkPhysicalEquipmentService;
    @Mock
    private InPhysicalEquipmentMapper inPhysicalEquipmentMapper;
    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadPhysicalEquipmentService service;

    @Test
    void testExecuteEmptyPhysicalEquipments() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<InPhysicalEquipmentRest> physicalEquipments = Collections.emptyList();
        List<LineError> result = service.execute(context, fileToLoad, 0, physicalEquipments);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExecuteValidPhysicalEquipments() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(1L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(Collections.emptyMap());
        InPhysicalEquipmentRest peRest = mock(InPhysicalEquipmentRest.class);
        when(peRest.getName()).thenReturn("pe1");
        List<InPhysicalEquipmentRest> physicalEquipments = List.of(peRest);
        when(checkPhysicalEquipmentService.checkRules(any(), any(), any(), anyInt())).thenReturn(Collections.emptyList());
        InPhysicalEquipment peEntity = mock(InPhysicalEquipment.class);
        when(inPhysicalEquipmentMapper.toEntity(peRest)).thenReturn(peEntity);
        doNothing().when(inPhysicalEquipmentRepository).deleteByInventoryIdAndNameIn(anyLong(), any());
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        service.execute(context, fileToLoad, 0, physicalEquipments);
        verify(inPhysicalEquipmentRepository).deleteByInventoryIdAndNameIn(eq(1L), any());
        verify(inPhysicalEquipmentRepository).saveAll(any());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testExecuteWithErrors() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(1L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        Map<Integer, List<LineError>> coherenceErrors = new HashMap<>();
        coherenceErrors.put(2, List.of(mock(LineError.class)));
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(coherenceErrors);
        InPhysicalEquipmentRest peRest = mock(InPhysicalEquipmentRest.class);
        when(peRest.getName()).thenReturn("pe1");
        List<InPhysicalEquipmentRest> physicalEquipments = List.of(peRest);
        List<LineError> checkErrors = List.of(mock(LineError.class));
        when(checkPhysicalEquipmentService.checkRules(any(), any(), any(), anyInt())).thenReturn(checkErrors);
        service.execute(context, fileToLoad, 0, physicalEquipments);
    }

    @Test
    void testExecuteWithNullInventoryId() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(Collections.emptyMap());
        InPhysicalEquipmentRest peRest = mock(InPhysicalEquipmentRest.class);
        when(peRest.getName()).thenReturn("pe1");
        List<InPhysicalEquipmentRest> physicalEquipments = List.of(peRest);
        when(checkPhysicalEquipmentService.checkRules(any(), any(), any(), anyInt())).thenReturn(Collections.emptyList());
        InPhysicalEquipment peEntity = mock(InPhysicalEquipment.class);
        when(inPhysicalEquipmentMapper.toEntity(peRest)).thenReturn(peEntity);
        doNothing().when(inPhysicalEquipmentRepository).deleteByDigitalServiceVersionUidAndNameIn(anyString(), any());
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        service.execute(context, fileToLoad, 0, physicalEquipments);
        verify(inPhysicalEquipmentRepository).deleteByDigitalServiceVersionUidAndNameIn(eq("uid"), any());
        verify(inPhysicalEquipmentRepository).saveAll(any());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testGetPhysicalEquipmentCount() {
        when(inPhysicalEquipmentRepository.sumQuantityByInventoryId(1L)).thenReturn(5L);
        Long result = service.getPhysicalEquipmentCount(1L);
        assertEquals(5L, result);
        verify(inPhysicalEquipmentRepository).sumQuantityByInventoryId(1L);
    }
}
