/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InDatacenterMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckDatacenterService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
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
class LoadDatacenterServiceTest {
    @Mock
    private CheckDatacenterService checkDatacenterService;
    @Mock
    private InDatacenterMapper inDatacenterMapper;
    @Mock
    private InDatacenterRepository inDatacenterRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private LoadDatacenterService service;

    @Test
    void testExecuteEmptyDatacenters() {
        Context context = mock(Context.class);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        List<InDatacenterRest> datacenters = Collections.emptyList();
        List<LineError> result = service.execute(context, fileToLoad, 0, datacenters);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExecuteValidDatacenters() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(1L);
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(Collections.emptyMap());
        InDatacenterRest dcRest = mock(InDatacenterRest.class);
        when(dcRest.getName()).thenReturn("dc1");
        List<InDatacenterRest> datacenters = List.of(dcRest);
        when(checkDatacenterService.checkRules(any(), any(), any(), anyInt())).thenReturn(Collections.emptyList());
        InDatacenter dcEntity = mock(InDatacenter.class);
        when(inDatacenterMapper.toEntity(dcRest)).thenReturn(dcEntity);
        doNothing().when(inDatacenterRepository).deleteByInventoryIdAndNameIn(anyLong(), any());
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        service.execute(context, fileToLoad, 0, datacenters);
        verify(inDatacenterRepository).deleteByInventoryIdAndNameIn(eq(1L), any());
        verify(inDatacenterRepository).saveAll(any());
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
        InDatacenterRest dcRest = mock(InDatacenterRest.class);
        when(dcRest.getName()).thenReturn("dc1");
        List<InDatacenterRest> datacenters = List.of(dcRest);
        List<LineError> checkErrors = List.of(mock(LineError.class));
        when(checkDatacenterService.checkRules(any(), any(), any(), anyInt())).thenReturn(checkErrors);
        service.execute(context, fileToLoad, 0, datacenters);
    }

    @Test
    void testExecuteWithNullInventoryId() {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("log");
        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getCoherenceErrorByLineNumer()).thenReturn(Collections.emptyMap());
        InDatacenterRest dcRest = mock(InDatacenterRest.class);
        when(dcRest.getName()).thenReturn("dc1");
        List<InDatacenterRest> datacenters = List.of(dcRest);
        when(checkDatacenterService.checkRules(any(), any(), any(), anyInt())).thenReturn(Collections.emptyList());
        InDatacenter dcEntity = mock(InDatacenter.class);
        when(inDatacenterMapper.toEntity(dcRest)).thenReturn(dcEntity);
        doNothing().when(inDatacenterRepository).deleteByDigitalServiceVersionUidAndNameIn(anyString(), any());
        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).clear();
        service.execute(context, fileToLoad, 0, datacenters);
        verify(inDatacenterRepository).deleteByDigitalServiceVersionUidAndNameIn(eq("uid"), any());
        verify(inDatacenterRepository).saveAll(any());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testGetDatacenterCount() {
        when(inDatacenterRepository.countDistinctNameByInventoryId(1L)).thenReturn(2L);
        Long result = service.getDatacenterCount(1L);
        assertEquals(2L, result);
        verify(inDatacenterRepository).countDistinctNameByInventoryId(1L);
    }
}
