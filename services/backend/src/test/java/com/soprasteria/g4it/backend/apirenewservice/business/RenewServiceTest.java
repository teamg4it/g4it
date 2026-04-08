/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apirenewservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.error.ErrorConstants;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewResponseRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewUpdateRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenewServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @InjectMocks
    private RenewService renewService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(renewService, "dataRetentiondDay", 30);
    }

    @Test
    void testGetRenewDetailsInventory_success() {
        Workspace workspace = new Workspace();
        Organization org = new Organization();
        org.setDataRetentionDay(20);
        workspace.setOrganization(org);
        workspace.setDataRetentionDay(null);
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setName("Test Inventory");
        inventory.setLastUpdateDate(LocalDateTime.now().minusDays(5));
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        RenewRest result = renewService.getRenewDetailsInventory(1L, 1L);
        assertEquals("Test Inventory", result.getServiceName());
        assertEquals("1", result.getServiceId());
        assertEquals(20, result.getRetentionDays());
        assertNotNull(result.getExpiryDate());
    }

    @Test
    void testGetRenewDetailsInventory_workspaceNotFound() {
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.empty());
        G4itRestException ex = assertThrows(G4itRestException.class, () -> renewService.getRenewDetailsInventory(1L, 1L));
        assertTrue(ex.getMessage().contains(String.format(ErrorConstants.WORKSPACE_NOT_FOUND, 1L)));
    }

    @Test
    void testGetRenewDetailsInventory_inventoryNotFound() {
        Workspace workspace = new Workspace();
        Organization org = new Organization();
        workspace.setOrganization(org);
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(workspace));
        when(inventoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        G4itRestException ex = assertThrows(G4itRestException.class, () -> renewService.getRenewDetailsInventory(1L, 1L));
        String expectedMsg = String.format(ErrorConstants.INVENTORY_NOT_FOUND, 1L);
        assertTrue(ex.getMessage().contains(expectedMsg));
    }

    @Test
    void testGetRenewDetailsDigitalService_success() {
        Workspace workspace = new Workspace();
        Organization org = new Organization();
        org.setDataRetentionDay(15);
        workspace.setOrganization(org);
        workspace.setDataRetentionDay(null);
        DigitalService digitalService = new DigitalService();
        digitalService.setUid("ds-uid");
        digitalService.setName("Digital Service");
        digitalService.setLastUpdateDate(LocalDateTime.now().minusDays(3));
        DigitalServiceVersion version = new DigitalServiceVersion();
        version.setDigitalService(digitalService);
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(workspace));
        when(digitalServiceVersionRepository.findById(anyString())).thenReturn(Optional.of(version));
        RenewRest result = renewService.getRenewDetailsDigitalService(1L, "ver-uid");
        assertEquals("Digital Service", result.getServiceName());
        assertEquals("ds-uid", result.getServiceId());
        assertEquals(15, result.getRetentionDays());
        assertNotNull(result.getExpiryDate());
    }

    @Test
    void testGetRenewDetailsDigitalService_notFound() {
        Workspace workspace = new Workspace();
        Organization org = new Organization();
        workspace.setOrganization(org);
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(workspace));
        when(digitalServiceVersionRepository.findById(anyString())).thenReturn(Optional.empty());
        G4itRestException ex = assertThrows(G4itRestException.class, () -> renewService.getRenewDetailsDigitalService(1L, "ver-uid"));
        String expectedMsg = String.format(ErrorConstants.DIGITAL_SERVICE_NOT_FOUND, "ver-uid");
        assertTrue(ex.getMessage().contains(expectedMsg));
    }

    @Test
    void testRenewDigitalService_success() {
        Workspace workspace = new Workspace();
        DigitalService digitalService = new DigitalService();
        digitalService.setUid("ds-uid");
        RenewUpdateRest updateRest = RenewUpdateRest.builder().action("renew").serviceId("ds-uid").build();
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(workspace));
        when(digitalServiceRepository.findByWorkspaceAndUid(any(), anyString())).thenReturn(Optional.of(digitalService));
        RenewResponseRest response = renewService.renewDigitalService(1L, updateRest);
        assertTrue(response.getIsRenewed());
        assertEquals("ds-uid", response.getServiceId());
        assertEquals(Constants.RENEWAL_SUCCESS_MESSAGE, response.getResponseMessage());
        verify(digitalServiceRepository).save(any(DigitalService.class));
    }

    @Test
    void testRenewDigitalService_invalidAction() {
        RenewUpdateRest updateRest = RenewUpdateRest.builder().action("invalid").build();
        G4itRestException ex = assertThrows(G4itRestException.class, () -> renewService.renewDigitalService(1L, updateRest));
        assertTrue(ex.getMessage().contains(ErrorConstants.INVALID_RENEW_ACTION));
    }

    @Test
    void testRenewInventoryService_success() {
        Workspace workspace = new Workspace();
        Inventory inventory = new Inventory();
        inventory.setId(2L);
        RenewUpdateRest updateRest = RenewUpdateRest.builder().action("renew").build();
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(workspace));
        when(inventoryRepository.findById(anyLong())).thenReturn(Optional.of(inventory));
        RenewResponseRest response = renewService.renewInventoryService(1L, 2L, updateRest);
        assertTrue(response.getIsRenewed());
        assertEquals("2", response.getServiceId());
        assertEquals(Constants.RENEWAL_SUCCESS_MESSAGE, response.getResponseMessage());
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void testRenewInventoryService_invalidAction() {
        RenewUpdateRest updateRest = RenewUpdateRest.builder().action("invalid").build();
        G4itRestException ex = assertThrows(G4itRestException.class, () -> renewService.renewInventoryService(1L, 2L, updateRest));
        assertTrue(ex.getMessage().contains(ErrorConstants.INVALID_RENEW_ACTION));
    }
}
