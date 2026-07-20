/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckApplicationService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
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
class LoadApplicationServiceTest {
    @Mock
    private CheckApplicationService checkApplicationService;
    @Mock
    private InApplicationMapper inApplicationMapper;
    @Mock
    private InApplicationRepository inApplicationRepository;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadApplicationService service;

    @Test
    void testExecuteEmptyApplications() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<InApplicationRest> applications = Collections.emptyList();
        List<LineError> result = service.execute(context, fileToLoad, 0, applications);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExecuteValidApplications() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(1L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(Collections.emptyMap());
        InApplicationRest appRest = mock(InApplicationRest.class);
        when(appRest.getName()).thenReturn("app1");
        List<InApplicationRest> applications = List.of(appRest);
        when(checkApplicationService.checkRules(any(), any(), any(), anyInt())).thenReturn(Collections.emptyList());
        InApplication appEntity = mock(InApplication.class);
        when(inApplicationMapper.toEntity(appRest)).thenReturn(appEntity);
        when(appEntity.getVirtualEquipmentName()).thenReturn("ve1");
        when(appEntity.getPhysicalEquipmentName()).thenReturn(null);
        InVirtualEquipment ve = mock(InVirtualEquipment.class);
        when(ve.getName()).thenReturn("ve1");
        when(ve.getPhysicalEquipmentName()).thenReturn("pe1");
        when(inVirtualEquipmentRepository.findByInventoryIdAndVirtualEquipmentName(anyLong(), any())).thenReturn(List.of(ve));
        doNothing().when(inApplicationRepository).deleteByInventoryIdAndNameIn(anyLong(), any());
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        service.execute(context, fileToLoad, 0, applications);
        verify(inApplicationRepository).deleteByInventoryIdAndNameIn(eq(1L), any());
        verify(inApplicationRepository).saveAll(any());
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
        InApplicationRest appRest = mock(InApplicationRest.class);
        when(appRest.getName()).thenReturn("app1");
        List<InApplicationRest> applications = List.of(appRest);
        List<LineError> checkErrors = List.of(mock(LineError.class));
        when(checkApplicationService.checkRules(any(), any(), any(), anyInt())).thenReturn(checkErrors);
        List<LineError> result = service.execute(context, fileToLoad, 0, applications);
        assertEquals(2, result.size());
    }

    @Test
    void testGetApplicationCount() {
        when(inApplicationRepository.countDistinctNameByInventoryId(1L)).thenReturn(5L);
        Long result = service.getApplicationCount(1L);
        assertEquals(5L, result);
        verify(inApplicationRepository).countDistinctNameByInventoryId(1L);
    }
}
