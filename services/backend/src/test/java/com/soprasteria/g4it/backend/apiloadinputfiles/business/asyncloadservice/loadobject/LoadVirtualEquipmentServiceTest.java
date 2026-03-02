/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckVirtualEquipmentService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadVirtualEquipmentServiceTest {
    @Mock
    private CheckVirtualEquipmentService checkVirtualEquipmentService;
    @Mock
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private BoaviztapiService boaviztapiService;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadVirtualEquipmentService service;

    @Test
    void testExecuteEmptyVirtualEquipments() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<InVirtualEquipmentRest> virtualEquipments = Collections.emptyList();
        List<LineError> result = service.execute(context, fileToLoad, 0, virtualEquipments);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExecuteValidVirtualEquipments() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(1L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(Collections.emptyMap());
        InVirtualEquipmentRest veRest = mock(InVirtualEquipmentRest.class);
        when(veRest.getName()).thenReturn("ve1");
        List<InVirtualEquipmentRest> virtualEquipments = List.of(veRest);
        when(checkVirtualEquipmentService.checkRules(any(), any(), any(), anyInt(), any())).thenReturn(Collections.emptyList());
        InVirtualEquipment veEntity = mock(InVirtualEquipment.class);
        when(inVirtualEquipmentMapper.toEntity(veRest)).thenReturn(veEntity);
        when(veEntity.getInfrastructureType()).thenReturn(InfrastructureType.CLOUD_SERVICES.name());
        when(veEntity.getLocation()).thenReturn("FR");
        Map<String, String> countryMap = Map.of("FR", "FRA");
        when(boaviztapiService.getCountryMap()).thenReturn(countryMap);
        doNothing().when(inVirtualEquipmentRepository).deleteByInventoryIdAndNameIn(anyLong(), any());
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        service.execute(context, fileToLoad, 0, virtualEquipments);
        verify(inVirtualEquipmentRepository).deleteByInventoryIdAndNameIn(eq(1L), any());
        verify(inVirtualEquipmentRepository).saveAll(any());
        verify(entityManager).flush();
        verify(entityManager).clear();
        verify(veEntity).setLocation("FRA");
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
        InVirtualEquipmentRest veRest = mock(InVirtualEquipmentRest.class);
        when(veRest.getName()).thenReturn("ve1");
        List<InVirtualEquipmentRest> virtualEquipments = List.of(veRest);
        List<LineError> checkErrors = List.of(mock(LineError.class));
        when(checkVirtualEquipmentService.checkRules(any(), any(), any(), anyInt(), any())).thenReturn(checkErrors);
        service.execute(context, fileToLoad, 0, virtualEquipments);
    }

    @Test
    void testExecuteWithNullInventoryId() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(Collections.emptyMap());
        InVirtualEquipmentRest veRest = mock(InVirtualEquipmentRest.class);
        when(veRest.getName()).thenReturn("ve1");
        List<InVirtualEquipmentRest> virtualEquipments = List.of(veRest);
        when(checkVirtualEquipmentService.checkRules(any(), any(), any(), anyInt(), any())).thenReturn(Collections.emptyList());
        InVirtualEquipment veEntity = mock(InVirtualEquipment.class);
        when(inVirtualEquipmentMapper.toEntity(veRest)).thenReturn(veEntity);
        when(veEntity.getInfrastructureType()).thenReturn("OTHER");
        doNothing().when(inVirtualEquipmentRepository).deleteByDigitalServiceVersionUidAndNameIn(anyString(), any());
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        service.execute(context, fileToLoad, 0, virtualEquipments);
        verify(inVirtualEquipmentRepository).deleteByDigitalServiceVersionUidAndNameIn(eq("uid"), any());
        verify(inVirtualEquipmentRepository).saveAll(any());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testGetVirtualEquipmentCount() {
        when(inVirtualEquipmentRepository.countQuantityByDistinctNameByInventoryId(1L)).thenReturn(3L);
        Long result = service.getVirtualEquipmentCount(1L);
        assertEquals(3L, result);
        verify(inVirtualEquipmentRepository).countQuantityByDistinctNameByInventoryId(1L);
    }
}
