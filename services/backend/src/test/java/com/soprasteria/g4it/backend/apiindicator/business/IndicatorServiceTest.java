/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.ApplicationIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.EquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndicatorServiceTest {

    @Mock
    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Mock
    private EquipmentIndicatorMapper equipmentIndicatorMapper;

    @Mock
    private OutApplicationRepository outApplicationRepository;

    @Mock
    private ApplicationIndicatorMapper applicationIndicatorMapper;

    @Mock
    private DataCenterIndicatorService dataCenterIndicatorService;

    @Mock
    private PhysicalEquipmentIndicatorService physicalEquipmentIndicatorService;

    @InjectMocks
    private IndicatorService indicatorService;

    @Mock
    private VirtualEquipmentIndicatorService virtualEquipmentIndicatorService;

    /*@Test
    void getEquipmentIndicatorsReturnsMappedIndicatorsWhenTaskIdIsValid() {
        Long taskId = 1L;
        String criterion = "Resource_Group";
        String expectedKey = "resource-group"; // Adjust if your service transforms the key

        List<OutPhysicalEquipment> equipmentList = List.of(
                OutPhysicalEquipment.builder().id(3L).criterion(criterion).build()
        );
        EquipmentIndicatorBO indicatorBO = EquipmentIndicatorBO.builder().build();

        when(outPhysicalEquipmentRepository.findByTaskId(taskId)).thenReturn(equipmentList);
        when(equipmentIndicatorMapper.outToDto(equipmentList)).thenReturn(indicatorBO);

        Map<String, EquipmentIndicatorBO> result = indicatorService.getEquipmentIndicators(taskId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }*/

    @Test
    void getApplicationIndicatorsReturnsMappedIndicatorsWhenTaskIdIsValid() {
        Long taskId = 1L;
        List<OutApplication> applications = List.of(new OutApplication());
        List<ApplicationIndicatorBO<ApplicationImpactBO>> mappedIndicators = List.of(new ApplicationIndicatorBO<>());

        when(outApplicationRepository.findByTaskId(taskId)).thenReturn(applications);
        when(applicationIndicatorMapper.toOutDto(applications)).thenReturn(mappedIndicators);

        List<ApplicationIndicatorBO<ApplicationImpactBO>> result = indicatorService.getApplicationIndicators(taskId);

        assertNotNull(result);
        assertEquals(mappedIndicators, result);
    }

    @Test
    void getDataCenterIndicatorsReturnsIndicatorsWhenInventoryIdIsValid() {
        Long inventoryId = 1L;
        List<DataCentersInformationBO> indicators = List.of(new DataCentersInformationBO());

        when(dataCenterIndicatorService.getDataCenterIndicators(inventoryId)).thenReturn(indicators);

        List<DataCentersInformationBO> result = indicatorService.getDataCenterIndicators(inventoryId);

        assertNotNull(result);
        assertEquals(indicators, result);
    }

    @Test
    void getPhysicalEquipmentAvgAgeReturnsIndicatorsWhenInventoryIdIsValid() {
        long inventoryId = 1L;
        List<PhysicalEquipmentsAvgAgeBO> avgAgeIndicators = List.of(new PhysicalEquipmentsAvgAgeBO());

        when(physicalEquipmentIndicatorService.getPhysicalEquipmentAvgAge(inventoryId)).thenReturn(avgAgeIndicators);

        List<PhysicalEquipmentsAvgAgeBO> result = indicatorService.getPhysicalEquipmentAvgAge(inventoryId);

        assertNotNull(result);
        assertEquals(avgAgeIndicators, result);
    }

    @Test
    void getPhysicalEquipmentsLowImpactReturnsIndicatorsWhenParametersAreValid() {
        String organization = "organization";
        Long workspaceId = 1L;
        Long inventoryId = 1L;
        List<PhysicalEquipmentLowImpactBO> lowImpactIndicators = List.of(new PhysicalEquipmentLowImpactBO());

        when(physicalEquipmentIndicatorService.getPhysicalEquipmentsLowImpact(organization, workspaceId, inventoryId))
                .thenReturn(lowImpactIndicators);

        List<PhysicalEquipmentLowImpactBO> result = indicatorService.getPhysicalEquipmentsLowImpact(organization, workspaceId, inventoryId);

        assertNotNull(result);
        assertEquals(lowImpactIndicators, result);
    }

    @Test
    void getPhysicalEquipmentElecConsumptionReturnsIndicatorsWhenParametersAreValid() {
        Long taskId = 1L;
        Long criteriaNumber = 2L;
        List<PhysicalEquipmentElecConsumptionBO> elecConsumptionIndicators = List.of(new PhysicalEquipmentElecConsumptionBO());

        when(physicalEquipmentIndicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber))
                .thenReturn(elecConsumptionIndicators);

        List<PhysicalEquipmentElecConsumptionBO> result = indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber);

        assertNotNull(result);
        assertEquals(elecConsumptionIndicators, result);
    }

    @Test
    void shouldDelegateGetVirtualEquipmentElecConsumptionToVirtualService() {

        Long taskId = 123L;

        List<VirtualEquipmentElecConsumptionBO> expected =
                List.of(new VirtualEquipmentElecConsumptionBO());

        when(virtualEquipmentIndicatorService
                .getVirtualEquipmentElecConsumption(taskId))
                .thenReturn(expected);

        List<VirtualEquipmentElecConsumptionBO> result =
                indicatorService.getVirtualEquipmentElecConsumption(taskId);

        assertThat(result).isEqualTo(expected);

        verify(virtualEquipmentIndicatorService)
                .getVirtualEquipmentElecConsumption(taskId);
    }

    @Test
    void getEquipmentIndicators_returnsGroupedAndMappedIndicators() {

        Long taskId = 1L;

        OutPhysicalEquipment equipment1 =
                OutPhysicalEquipment.builder()
                        .id(1L)
                        .build();

        OutPhysicalEquipment equipment2 =
                OutPhysicalEquipment.builder()
                        .id(2L)
                        .build();

        List<Object[]> repositoryResult = List.of(
                new Object[]{"resource_group", equipment1},
                new Object[]{"resource_group", equipment2}
        );

        EquipmentIndicatorBO indicatorBO =
                EquipmentIndicatorBO.builder().build();

        when(outPhysicalEquipmentRepository.findCriterionAndEquipmentByTaskId(taskId))
                .thenReturn(repositoryResult);

        when(equipmentIndicatorMapper.outToDto(List.of(equipment1, equipment2)))
                .thenReturn(indicatorBO);

        Map<String, EquipmentIndicatorBO> result =
                indicatorService.getEquipmentIndicators(taskId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("resource-group"));
        assertEquals(indicatorBO, result.get("resource-group"));

        verify(outPhysicalEquipmentRepository)
                .findCriterionAndEquipmentByTaskId(taskId);

        verify(equipmentIndicatorMapper)
                .outToDto(List.of(equipment1, equipment2));
    }

    @Test
    void getEquipmentIndicators_groupsByCriterion() {

        Long taskId = 1L;

        OutPhysicalEquipment equipment1 =
                OutPhysicalEquipment.builder().id(1L).build();

        OutPhysicalEquipment equipment2 =
                OutPhysicalEquipment.builder().id(2L).build();

        EquipmentIndicatorBO indicator1 =
                EquipmentIndicatorBO.builder().build();

        EquipmentIndicatorBO indicator2 =
                EquipmentIndicatorBO.builder().build();

        when(outPhysicalEquipmentRepository.findCriterionAndEquipmentByTaskId(taskId))
                .thenReturn(List.of(
                        new Object[]{"resource_group", equipment1},
                        new Object[]{"climate_change", equipment2}
                ));

        when(equipmentIndicatorMapper.outToDto(List.of(equipment1)))
                .thenReturn(indicator1);

        when(equipmentIndicatorMapper.outToDto(List.of(equipment2)))
                .thenReturn(indicator2);

        Map<String, EquipmentIndicatorBO> result =
                indicatorService.getEquipmentIndicators(taskId);

        assertEquals(2, result.size());

        assertEquals(
                indicator1,
                result.get("resource-group"));

        assertEquals(
                indicator2,
                result.get("climate-change"));
    }

    @Test
    void getEquipmentIndicators_returnsEmptyMap_whenNoDataFound() {

        Long taskId = 1L;

        when(outPhysicalEquipmentRepository.findCriterionAndEquipmentByTaskId(taskId))
                .thenReturn(List.of());

        Map<String, EquipmentIndicatorBO> result =
                indicatorService.getEquipmentIndicators(taskId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(outPhysicalEquipmentRepository)
                .findCriterionAndEquipmentByTaskId(taskId);

        verifyNoInteractions(equipmentIndicatorMapper);
    }

    @Test
    void shouldDelegateGetVirtualEquipmentsLowImpactToVirtualService() {

        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 10L;

        List<VirtualEquipmentLowImpactBO> expected =
                List.of(new VirtualEquipmentLowImpactBO());

        when(virtualEquipmentIndicatorService.getVirtualEquipmentsLowImpact(
                organization,
                workspaceId,
                inventoryId))
                .thenReturn(expected);

        List<VirtualEquipmentLowImpactBO> result =
                indicatorService.getVirtualEquipmentsLowImpact(
                        organization,
                        workspaceId,
                        inventoryId);

        assertEquals(expected, result);

        verify(virtualEquipmentIndicatorService)
                .getVirtualEquipmentsLowImpact(
                        organization,
                        workspaceId,
                        inventoryId);
    }
}
