/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;


import com.soprasteria.g4it.backend.apiinout.mapper.InApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InApplicationServiceTest {

    @Mock
    private InApplicationRepository inApplicationRepository;

    @Mock
    private InApplicationMapper inApplicationMapper;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private InApplicationService inApplicationService;

    @Test
    void getByInventory_returnsEmptyList_whenNoApplicationsFound() {
        Long inventoryId = 1L;

        when(inApplicationRepository.findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class)))
                .thenReturn(List.of());

        List<InApplicationRest> result = inApplicationService.getByInventory(inventoryId);

        assertEquals(List.of(), result);
        verify(inApplicationRepository).findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class));
        verifyNoInteractions(inApplicationMapper);
    }

    @Test
    void getByInventory_returnsMappedApplications_whenApplicationsFound() {
        Long inventoryId = 1L;

        InApplication application = new InApplication();
        application.setId(1L);
        application.setInventoryId(inventoryId);

        List<InApplicationRest> mappedRest = List.of(InApplicationRest.builder().id(1L).build());

        when(inApplicationRepository.findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(application)))
                .thenReturn(List.of());

        when(inApplicationMapper.toRest(any(List.class)))
                .thenReturn(mappedRest);

        List<InApplicationRest> result = inApplicationService.getByInventory(inventoryId);

        assertEquals(mappedRest, result);
        verify(inApplicationRepository, atLeast(1))
                .findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class));
        verify(inApplicationMapper).toRest(any(List.class));
    }

    @Test
    void getByInventory_processesMultipleBatches() {
        Long inventoryId = 1L;

        InApplication app1 = new InApplication();
        app1.setId(1L);
        InApplication app2 = new InApplication();
        app2.setId(2L);

        List<InApplicationRest> mapped1 = List.of(InApplicationRest.builder().id(1L).build());
        List<InApplicationRest> mapped2 = List.of(InApplicationRest.builder().id(2L).build());

        when(inApplicationRepository.findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(app1)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(app2)))
                .thenReturn(List.of());

        when(inApplicationMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<InApplicationRest> result = inApplicationService.getByInventory(inventoryId);

        assertEquals(2, result.size());
        verify(inApplicationRepository, times(3))
                .findByInventoryIdOrderByIdAsc(eq(inventoryId), any(Pageable.class));
        verify(inApplicationMapper, times(2))
                .toRest(any(List.class));
    }

    @Test
    void getByInventoryAndId_returnsApplication_whenFound() {
        Long inventoryId = 1L;
        Long applicationId = 2L;
        InApplication application = new InApplication();
        application.setInventoryId(inventoryId);
        application.setId(applicationId);
        InApplicationRest applicationRest = InApplicationRest.builder().id(applicationId).build();

        when(inApplicationRepository.findByInventoryIdAndId(inventoryId, applicationId))
                .thenReturn(Optional.of(application));
        when(inApplicationMapper.toRest(application))
                .thenReturn(applicationRest);

        InApplicationRest result = inApplicationService.getByInventoryAndId(inventoryId, applicationId);

        assertNotNull(result);
        assertEquals(applicationId, result.getId());
        verify(inApplicationRepository).findByInventoryIdAndId(inventoryId, applicationId);
        verify(inApplicationMapper).toRest(application);
    }

    @Test
    void getByInventoryAndIdThrowsException() {
        Long inventoryId = 1L;
        Long applicationId = 2L;
        InApplication existingApplication = new InApplication();
        existingApplication.setInventoryId(3L);
        existingApplication.setId(5L);
        when(inApplicationRepository.findByInventoryIdAndId(inventoryId, applicationId)).thenReturn(Optional.empty());

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inApplicationService.getByInventoryAndId(inventoryId, applicationId));

        assertEquals("404", exception1.getCode());
        assertTrue(exception1.getMessage().contains("has no application with id"));
        verify(inApplicationRepository).findByInventoryIdAndId(inventoryId, applicationId);

        when(inApplicationRepository.findByInventoryIdAndId(inventoryId, applicationId)).thenReturn(Optional.of(existingApplication));

        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inApplicationService.getByInventoryAndId(inventoryId, applicationId));

        assertEquals("409", exception2.getCode());
        assertTrue(exception2.getMessage().contains("not compatible with the inventory id"));
    }

    @Test
    void createInApplicationInventoryThrowsExceptionWhenInventoryDoesNotExist() {
        Long inventoryId = 1L;
        InApplicationRest inApplicationRest = new InApplicationRest();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inApplicationService.createInApplicationInventory(inventoryId, inApplicationRest));

        assertEquals("404", exception.getCode());
        assertTrue(exception.getMessage().contains("doesn't exist"));
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void createInApplicationInventory() {
        Long inventoryId = 1L;
        InApplication existingApplication = new InApplication();
        existingApplication.setInventoryId(3L);
        existingApplication.setId(5L);
        InApplicationRest inApplicationRest = new InApplicationRest();
        var organization = Workspace.builder()
                .name("DEMO")
                .organization(Organization.builder().name("SUBSCRIBER").build())
                .build();
        var inventory = Inventory.builder()
                .name("Inventory Name")
                .id(1L)
                .workspace(organization)
                .doExportVerbose(true)
                .build();
        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.of(inventory));
        when(inApplicationMapper.toEntity(inApplicationRest)).thenReturn(existingApplication);
        when(inApplicationMapper.toRest(Mockito.any(InApplication.class))).thenReturn(inApplicationRest);
        InApplicationRest response = inApplicationService.createInApplicationInventory(inventoryId, inApplicationRest);
        assertNotNull(response);
    }

    @Test
    void updateInApplicationThrowsException() {
        Long inventoryId = 1L;
        Long applicationId = 2L;
        InApplicationRest inApplicationUpdateRest = new InApplicationRest();
        InApplication existingApplication = new InApplication();
        existingApplication.setInventoryId(3L);
        existingApplication.setId(5L);

        when(inApplicationRepository.findByInventoryIdAndId(inventoryId, applicationId)).thenReturn(Optional.of(existingApplication));

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inApplicationService.updateInApplication(inventoryId, applicationId, inApplicationUpdateRest));

        assertEquals("409", exception1.getCode());
        assertTrue(exception1.getMessage().contains("is not compatible with the inventory id"));
        verify(inApplicationRepository).findByInventoryIdAndId(inventoryId, applicationId);

        when(inApplicationRepository.findByInventoryIdAndId(inventoryId, applicationId)).thenReturn(Optional.empty());

        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inApplicationService.updateInApplication(inventoryId, applicationId, inApplicationUpdateRest));

        assertEquals("404", exception2.getCode());
        assertTrue(exception2.getMessage().contains("has no application with id"));
    }

    @Test
    void updateInApplication() {
        InApplicationRest inApplicationUpdateRest = new InApplicationRest();
        InApplication existingApplication = new InApplication();
        existingApplication.setInventoryId(3L);
        existingApplication.setId(5L);

        when(inApplicationRepository.findByInventoryIdAndId(existingApplication.getInventoryId(), existingApplication.getId())).thenReturn(Optional.of(existingApplication));
        when(inApplicationMapper.toEntity(inApplicationUpdateRest)).thenReturn(existingApplication);
        when(inApplicationMapper.toRest(Mockito.any(InApplication.class))).thenReturn(inApplicationUpdateRest);
        InApplicationRest response = inApplicationService.updateInApplication(existingApplication.getInventoryId(), existingApplication.getId(), inApplicationUpdateRest);
        assertNotNull(response);
    }

    @Test
    void deleteInApplicationDeletesApplicationWhenIdExists() {
        Long applicationId = 1L;

        doNothing().when(inApplicationRepository).deleteById(applicationId);

        inApplicationService.deleteInApplication(applicationId);

        verify(inApplicationRepository).deleteById(applicationId);
    }
}