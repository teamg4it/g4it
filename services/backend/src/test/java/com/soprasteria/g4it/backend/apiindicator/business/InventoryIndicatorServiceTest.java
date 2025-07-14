/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentElecConsumptionBO;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        String subscriber = "testSubscriber";
        Long organizationId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        Long criteriaNumber = 4L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        List<PhysicalEquipmentElecConsumptionBO> expectedIndicators = List.of(new PhysicalEquipmentElecConsumptionBO());

        when(inventoryService.getInventory(subscriber, organizationId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(inventoryService.getCriteriaNumber(taskId)).thenReturn(criteriaNumber);
        when(indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber)).thenReturn(expectedIndicators);

        List<PhysicalEquipmentElecConsumptionBO> result = inventoryIndicatorService.getPhysicalEquipmentElecConsumption(subscriber, organizationId, inventoryId);

        assertEquals(expectedIndicators, result);
    }

    @Test
    void throwsExceptionWhenNoTaskFoundForInventory() {
        String subscriber = "testSubscriber";
        Long organizationId = 1L;
        Long inventoryId = 2L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        when(inventoryService.getInventory(subscriber, organizationId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class))).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class,
                () -> inventoryIndicatorService.getPhysicalEquipmentElecConsumption(subscriber, organizationId, inventoryId));

        assertEquals("404", exception.getCode());
        assertEquals(String.format("inventory %d has no batch executed", inventoryId), exception.getMessage());
    }

    @Test
    void returnsEmptyListWhenNoIndicatorsFound() {
        String subscriber = "testSubscriber";
        Long organizationId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        Long criteriaNumber = 4L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);
        inventory.setName("test_data");
        when(inventoryService.getInventory(subscriber, organizationId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(inventoryService.getCriteriaNumber(taskId)).thenReturn(criteriaNumber);
        when(indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber)).thenReturn(List.of());

        List<PhysicalEquipmentElecConsumptionBO> result = inventoryIndicatorService.getPhysicalEquipmentElecConsumption(subscriber, organizationId, inventoryId);

        assertTrue(result.isEmpty());
    }
}
