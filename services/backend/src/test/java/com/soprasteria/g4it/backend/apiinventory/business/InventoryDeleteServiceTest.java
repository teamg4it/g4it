/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinventory.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiindicator.business.InventoryIndicatorService;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDeleteServiceTest {

    @InjectMocks
    private InventoryDeleteService inventoryDeleteService;

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private InventoryIndicatorService inventoryIndicatorService;
    @Mock
    private InDatacenterRepository inDatacenterRepository;
    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private InApplicationRepository inApplicationRepository;

    private final String subscriber = "subscriber";

    @Test
    void testDeleteAllInventoriesOfOrganization(){
        final Organization organization = TestUtils.createOrganization();

        final List<Inventory> inventorysEntitiesList = List.of(Inventory.builder().id(2L).name("03-2023").build());

        when(organizationService.getOrganizationById(1L)).thenReturn(organization);
        when(inventoryRepository.findByOrganization(organization)).thenReturn(inventorysEntitiesList);

        inventoryDeleteService.deleteInventories(subscriber, 1L);

        verify(organizationService).getOrganizationById(1L);
        verify(inventoryRepository).findByOrganization(organization);
        verify(inDatacenterRepository).deleteByInventoryId(2L);
        verify(inPhysicalEquipmentRepository).deleteByInventoryId(2L);
        verify(inVirtualEquipmentRepository).deleteByInventoryId(2L);
        verify(inApplicationRepository).deleteByInventoryId(2L);
        verify(inventoryIndicatorService).deleteIndicators(subscriber, 1L, 2L);
        verify(inventoryRepository).deleteByInventoryId(2L);
    }

    @Test
    void testDeleteByIdInventory_Exists() {
        final Organization organization = TestUtils.createOrganization();
        final Inventory inventory = Inventory.builder().id(2L).name("03-2023").build();
        when(organizationService.getOrganizationById(1L)).thenReturn(organization);
        when(inventoryRepository.findByOrganizationAndId(organization, 2L)).thenReturn(Optional.of(inventory));

        inventoryDeleteService.deleteInventory(subscriber, 1L, 2L);

        verify(organizationService).getOrganizationById(1L);
        verify(inventoryRepository).findByOrganizationAndId(organization, 2L);
        verify(inDatacenterRepository).deleteByInventoryId(2L);
        verify(inPhysicalEquipmentRepository).deleteByInventoryId(2L);
        verify(inVirtualEquipmentRepository).deleteByInventoryId(2L);
        verify(inApplicationRepository).deleteByInventoryId(2L);
        verify(inventoryIndicatorService).deleteIndicators(subscriber, 1L, 2L);
        verify(inventoryRepository).deleteByInventoryId(2L);
    }

    @Test
    void testDeleteByIdInventory_NotFound() {
        final Organization organization = TestUtils.createOrganization();

        when(organizationService.getOrganizationById(1L)).thenReturn(organization);
        when(inventoryRepository.findByOrganizationAndId(organization, 2L)).thenReturn(Optional.empty());

        inventoryDeleteService.deleteInventory(subscriber, 1L, 2L);

        verify(organizationService).getOrganizationById(1L);
        verify(inventoryRepository).findByOrganizationAndId(organization, 2L);
        verifyNoMoreInteractions(inDatacenterRepository, inPhysicalEquipmentRepository,
                inVirtualEquipmentRepository, inApplicationRepository,
                inventoryIndicatorService, inventoryRepository);
    }

    @Test
    void deleteInventory_byInventory_shouldDeleteInputAndIndicatorsAndInventory() {
        final Inventory inventory = Inventory.builder().id(2L).name("03-2023").build();

        inventoryDeleteService.deleteInventory(subscriber, 1L, inventory);

        verify(inDatacenterRepository).deleteByInventoryId(2L);
        verify(inPhysicalEquipmentRepository).deleteByInventoryId(2L);
        verify(inVirtualEquipmentRepository).deleteByInventoryId(2L);
        verify(inApplicationRepository).deleteByInventoryId(2L);
        verify(inventoryIndicatorService).deleteIndicators(subscriber, 1L, 2L);
        verify(inventoryRepository).deleteByInventoryId(2L);
    }
}
