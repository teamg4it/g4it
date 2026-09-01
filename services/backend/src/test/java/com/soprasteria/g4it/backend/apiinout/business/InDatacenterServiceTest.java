/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InDatacenterMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class InDatacenterServiceTest {

    @Mock
    private InDatacenterRepository inDatacenterRepository;

    @Mock
    private InDatacenterMapper inDatacenterMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private InDatacenterService inDatacenterService;

    // ========== getByInventory Tests ==========
    
    @Test
    void getByInventory_returnsEmptyList_whenNoDatacentersFound() {
        Long inventoryId = 1L;

        when(inDatacenterRepository.findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class)))
                .thenReturn(List.of());

        List<InDatacenterRest> result = inDatacenterService.getByInventory(inventoryId);

        assertEquals(List.of(), result);
        verify(inDatacenterRepository).findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class));
        verifyNoInteractions(inDatacenterMapper);
    }

    @Test
    void getByInventory_returnsDatacenters_whenInventoryExists() {
        Long inventoryId = 1L;
        InDatacenter datacenter = new InDatacenter();
        List<InDatacenterRest> datacenterRests = List.of(new InDatacenterRest());

        when(inDatacenterRepository.findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(datacenter)))
                .thenReturn(List.of());
        when(inDatacenterMapper.toRest(any(List.class))).thenReturn(datacenterRests);

        List<InDatacenterRest> result = inDatacenterService.getByInventory(inventoryId);

        assertEquals(datacenterRests, result);
        verify(inDatacenterRepository, atLeast(1)).findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class));
        verify(inDatacenterMapper).toRest(any(List.class));
    }

    @Test
    void getByInventory_processesMultipleBatches() {
        Long inventoryId = 1L;
        InDatacenter dc1 = new InDatacenter();
        InDatacenter dc2 = new InDatacenter();
        List<InDatacenterRest> mapped1 = List.of(new InDatacenterRest());
        List<InDatacenterRest> mapped2 = List.of(new InDatacenterRest());

        when(inDatacenterRepository.findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(dc1)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(dc2)))
                .thenReturn(List.of());
        when(inDatacenterMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<InDatacenterRest> result = inDatacenterService.getByInventory(inventoryId);

        assertEquals(2, result.size());
        verify(inDatacenterRepository, times(3)).findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class));
        verify(inDatacenterMapper, times(2)).toRest(any(List.class));
    }

    @Test
    void getByInventoryAndId() {
        Long inventoryId = 1L;
        Long id = 1L;
        InDatacenter inDatacenter = InDatacenter.builder().id(11L).inventoryId(2L).build();
        when(inDatacenterRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inDatacenterService.getByInventoryAndId(inventoryId, id));

        assertEquals("404", exception1.getCode());
        verify(inDatacenterRepository).findByInventoryIdAndId(inventoryId, id);

        when(inDatacenterRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.of(inDatacenter));
        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inDatacenterService.getByInventoryAndId(inventoryId, id));

        assertEquals("409", exception2.getCode());

        when(inDatacenterRepository.findByInventoryIdAndId(inDatacenter.getInventoryId(), inDatacenter.getId())).thenReturn(Optional.of(inDatacenter));
        when(inDatacenterMapper.toRest(inDatacenter)).thenReturn(new InDatacenterRest());
        InDatacenterRest response = inDatacenterService.getByInventoryAndId(inDatacenter.getInventoryId(), inDatacenter.getId());
        assertNotNull(response);
    }

    @Test
    void createInDatacenterInventory() {
        var organization = Workspace.builder()
                .name("DEMO")
                .organization(Organization.builder().name("SUBSCRIBER").build())
                .build();
        var inventory = Inventory.builder()
                .name("Inventory Name")
                .workspace(organization)
                .doExportVerbose(true)
                .build();
        InDatacenterRest inDatacenterRest = new InDatacenterRest();
        InDatacenter inDatacenter = InDatacenter.builder().name("datacenter_name").build();
        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.of(inventory));
        when(inDatacenterMapper.toEntity(inDatacenterRest)).thenReturn(inDatacenter);
        when(inDatacenterMapper.toRest(Mockito.any(InDatacenter.class))).thenReturn(inDatacenterRest);
        InDatacenterRest response = inDatacenterService.createInDatacenterInventory(inventory.getId(), inDatacenterRest);
        assertNotNull(response);
    }


    @Test
    void createInDatacenterInventoryThrowsException() {
        Long inventoryId = 1L;
        InDatacenterRest inDatacenterRest = new InDatacenterRest();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.createInDatacenterInventory(inventoryId, inDatacenterRest));

        assertEquals("404", exception.getCode());
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void updateInDatacenterThrowsException() {
        Long inventoryId = 1L;
        Long id = 1L;
        InDatacenterRest inDatacenterUpdateRest = new InDatacenterRest();
        InDatacenter inDatacenter = InDatacenter.builder().name("datacenter_name").build();
        when(inDatacenterRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inDatacenterService.updateInDatacenter(inventoryId, id, inDatacenterUpdateRest));

        assertEquals("404", exception1.getCode());
        verify(inDatacenterRepository).findByInventoryIdAndId(inventoryId, id);

        when(inDatacenterRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.of(inDatacenter));

        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inDatacenterService.updateInDatacenter(inventoryId, id, inDatacenterUpdateRest));

        assertEquals("409", exception2.getCode());
    }

    // ========== getByDigitalServiceVersion Tests ==========

    @Test
    void getByDigitalService_returnsEmptyList_whenNoDatacentersExist() {
        String digitalServiceUid = "nonexistent-uid";

        when(inDatacenterRepository.findByDigitalServiceVersionUid(eq(digitalServiceUid), any(Pageable.class)))
                .thenReturn(List.of());

        List<InDatacenterRest> result = inDatacenterService.getByDigitalServiceVersion(digitalServiceUid);

        assertEquals(0, result.size());
        verify(inDatacenterRepository).findByDigitalServiceVersionUid(eq(digitalServiceUid), any(Pageable.class));
        verifyNoInteractions(inDatacenterMapper);
    }

    @Test
    void getByDigitalService_returnsDatacenters_whenDatacentersExist() {
        String digitalServiceUid = "test-uid";
        InDatacenter datacenter = new InDatacenter();
        List<InDatacenterRest> datacenterRests = List.of(new InDatacenterRest());

        when(inDatacenterRepository.findByDigitalServiceVersionUid(eq(digitalServiceUid), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(datacenter)))
                .thenReturn(List.of());
        when(inDatacenterMapper.toRest(any(List.class))).thenReturn(datacenterRests);

        List<InDatacenterRest> result = inDatacenterService.getByDigitalServiceVersion(digitalServiceUid);

        assertEquals(datacenterRests, result);
        verify(inDatacenterRepository, atLeast(1)).findByDigitalServiceVersionUid(eq(digitalServiceUid), any(Pageable.class));
        verify(inDatacenterMapper).toRest(any(List.class));
    }

    @Test
    void getByDigitalService_processesMultipleBatches() {
        String digitalServiceUid = "test-uid";
        InDatacenter dc1 = new InDatacenter();
        InDatacenter dc2 = new InDatacenter();
        List<InDatacenterRest> mapped1 = List.of(new InDatacenterRest());
        List<InDatacenterRest> mapped2 = List.of(new InDatacenterRest());

        when(inDatacenterRepository.findByDigitalServiceVersionUid(eq(digitalServiceUid), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(dc1)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(dc2)))
                .thenReturn(List.of());
        when(inDatacenterMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<InDatacenterRest> result = inDatacenterService.getByDigitalServiceVersion(digitalServiceUid);

        assertEquals(2, result.size());
        verify(inDatacenterRepository, times(3)).findByDigitalServiceVersionUid(eq(digitalServiceUid), any(Pageable.class));
        verify(inDatacenterMapper, times(2)).toRest(any(List.class));
    }


    @Test
    void getByDigitalServiceAndId() {
        InDatacenter inDatacenter = new InDatacenter();
        inDatacenter.setDigitalServiceVersionUid("uid2");
        inDatacenter.setId(1L);
        InDatacenterRest inDatacenterRest = new InDatacenterRest();

        when(inDatacenterRepository.findByDigitalServiceVersionUidAndId(inDatacenter.getDigitalServiceVersionUid(), inDatacenter.getId())).thenReturn(Optional.of(inDatacenter));
        when(inDatacenterMapper.toRest(Mockito.any(InDatacenter.class))).thenReturn(inDatacenterRest);
        InDatacenterRest response = inDatacenterService.getByDigitalServiceVersionAndId(inDatacenter.getDigitalServiceVersionUid(), inDatacenter.getId());
        assertNotNull(response);

    }

    @Test
    void getByDigitalServiceAndIdThrowsException() {
        String digitalServiceUid = "uid1";
        Long id = 1L;
        InDatacenter inDatacenter = new InDatacenter();
        inDatacenter.setDigitalServiceUid("uid2");

        when(inDatacenterRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(inDatacenter));

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inDatacenterService.getByDigitalServiceVersionAndId(digitalServiceUid, id));

        assertEquals("409", exception1.getCode());
        verify(inDatacenterRepository).findByDigitalServiceVersionUidAndId(digitalServiceUid, id);

        when(inDatacenterRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inDatacenterService.getByDigitalServiceVersionAndId(digitalServiceUid, id));

        assertEquals("404", exception2.getCode());
    }

    @Test
    void createInDatacenterDigitalService() {
        
        DigitalService digitalService = new DigitalService();
        digitalService.setUid("uid1");
        var digitalServiceVersion = DigitalServiceVersion.builder()
                .description("DS_Name")
                .uid("dummy_id")
                .digitalService(digitalService)
                .build();

        InDatacenterRest inDatacenterRest = new InDatacenterRest();
        InDatacenter inDatacenter = InDatacenter.builder().name("datacenter_name").build();
        when(digitalServiceVersionRepository.findById(digitalServiceVersion.getUid())).thenReturn(Optional.of(digitalServiceVersion));
        when(inDatacenterMapper.toEntity(inDatacenterRest)).thenReturn(inDatacenter);
        when(inDatacenterMapper.toRest(Mockito.any(InDatacenter.class))).thenReturn(inDatacenterRest);
        InDatacenterRest response = inDatacenterService.createInDatacenterDigitalServiceVersion(digitalServiceVersion.getUid(), inDatacenterRest);
        assertNotNull(response);

    }


    @Test
    void createInDatacenterDigitalServiceThrowsExceptionWhenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "nonexistent-uid";
        InDatacenterRest inDatacenterRest = new InDatacenterRest();

        when(digitalServiceVersionRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.createInDatacenterDigitalServiceVersion(digitalServiceUid, inDatacenterRest));

        assertEquals("404", exception.getCode());
        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
    }

    @Test
    void updateInDatacenterDigitalService() {
        InDatacenterRest inDatacenterUpdateRest = new InDatacenterRest();
        InDatacenter inDatacenter = InDatacenter.builder().name("datacenter_name").id(1L).build();

        when(inDatacenterRepository.findByDigitalServiceVersionUidAndId(inDatacenter.getDigitalServiceUid(), inDatacenter.getId())).thenReturn(Optional.of(inDatacenter));
        when(inDatacenterMapper.toEntity(inDatacenterUpdateRest)).thenReturn(inDatacenter);
        when(inDatacenterMapper.toRest(Mockito.any(InDatacenter.class))).thenReturn(inDatacenterUpdateRest);

        InDatacenterRest response = inDatacenterService.updateInDatacenter(inDatacenter.getDigitalServiceUid(), inDatacenter.getId(), inDatacenterUpdateRest);
        assertNotNull(response);
    }

    @Test
    void updateInDatacenterInventory() {
        InDatacenterRest inDatacenterUpdateRest = new InDatacenterRest();
        InDatacenter inDatacenter = InDatacenter.builder().name("test_data").id(12L).inventoryId(31L).build();
        when(inDatacenterRepository.findByInventoryIdAndId(inDatacenter.getInventoryId(), inDatacenter.getId())).thenReturn(Optional.of(inDatacenter));
        when(inDatacenterMapper.toEntity(Mockito.any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inDatacenterMapper.toRest(Mockito.any(InDatacenter.class))).thenReturn(new InDatacenterRest());
        InDatacenterRest response = inDatacenterService.updateInDatacenter(inDatacenter.getInventoryId(), inDatacenter.getId(), inDatacenterUpdateRest);
        assertNotNull(response);
    }

    @Test
    void deleteInDatacenter_doesNotThrowException_whenDatacenterExists() {
        Long id = 1L;

        doNothing().when(inDatacenterRepository).deleteById(id);

        inDatacenterService.deleteInDatacenter(id);

        verify(inDatacenterRepository).deleteById(id);
    }


}