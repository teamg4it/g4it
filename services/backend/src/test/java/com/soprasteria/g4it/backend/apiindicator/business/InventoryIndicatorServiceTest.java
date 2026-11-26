// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiindicator.mapper.IndicatorRestMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.PhysicalEquipmentEvaluationTraceRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
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
    @Mock
    private PhysicalEquipmentEvaluationTraceRepository physicalEquipmentEvaluationTraceRepository;
    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @InjectMocks
    private InventoryIndicatorService inventoryIndicatorService;

    @BeforeEach
    void setUpCategoryResolver() {
        ReflectionTestUtils.setField(inventoryIndicatorService, "itemCategoryResolver",
                new ItemCategoryResolver(itemTypeRepository));
        lenient().when(physicalEquipmentEvaluationTraceRepository.findByTaskId(anyLong(), any(Pageable.class)))
                .thenReturn(Page.empty());
    }

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
        EquipmentImpactBO printer = EquipmentImpactBO.builder()
                .equipment("printer")
                .reference("matched-model")
                .build();
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
                ItemType.builder()
                        .type("mapped printer")
                        .itemCategory("DEVICES")
                        .refDefaultItem("matched-model")
                        .workspaceId(workspaceId)
                        .build(),
                itemType("laptop", "INFRASTRUCTURE", null, null),
                itemType("laptop", "ORGANIZATION", organization, null),
                itemType("laptop", "INFRASTRUCTURE", null, null)
        ));

        inventoryIndicatorService.getEquipmentIndicators(organization, workspaceId, inventoryId);

        assertEquals("mapped printer", printer.getItemType());
        assertEquals("DEVICES", printer.getItemCategory());
        assertEquals("laptop", laptop.getItemType());
        assertEquals("ORGANIZATION", laptop.getItemCategory());
    }

    @Test
    void usageSharesUseOriginalQuantitiesOnceAcrossCriteriaWithoutChangingExistingIndicators() {
        EquipmentImpactBO impact = EquipmentImpactBO.builder()
                .equipment("laptop").quantity(123.5).countValue(7L).impact(19d).sip(2d).build();
        EquipmentIndicatorBO climate = EquipmentIndicatorBO.builder()
                .label("climate").unit("kg").impacts(List.of(impact)).build();
        EquipmentIndicatorBO energy = EquipmentIndicatorBO.builder()
                .label("energy").unit("MJ").impacts(List.of(impact)).build();
        Map<String, EquipmentIndicatorBO> indicators = Map.of("climate", climate, "energy", energy);
        prepareUsageIndicators(indicators, List.of(
                usageTrace(1L, "DEVICES", "BYOD", 2d, 0d),
                usageTrace(2L, "DEVICES", "COPE", 3d, 1d),
                usageTrace(3L, "DEVICES", "", 5d, 1d),
                usageTrace(4L, "INFRASTRUCTURE", "BYOD", 100d, 1d),
                usageTrace(5L, "custom", "COPE", 200d, 1d),
                usageTrace(6L, "devices", "BYOD", 300d, 1d)));

        Map<String, EquipmentIndicatorBO> result =
                inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        assertSame(indicators, result);
        for (EquipmentIndicatorBO indicator : result.values()) {
            assertEquals(20d, indicator.getByodDevicesPercentage());
            assertEquals(30d, indicator.getCopeDevicesPercentage());
            assertSame(impact, indicator.getImpacts().getFirst());
        }
        assertEquals("climate", climate.getLabel());
        assertEquals("kg", climate.getUnit());
        assertEquals("energy", energy.getLabel());
        assertEquals("MJ", energy.getUnit());
        assertEquals(123.5, impact.getQuantity());
        assertEquals(7L, impact.getCountValue());
        assertEquals(19d, impact.getImpact());
        assertEquals(2d, impact.getSip());
        verify(physicalEquipmentEvaluationTraceRepository).findByTaskId(eq(3L), any(Pageable.class));
        verifyNoInteractions(inPhysicalEquipmentRepository);
    }

    @Test
    void usageSharesPreserveFractionalOriginalQuantityAndEvaluationNullDefaultWithoutAllocationWeighting() {
        EquipmentIndicatorBO indicator = usageIndicator();
        List<PhysicalEquipmentEvaluationTrace> traces = List.of(
                usageTrace(1L, "DEVICES", "BYOD", 0.25, 0d),
                usageTrace(2L, "DEVICES", "COPE", 0.5, 0.85),
                usageTrace(3L, "DEVICES", null, 0.75, 0.57),
                usageTrace(4L, "DEVICES", "BYOD", null, 1d));
        prepareUsageIndicators(Map.of("climate", indicator), traces);

        inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        // (0.25 + 1) / 2.5 and 0.5 / 2.5, not scaled quantities or integer casts.
        assertEquals(50d, indicator.getByodDevicesPercentage());
        assertEquals(20d, indicator.getCopeDevicesPercentage());
        assertEquals(0.25, traces.getFirst().getOriginalQuantity());
        assertEquals(0d, traces.getFirst().getEffectiveFraction());
        assertNull(traces.getLast().getOriginalQuantity());
    }

    @Test
    void usageSharesKeepUnroundedPercentageValuesForTheExistingIntegerDisplayFormatter() {
        EquipmentIndicatorBO indicator = usageIndicator();
        prepareUsageIndicators(Map.of("climate", indicator), List.of(
                usageTrace(1L, "DEVICES", "BYOD", 1d, 0.57),
                usageTrace(2L, "DEVICES", "COPE", 2d, 0.85)));

        inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        assertEquals(100d / 3, indicator.getByodDevicesPercentage(), 1e-12);
        assertEquals(200d / 3, indicator.getCopeDevicesPercentage(), 1e-12);
        assertEquals(33L, Math.round(indicator.getByodDevicesPercentage()));
        assertEquals(67L, Math.round(indicator.getCopeDevicesPercentage()));
    }

    @Test
    void usageSharesRetainPositiveSubOnePercentRatherThanRoundingItToZero() {
        EquipmentIndicatorBO indicator = usageIndicator();
        prepareUsageIndicators(Map.of("climate", indicator), List.of(
                usageTrace(1L, "DEVICES", "BYOD", 0.01, 0d),
                usageTrace(2L, "DEVICES", "", 9.99, 1d)));

        inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        // Existing integer display renders this as "< 1", not "0".
        assertEquals(0.1, indicator.getByodDevicesPercentage(), 1e-12);
        assertEquals(0d, indicator.getCopeDevicesPercentage());
    }

    @Test
    void noDeviceSnapshotsYieldNoDataDespiteUsageValuesOrCurrentDeviceReferential() {
        EquipmentIndicatorBO indicator = usageIndicator();
        prepareUsageIndicators(Map.of("climate", indicator), List.of(
                usageTrace(1L, "INFRASTRUCTURE", "BYOD", 100d, 1d),
                usageTrace(2L, "custom", "COPE", 100d, 1d),
                usageTrace(3L, "devices", "BYOD", 100d, 1d)));
        when(itemTypeRepository.findByWorkspaceIdOrWorkspaceIdIsNull(1L))
                .thenReturn(List.of(itemType("laptop", "DEVICES", "ORG", 1L)));

        inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        assertNull(indicator.getByodDevicesPercentage());
        assertNull(indicator.getCopeDevicesPercentage());
    }

    @Test
    void zeroDeviceQuantityYieldsNoDataWithoutDivisionErrors() {
        EquipmentIndicatorBO indicator = usageIndicator();
        prepareUsageIndicators(Map.of("climate", indicator), List.of(
                usageTrace(1L, "DEVICES", "BYOD", 0d, 0.57),
                usageTrace(2L, "DEVICES", "COPE", 0d, 0.85)));

        inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        assertNull(indicator.getByodDevicesPercentage());
        assertNull(indicator.getCopeDevicesPercentage());
    }

    @Test
    void historicalTaskWithoutTracesDoesNotInventUsageFromCurrentInputsOrCategories() {
        EquipmentIndicatorBO indicator = usageIndicator();
        indicator.setImpacts(List.of(EquipmentImpactBO.builder().equipment("laptop").quantity(42d).build()));
        prepareUsageIndicators(Map.of("climate", indicator), List.of());
        when(itemTypeRepository.findByWorkspaceIdOrWorkspaceIdIsNull(1L))
                .thenReturn(List.of(itemType("laptop", "DEVICES", "ORG", 1L)));

        inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        assertNull(indicator.getByodDevicesPercentage());
        assertNull(indicator.getCopeDevicesPercentage());
        assertEquals(42d, indicator.getImpacts().getFirst().getQuantity());
        assertEquals("DEVICES", indicator.getImpacts().getFirst().getItemCategory());
        verifyNoInteractions(inPhysicalEquipmentRepository);
    }

    @Test
    void usageSharesAggregateEveryTracePageForOnlyTheSelectedTask() {
        EquipmentIndicatorBO indicator = usageIndicator();
        prepareUsageIndicators(Map.of("climate", indicator));
        when(physicalEquipmentEvaluationTraceRepository.findByTaskId(eq(3L), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(1);
                    assertEquals(10000, pageable.getPageSize());
                    assertTrue(pageable.getSort().getOrderFor("id").isAscending());
                    return pageable.getPageNumber() == 0
                            ? new PageImpl<>(List.of(
                                    usageTrace(1L, "DEVICES", "BYOD", 1d, 0.57),
                                    usageTrace(2L, "DEVICES", "COPE", 1d, 0.85)), pageable, 10001)
                            : new PageImpl<>(List.of(
                                    usageTrace(3L, "DEVICES", "", 2d, 1d)), pageable, 10001);
                });

        inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        assertEquals(25d, indicator.getByodDevicesPercentage());
        assertEquals(25d, indicator.getCopeDevicesPercentage());
        verify(physicalEquipmentEvaluationTraceRepository, times(2))
                .findByTaskId(eq(3L), any(Pageable.class));
    }

    @Test
    void restMappingCarriesUsagePercentagesAndNullNoDataWithoutChangingExistingCountMapping() {
        IndicatorRestMapper mapper = Mappers.getMapper(IndicatorRestMapper.class);
        EquipmentIndicatorBO indicator = usageIndicator();
        indicator.setByodDevicesPercentage(100d / 3);
        indicator.setCopeDevicesPercentage(0d);
        indicator.setImpacts(List.of(EquipmentImpactBO.builder().quantity(2.5).countValue(99L).build()));

        var result = mapper.toDto(indicator);

        assertEquals(100d / 3, result.getByodDevicesPercentage().get());
        assertEquals(0d, result.getCopeDevicesPercentage().get());
        assertEquals(BigDecimal.valueOf(2.5), result.getImpacts().getFirst().getCountValue());
        var noData = mapper.toDto(usageIndicator());
        assertTrue(noData.getByodDevicesPercentage().isPresent());
        assertNull(noData.getByodDevicesPercentage().get());
        assertNull(noData.getCopeDevicesPercentage().get());
    }

    private EquipmentIndicatorBO usageIndicator() {
        return EquipmentIndicatorBO.builder().label("climate").unit("kg").impacts(List.of()).build();
    }

    @Test
    void emptyEquipmentResultsDoNotReadUsageSnapshotsOrInventIndicators() {
        prepareUsageIndicators(Map.of());
        clearInvocations(physicalEquipmentEvaluationTraceRepository);

        var result = inventoryIndicatorService.getEquipmentIndicators("ORG", 1L, 2L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(physicalEquipmentEvaluationTraceRepository, inPhysicalEquipmentRepository);
    }

    private void prepareUsageIndicators(Map<String, EquipmentIndicatorBO> indicators,
                                        List<PhysicalEquipmentEvaluationTrace> traces) {
        prepareUsageIndicators(indicators);
        when(physicalEquipmentEvaluationTraceRepository.findByTaskId(eq(3L), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(traces, invocation.getArgument(1), traces.size()));
    }

    private void prepareUsageIndicators(Map<String, EquipmentIndicatorBO> indicators) {
        when(inventoryService.getInventory("ORG", 1L, 2L)).thenReturn(InventoryBO.builder().id(2L).build());
        when(taskRepository.findByInventoryAndLastCreationDate(any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(3L).build()));
        when(indicatorService.getEquipmentIndicators(3L)).thenReturn(indicators);
    }

    private PhysicalEquipmentEvaluationTrace usageTrace(Long sourceId, String category, String context,
                                                         Double quantity, Double fraction) {
        return PhysicalEquipmentEvaluationTrace.builder()
                .taskId(3L).sourceEquipmentId(sourceId).itemType("laptop").itemCategory(category)
                .usageContext(context).originalQuantity(quantity).effectiveFraction(fraction).build();
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
