/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileDeletionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationDeletionServiceTest {
    @InjectMocks
    OrganizationDeletionService organizationDeletionService;
    @Mock
    OrganizationRepository organizationRepository;
    @Mock
    FileDeletionService fileDeletionService;
    @Mock
    InventoryDeleteService inventoryDeleteService;
    @Mock
    DigitalServiceService digitalServiceService;
    @Mock
    private InventoryRepository inventoryRepo;
    @Mock
    private DigitalServiceRepository digitalServiceRepo;

    @Test
    void testOrganizationDeletionService_toBeDeletedStatusWithPastDate() {
        var now = LocalDateTime.now().minusHours(1);
        final Optional<Inventory> inventoryEntity1 = Optional.ofNullable(Inventory.builder().id(1L).name("03-2023").lastUpdateDate(now).build());
        final Optional<DigitalService> digitalServiceEntity = Optional.ofNullable(DigitalService.builder().uid("1234").name("name").lastUpdateDate(now).build());
        final Organization linkedOrganization = TestUtils.createToBeDeletedOrganization(now);

        when(inventoryRepo.findByOrganization(linkedOrganization)).thenReturn(List.of(inventoryEntity1.get()));
        when(digitalServiceRepo.findByOrganization(linkedOrganization)).thenReturn(List.of(digitalServiceEntity.get()));
        when(organizationRepository.findAllByStatusIn(List.of(OrganizationStatus.TO_BE_DELETED.name()))).thenReturn(List.of(linkedOrganization));
        lenient().when(fileDeletionService.deleteFiles(any(), any(), any(), any())).thenReturn(List.of());

        // EXECUTE
        organizationDeletionService.executeDeletion();

        verify(fileDeletionService, times(1)).deleteFiles(any(), any(), eq(FileFolder.EXPORT), eq(0));
        verify(fileDeletionService, times(1)).deleteFiles(any(), any(), eq(FileFolder.OUTPUT), eq(0));
    }

    @Test
    void testOrganizationDeletionService_toBeDeletedStatusWithFutureDate() {
        final Organization linkedOrganization = TestUtils.createToBeDeletedOrganization(LocalDateTime.now().plusDays(1));
        when(organizationRepository.findAllByStatusIn(List.of(OrganizationStatus.TO_BE_DELETED.name()))).thenReturn(List.of(linkedOrganization));

        // EXECUTE
        organizationDeletionService.executeDeletion();
        verify(inventoryDeleteService, times(0)).deleteInventory(any(), any(), anyLong());
        verify(digitalServiceService, times(0)).deleteDigitalService(any());
        verify(fileDeletionService, times(0)).deleteFiles(any(), any(), eq(FileFolder.EXPORT), eq(0));
        verify(fileDeletionService, times(0)).deleteFiles(any(), any(), eq(FileFolder.OUTPUT), eq(0));
    }

    @Test
    void testStorageDeletionService_inActiveStatus() {
        final Organization linkedOrganization = TestUtils.createOrganizationWithStatus(OrganizationStatus.INACTIVE.name());

        when(organizationRepository.findAllByStatusIn(List.of(OrganizationStatus.TO_BE_DELETED.name()))).thenReturn(List.of(linkedOrganization));
        // EXECUTE
        organizationDeletionService.executeDeletion();
        verify(inventoryDeleteService, times(0)).deleteInventory(any(), any(), anyLong());
        verify(digitalServiceService, times(0)).deleteDigitalService(any());

    }

    @Test
    void testStorageDeletionService_deletionDateNull() {
        final Organization linkedOrganization = TestUtils.createOrganizationWithStatus(OrganizationStatus.INACTIVE.name());
        linkedOrganization.setDeletionDate(null);

        when(organizationRepository.findAllByStatusIn(List.of(OrganizationStatus.TO_BE_DELETED.name()))).thenReturn(List.of(linkedOrganization));
        // EXECUTE
        organizationDeletionService.executeDeletion();
        verify(inventoryDeleteService, times(0)).deleteInventory(any(), any(), anyLong());
        verify(digitalServiceService, times(0)).deleteDigitalService(any());

    }
}
