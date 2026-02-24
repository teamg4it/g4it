/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryIndicatorServiceTest {

    @Mock
    private InventoryService inventoryService;
    @Mock
    private IndicatorService indicatorService;
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private InventoryIndicatorService inventoryIndicatorService;

    @Test
    void returnsElectricConsumptionIndicatorsWhenValidInputsProvided() {
        String organization = "testOrganization";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        Long criteriaNumber = 4L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        List<PhysicalEquipmentElecConsumptionBO> expectedIndicators = List.of(new PhysicalEquipmentElecConsumptionBO());

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(inventoryService.getCriteriaNumber(taskId)).thenReturn(criteriaNumber);
        when(indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber)).thenReturn(expectedIndicators);

        List<PhysicalEquipmentElecConsumptionBO> result = inventoryIndicatorService.getPhysicalEquipmentElecConsumption(organization, workspaceId, inventoryId);

        assertEquals(expectedIndicators, result);
    }

    @Test
    void throwsExceptionWhenNoTaskFoundForInventory() {
        String organization = "testOrganization";
        Long workspaceId = 1L;
        Long inventoryId = 2L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class))).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class,
                () -> inventoryIndicatorService.getPhysicalEquipmentElecConsumption(organization, workspaceId, inventoryId));

        assertEquals("404", exception.getCode());
        assertEquals(String.format("inventory %d has no batch executed", inventoryId), exception.getMessage());
    }

    @Test
    void returnsEmptyListWhenNoIndicatorsFound() {
        String organization = "testOrganization";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        Long criteriaNumber = 4L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);
        inventory.setName("test_data");
        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(inventoryService.getCriteriaNumber(taskId)).thenReturn(criteriaNumber);
        when(indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber)).thenReturn(List.of());

        List<PhysicalEquipmentElecConsumptionBO> result = inventoryIndicatorService.getPhysicalEquipmentElecConsumption(organization, workspaceId, inventoryId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDelegateVirtualEquipmentsLowImpact() {

        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 10L;

        List<VirtualEquipmentLowImpactBO> expected =
                List.of(new VirtualEquipmentLowImpactBO());

        when(indicatorService
                .getVirtualEquipmentsLowImpact(organization, workspaceId, inventoryId))
                .thenReturn(expected);

        List<VirtualEquipmentLowImpactBO> result =
                inventoryIndicatorService
                        .getVirtualEquipmentsLowImpact(organization, workspaceId, inventoryId);

        assertThat(result).isEqualTo(expected);

        verify(indicatorService)
                .getVirtualEquipmentsLowImpact(organization, workspaceId, inventoryId);
    }

    @Test
    void shouldReturnVirtualEquipmentElecConsumption() {

        Long inventoryId = 10L;
        Long taskId = 100L;
        Long criteriaNumber = 5L;

        Task task = new Task();
        task.setId(taskId);

        List<VirtualEquipmentElecConsumptionBO> expected =
                List.of(new VirtualEquipmentElecConsumptionBO());

        when(taskRepository.findByInventoryAndLastCreationDate(any()))
                .thenReturn(Optional.of(task));

        when(indicatorService.getVirtualEquipmentElecConsumption(taskId))
                .thenReturn(expected);

        List<VirtualEquipmentElecConsumptionBO> result =
                inventoryIndicatorService
                        .getVirtualEquipmentElecConsumption("ORG", 1L, inventoryId);

        assertThat(result).isEqualTo(expected);

        verify(taskRepository).findByInventoryAndLastCreationDate(any());
        verify(indicatorService)
                .getVirtualEquipmentElecConsumption(taskId);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {

        when(taskRepository.findByInventoryAndLastCreationDate(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                inventoryIndicatorService
                        .getVirtualEquipmentElecConsumption("ORG", 1L, 10L)
        ).isInstanceOf(NoSuchElementException.class);

        verify(taskRepository).findByInventoryAndLastCreationDate(any());
        verifyNoInteractions(inventoryService);
        verifyNoInteractions(indicatorService);
    }
}
