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
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationFiltersProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationIndicatorRowProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaAggregateProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaImpactProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentElecConsumptionSumProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentLowImpactCountProjection;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private EntityManager entityManager;

    @Mock
    private BoaviztapiService boaviztapiService;


    @Test
    void getApplicationIndicatorsReturnsMappedIndicatorsWhenTaskIdIsValid() {
        Long taskId = 1L;

        OutApplication application = new OutApplication();
        List<OutApplication> applications = new ArrayList<>(List.of(application));

        List<ApplicationIndicatorBO<ApplicationImpactBO>> mappedIndicators =
                List.of(new ApplicationIndicatorBO<>());

        doReturn(applications, new ArrayList<>()).when(outApplicationRepository).findByTaskIdOrderByIdAsc(
                eq(taskId),
                any(Pageable.class)
        );

        when(applicationIndicatorMapper.toOutDto(any(List.class)))
                .thenReturn(mappedIndicators);

        List<ApplicationIndicatorBO<ApplicationImpactBO>> result =
                indicatorService.getApplicationIndicators(taskId);

        assertNotNull(result);
        assertEquals(mappedIndicators, result);

        verify(outApplicationRepository, atLeast(1)).findByTaskIdOrderByIdAsc(
                eq(taskId),
                any(Pageable.class)
        );

        verify(applicationIndicatorMapper).toOutDto(any(List.class));
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

        List<Object[]> repositoryResult = new ArrayList<>(List.of(
                new Object[]{"resource_group", equipment1},
                new Object[]{"resource_group", equipment2}
        ));

        EquipmentIndicatorBO indicatorBO =
                EquipmentIndicatorBO.builder().build();

        doReturn(repositoryResult, new ArrayList<>()).when(outPhysicalEquipmentRepository).findCriterionAndEquipmentByTaskId(eq(taskId), any());

        when(equipmentIndicatorMapper.outToDto(List.of(equipment1, equipment2)))
                .thenReturn(indicatorBO);

        Map<String, EquipmentIndicatorBO> result =
                indicatorService.getEquipmentIndicators(taskId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("resource-group"));
        assertEquals(indicatorBO, result.get("resource-group"));

        verify(outPhysicalEquipmentRepository, atLeast(1))
                .findCriterionAndEquipmentByTaskId(eq(taskId), any());

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

        doReturn(new ArrayList<>(List.of(
                new Object[]{"resource_group", equipment1},
                new Object[]{"climate_change", equipment2}
        )), new ArrayList<>()).when(outPhysicalEquipmentRepository).findCriterionAndEquipmentByTaskId(eq(taskId), any());

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

        when(outPhysicalEquipmentRepository.findCriterionAndEquipmentByTaskId(eq(taskId), any()))
                .thenReturn(new ArrayList<>());

        Map<String, EquipmentIndicatorBO> result =
                indicatorService.getEquipmentIndicators(taskId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(outPhysicalEquipmentRepository)
                .findCriterionAndEquipmentByTaskId(eq(taskId), any());

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

    @Test
    void getApplicationIndicatorsPageReturnsMappedPage() {
        Long taskId = 1L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);

        ApplicationIndicatorRowProjection row = mock(ApplicationIndicatorRowProjection.class);
        Page<ApplicationIndicatorRowProjection> page = new PageImpl<>(List.of(row), pageable, 1);

        when(outApplicationRepository.findByTaskId(taskId, filters, pageable)).thenReturn(page);
        when(applicationIndicatorMapper.toRowBO(page.getContent()))
                .thenReturn(List.of(new ApplicationIndicatorRowBO()));

        ApplicationIndicatorsPageBO result = indicatorService.getApplicationIndicatorsPage(taskId, filters, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(20, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(outApplicationRepository).findByTaskId(taskId, filters, pageable);
        verify(applicationIndicatorMapper).toRowBO(page.getContent());
    }

    @Test
    void getApplicationMultiCriteriaImpactsReturnsMappedImpacts() {
        Long taskId = 1L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        List<MultiCriteriaImpactProjection> projections = List.of(mock(MultiCriteriaImpactProjection.class));
        List<ApplicationMultiCriteriaImpactBO> expected = List.of(ApplicationMultiCriteriaImpactBO.builder().build());

        when(outApplicationRepository.aggregateMultiCriteriaImpacts(taskId, filters)).thenReturn(projections);
        when(applicationIndicatorMapper.toMultiCriteriaImpactBO(projections)).thenReturn(expected);

        List<ApplicationMultiCriteriaImpactBO> result =
                indicatorService.getApplicationMultiCriteriaImpacts(taskId, filters);

        assertEquals(expected, result);
        verify(outApplicationRepository).aggregateMultiCriteriaImpacts(taskId, filters);
        verify(applicationIndicatorMapper).toMultiCriteriaImpactBO(projections);
    }

    @Test
    void getApplicationMultiCriteriaIndicatorsReturnsMappedIndicators() {
        Long taskId = 1L;
        List<String> criteria = List.of("climate-change");
        GraphLevel graphLevel = GraphLevel.GLOBAL;
        RepartitionType repartition = RepartitionType.ENVIRONMENT;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        List<MultiCriteriaAggregateProjection> projections = List.of(mock(MultiCriteriaAggregateProjection.class));
        List<ApplicationMultiCriteriaBO> expected = List.of(ApplicationMultiCriteriaBO.builder().build());

        when(outApplicationRepository.aggregateMultiCriteria(taskId, criteria, graphLevel, repartition, filters))
                .thenReturn(projections);
        when(applicationIndicatorMapper.toMultiCriteriaBO(projections)).thenReturn(expected);

        List<ApplicationMultiCriteriaBO> result = indicatorService.getApplicationMultiCriteriaIndicators(
                taskId, criteria, graphLevel, repartition, filters);

        assertEquals(expected, result);
        verify(outApplicationRepository).aggregateMultiCriteria(taskId, criteria, graphLevel, repartition, filters);
        verify(applicationIndicatorMapper).toMultiCriteriaBO(projections);
    }

    @Test
    void getVirtualEquipmentLowImpactCountReturnsAggregatedCounts() {
        ReflectionTestUtils.setField(indicatorService, "criterias", Set.of("climate-change"));

        Long taskId = 1L;
        Long inventoryId = 2L;
        Long workspaceId = 3L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        Map<String, String> countryMap = Map.of("France", "FR");
        VirtualEquipmentLowImpactCountProjection projection = mock(VirtualEquipmentLowImpactCountProjection.class);
        VirtualEquipmentLowImpactCountBO expected = VirtualEquipmentLowImpactCountBO.builder()
                .virtualEquipmentTotalCount(10L)
                .lowImpactVirtualEquipmentCount(2L)
                .build();

        when(boaviztapiService.getCountryMap()).thenReturn(countryMap);
        when(outApplicationRepository.countVirtualEquipmentLowImpact(
                eq(taskId), eq(inventoryId), eq(workspaceId), any(), any(), any(), eq(filters)))
                .thenReturn(projection);
        when(applicationIndicatorMapper.toVirtualEquipmentLowImpactCountBO(projection)).thenReturn(expected);

        VirtualEquipmentLowImpactCountBO result =
                indicatorService.getVirtualEquipmentLowImpactCount(taskId, inventoryId, workspaceId, filters);

        assertEquals(expected, result);
        verify(boaviztapiService).getCountryMap();
        verify(applicationIndicatorMapper).toVirtualEquipmentLowImpactCountBO(projection);
    }

    @Test
    void getApplicationFiltersReturnsMappedFilters() {
        Long taskId = 1L;
        ApplicationFiltersProjection projection = mock(ApplicationFiltersProjection.class);
        ApplicationFiltersBO expected = ApplicationFiltersBO.builder().build();

        when(outApplicationRepository.getDistinctFilters(taskId)).thenReturn(projection);
        when(applicationIndicatorMapper.toFiltersBO(projection)).thenReturn(expected);

        ApplicationFiltersBO result = indicatorService.getApplicationFilters(taskId);

        assertEquals(expected, result);
        verify(outApplicationRepository).getDistinctFilters(taskId);
        verify(applicationIndicatorMapper).toFiltersBO(projection);
    }

    @Test
    void getVirtualEquipmentElecConsumptionSumReturnsMappedSum() {
        Long taskId = 1L;
        ApplicationCriteriaFilterBO filters = ApplicationCriteriaFilterBO.builder().build();

        VirtualEquipmentElecConsumptionSumProjection projection = mock(VirtualEquipmentElecConsumptionSumProjection.class);
        VirtualEquipmentElecConsumptionSumBO expected = VirtualEquipmentElecConsumptionSumBO.builder()
                .elecConsumptionSum(100d)
                .noOfVirtualEquipmentsSum(5L)
                .applicationCount(2L)
                .build();

        when(outApplicationRepository.sumVirtualEquipmentElecConsumption(taskId, filters)).thenReturn(projection);
        when(applicationIndicatorMapper.toVirtualEquipmentElecConsumptionSumBO(projection)).thenReturn(expected);

        VirtualEquipmentElecConsumptionSumBO result =
                indicatorService.getVirtualEquipmentElecConsumptionSum(taskId, filters);

        assertEquals(expected, result);
        verify(outApplicationRepository).sumVirtualEquipmentElecConsumption(taskId, filters);
        verify(applicationIndicatorMapper).toVirtualEquipmentElecConsumptionSumBO(projection);
    }
}
