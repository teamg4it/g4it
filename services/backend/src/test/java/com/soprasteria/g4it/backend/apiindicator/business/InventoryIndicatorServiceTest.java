/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
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
    @Mock
    private TaskService taskService;
    @Mock
    private ItemTypeRepository itemTypeRepository;
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

    @Test
    void getEquipmentIndicatorsDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        Map<String, EquipmentIndicatorBO> expected = Map.of(
                "climate-change", EquipmentIndicatorBO.builder().impacts(List.of()).build());

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getEquipmentIndicators(taskId)).thenReturn(expected);

        Map<String, EquipmentIndicatorBO> result =
                inventoryIndicatorService.getEquipmentIndicators(organization, workspaceId, inventoryId);

        assertEquals(expected, result);
        verify(indicatorService).getEquipmentIndicators(taskId);
    }

    @Test
    void getApplicationIndicatorsDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        List<ApplicationIndicatorBO<ApplicationImpactBO>> expected = List.of(new ApplicationIndicatorBO<>());

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getApplicationIndicators(taskId)).thenReturn(expected);

        List<ApplicationIndicatorBO<ApplicationImpactBO>> result =
                inventoryIndicatorService.getApplicationIndicators(organization, workspaceId, inventoryId);

        assertEquals(expected, result);
        verify(indicatorService).getApplicationIndicators(taskId);
    }

    @Test
    void getApplicationIndicatorsPageDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();
        Pageable pageable = PageRequest.of(0, 20);

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        ApplicationIndicatorsPageBO expected = ApplicationIndicatorsPageBO.builder().build();

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getApplicationIndicatorsPage(taskId, filters, pageable)).thenReturn(expected);

        ApplicationIndicatorsPageBO result = inventoryIndicatorService.getApplicationIndicatorsPage(
                organization, workspaceId, inventoryId, filters, pageable);

        assertEquals(expected, result);
        verify(indicatorService).getApplicationIndicatorsPage(taskId, filters, pageable);
    }

    @Test
    void getApplicationMultiCriteriaImpactsDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        List<ApplicationMultiCriteriaImpactBO> expected = List.of(ApplicationMultiCriteriaImpactBO.builder().build());

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getApplicationMultiCriteriaImpacts(taskId, filters)).thenReturn(expected);

        List<ApplicationMultiCriteriaImpactBO> result = inventoryIndicatorService.getApplicationMultiCriteriaImpacts(
                organization, workspaceId, inventoryId, filters);

        assertEquals(expected, result);
        verify(indicatorService).getApplicationMultiCriteriaImpacts(taskId, filters);
    }

    @Test
    void getApplicationMultiCriteriaIndicatorsDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        List<String> criteria = List.of("climate-change");
        GraphLevel graphLevel = GraphLevel.GLOBAL;
        RepartitionType repartition = RepartitionType.ENVIRONMENT;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        List<ApplicationMultiCriteriaBO> expected = List.of(ApplicationMultiCriteriaBO.builder().build());

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getApplicationMultiCriteriaIndicators(taskId, criteria, graphLevel, repartition, filters))
                .thenReturn(expected);

        List<ApplicationMultiCriteriaBO> result = inventoryIndicatorService.getApplicationMultiCriteriaIndicators(
                organization, workspaceId, inventoryId, criteria, graphLevel, repartition, filters);

        assertEquals(expected, result);
        verify(indicatorService).getApplicationMultiCriteriaIndicators(taskId, criteria, graphLevel, repartition, filters);
    }

    @Test
    void getVirtualEquipmentLowImpactCountDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        VirtualEquipmentLowImpactCountBO expected = VirtualEquipmentLowImpactCountBO.builder().build();

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getVirtualEquipmentLowImpactCount(taskId, inventoryId, workspaceId, filters))
                .thenReturn(expected);

        VirtualEquipmentLowImpactCountBO result = inventoryIndicatorService.getVirtualEquipmentLowImpactCount(
                organization, workspaceId, inventoryId, filters);

        assertEquals(expected, result);
        verify(indicatorService).getVirtualEquipmentLowImpactCount(taskId, inventoryId, workspaceId, filters);
    }

    @Test
    void getApplicationFiltersDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        ApplicationFiltersBO expected = ApplicationFiltersBO.builder().build();

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getApplicationFilters(taskId)).thenReturn(expected);

        ApplicationFiltersBO result = inventoryIndicatorService.getApplicationFilters(organization, workspaceId, inventoryId);

        assertEquals(expected, result);
        verify(indicatorService).getApplicationFilters(taskId);
    }

    @Test
    void deleteIndicatorsDelegatesToTaskService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;

        inventoryIndicatorService.deleteIndicators(organization, workspaceId, inventoryId);

        verify(taskService).deleteEvaluatingTasksByInventoryId(organization, workspaceId, inventoryId);
    }

    @Test
    void getDataCenterIndicatorsDelegatesToIndicatorService() {
        Long inventoryId = 1L;
        List<DataCentersInformationBO> expected = List.of(new DataCentersInformationBO());

        when(indicatorService.getDataCenterIndicators(inventoryId)).thenReturn(expected);

        List<DataCentersInformationBO> result = inventoryIndicatorService.getDataCenterIndicators(inventoryId);

        assertEquals(expected, result);
        verify(indicatorService).getDataCenterIndicators(inventoryId);
    }

    @Test
    void getPhysicalEquipmentAvgAgeDelegatesToIndicatorService() {
        Long inventoryId = 1L;
        List<PhysicalEquipmentsAvgAgeBO> expected = List.of(new PhysicalEquipmentsAvgAgeBO());

        when(indicatorService.getPhysicalEquipmentAvgAge(inventoryId)).thenReturn(expected);

        List<PhysicalEquipmentsAvgAgeBO> result = inventoryIndicatorService.getPhysicalEquipmentAvgAge(inventoryId);

        assertEquals(expected, result);
        verify(indicatorService).getPhysicalEquipmentAvgAge(inventoryId);
    }

    @Test
    void getPhysicalEquipmentsLowImpactDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        List<PhysicalEquipmentLowImpactBO> expected = List.of(new PhysicalEquipmentLowImpactBO());

        when(indicatorService.getPhysicalEquipmentsLowImpact(organization, workspaceId, inventoryId)).thenReturn(expected);

        List<PhysicalEquipmentLowImpactBO> result =
                inventoryIndicatorService.getPhysicalEquipmentsLowImpact(organization, workspaceId, inventoryId);

        assertEquals(expected, result);
        verify(indicatorService).getPhysicalEquipmentsLowImpact(organization, workspaceId, inventoryId);
    }

    @Test
    void getVirtualEquipmentElecConsumptionSumDelegatesToIndicatorService() {
        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 2L;
        Long taskId = 3L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        InventoryBO inventory = new InventoryBO();
        inventory.setId(inventoryId);

        VirtualEquipmentElecConsumptionSumBO expected = VirtualEquipmentElecConsumptionSumBO.builder().build();

        when(inventoryService.getInventory(organization, workspaceId, inventoryId)).thenReturn(inventory);
        when(taskRepository.findByInventoryAndLastCreationDate(Mockito.any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getVirtualEquipmentElecConsumptionSum(taskId, filters)).thenReturn(expected);

        VirtualEquipmentElecConsumptionSumBO result = inventoryIndicatorService.getVirtualEquipmentElecConsumptionSum(
                organization, workspaceId, inventoryId, filters);

        assertEquals(expected, result);
        verify(indicatorService).getVirtualEquipmentElecConsumptionSum(taskId, filters);
    }

    @Test
    void getEquipmentIndicatorsPrefersWorkspaceThenOrganizationItemCategory() {
        String organization = "org";
        Long workspaceId = 7L;
        Long inventoryId = 11L;
        Long taskId = 13L;
        EquipmentImpactBO printer = EquipmentImpactBO.builder().equipment("printer").build();
        EquipmentImpactBO laptop = EquipmentImpactBO.builder().equipment("laptop").build();
        Map<String, EquipmentIndicatorBO> indicators = Map.of(
                "criterion",
                EquipmentIndicatorBO.builder().impacts(List.of(printer, laptop)).build()
        );

        when(inventoryService.getInventory(organization, workspaceId, inventoryId))
                .thenReturn(InventoryBO.builder().id(inventoryId).build());
        when(taskRepository.findByInventoryAndLastCreationDate(any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(taskId).build()));
        when(indicatorService.getEquipmentIndicators(taskId)).thenReturn(indicators);
        when(itemTypeRepository.findByWorkspaceIdOrWorkspaceIdIsNull(workspaceId)).thenReturn(List.of(
                itemType("printer", "INFRASTRUCTURE", null, null),
                itemType("printer", "ORGANIZATION", organization, null),
                itemType("printer", "Custom print category", null, workspaceId),
                itemType("laptop", "INFRASTRUCTURE", null, null),
                itemType("laptop", "ORGANIZATION", organization, null),
                itemType("laptop", "INFRASTRUCTURE", null, null)
        ));

        inventoryIndicatorService.getEquipmentIndicators(organization, workspaceId, inventoryId);

        assertEquals("Custom print category", printer.getItemCategory());
        assertEquals("ORGANIZATION", laptop.getItemCategory());
    }

    private ItemType itemType(String type, String itemCategory, String organization, Long workspaceId) {
        return ItemType.builder()
                .type(type)
                .itemCategory(itemCategory)
                .organization(organization)
                .workspaceId(workspaceId)
                .build();
    }
}
