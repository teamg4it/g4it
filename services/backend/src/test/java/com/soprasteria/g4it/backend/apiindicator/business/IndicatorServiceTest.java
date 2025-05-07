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
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

//    @Test
//    void getEquipmentIndicators_returnsMappedIndicators_whenTaskIdIsValid() {
//        Long taskId = 1L;
//        List<OutPhysicalEquipment> equipmentList = List.of(new OutPhysicalEquipment());
//        Map<String, List<OutPhysicalEquipment>> groupedEquipment = Map.of("criterion", equipmentList);
//        EquipmentIndicatorBO indicatorBO = new EquipmentIndicatorBO();
//
//        when(outPhysicalEquipmentRepository.findByTaskId(taskId)).thenReturn(equipmentList);
//        when(equipmentIndicatorMapper.outToDto(equipmentList)).thenReturn(indicatorBO);
//
//        Map<String, EquipmentIndicatorBO> result = indicatorService.getEquipmentIndicators(taskId);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(indicatorBO, result.get("criterion"));
//    }

    @Test
    void getApplicationIndicators_returnsMappedIndicators_whenTaskIdIsValid() {
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
    void getDataCenterIndicators_returnsIndicators_whenInventoryIdIsValid() {
        Long inventoryId = 1L;
        List<DataCentersInformationBO> indicators = List.of(new DataCentersInformationBO());

        when(dataCenterIndicatorService.getDataCenterIndicators(inventoryId)).thenReturn(indicators);

        List<DataCentersInformationBO> result = indicatorService.getDataCenterIndicators(inventoryId);

        assertNotNull(result);
        assertEquals(indicators, result);
    }

    @Test
    void getPhysicalEquipmentAvgAge_returnsIndicators_whenInventoryIdIsValid() {
        long inventoryId = 1L;
        List<PhysicalEquipmentsAvgAgeBO> avgAgeIndicators = List.of(new PhysicalEquipmentsAvgAgeBO());

        when(physicalEquipmentIndicatorService.getPhysicalEquipmentAvgAge(inventoryId)).thenReturn(avgAgeIndicators);

        List<PhysicalEquipmentsAvgAgeBO> result = indicatorService.getPhysicalEquipmentAvgAge(inventoryId);

        assertNotNull(result);
        assertEquals(avgAgeIndicators, result);
    }

    @Test
    void getPhysicalEquipmentsLowImpact_returnsIndicators_whenParametersAreValid() {
        String subscriber = "subscriber";
        Long organizationId = 1L;
        Long inventoryId = 1L;
        List<PhysicalEquipmentLowImpactBO> lowImpactIndicators = List.of(new PhysicalEquipmentLowImpactBO());

        when(physicalEquipmentIndicatorService.getPhysicalEquipmentsLowImpact(subscriber, organizationId, inventoryId))
                .thenReturn(lowImpactIndicators);

        List<PhysicalEquipmentLowImpactBO> result = indicatorService.getPhysicalEquipmentsLowImpact(subscriber, organizationId, inventoryId);

        assertNotNull(result);
        assertEquals(lowImpactIndicators, result);
    }

    @Test
    void getPhysicalEquipmentElecConsumption_returnsIndicators_whenParametersAreValid() {
        Long taskId = 1L;
        Long criteriaNumber = 2L;
        List<PhysicalEquipmentElecConsumptionBO> elecConsumptionIndicators = List.of(new PhysicalEquipmentElecConsumptionBO());

        when(physicalEquipmentIndicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber))
                .thenReturn(elecConsumptionIndicators);

        List<PhysicalEquipmentElecConsumptionBO> result = indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber);

        assertNotNull(result);
        assertEquals(elecConsumptionIndicators, result);
    }
}
