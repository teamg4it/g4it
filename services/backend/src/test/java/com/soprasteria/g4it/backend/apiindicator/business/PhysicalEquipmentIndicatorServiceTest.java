/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.PhysicalEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentsAvgAgeBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentAvgAgeView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentElecConsumptionView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.repository.InPhysicalEquipmentAvgAgeViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InPhysicalEquipmentElecConsumptionViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InPhysicalEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysicalEquipmentIndicatorServiceTest {

    @Mock
    private InPhysicalEquipmentAvgAgeViewRepository avgAgeViewRepository;

    @Mock
    private InPhysicalEquipmentLowImpactViewRepository lowImpactViewRepository;

    @Mock
    private InPhysicalEquipmentElecConsumptionViewRepository elecConsumptionViewRepository;

    @Mock
    private PhysicalEquipmentIndicatorMapper mapper;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private LowImpactService lowImpactService;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private PhysicalEquipmentIndicatorService service;

    @Test
    void getPhysicalEquipmentAvgAgeReturnsMappedIndicatorsWhenValidInventoryId() {
        Long inventoryId = 1L;
        Task task = new Task();
        task.setId(10L);
        List<InPhysicalEquipmentAvgAgeView> repositoryResult = List.of(InPhysicalEquipmentAvgAgeView.builder().id(1L).build());
        List<PhysicalEquipmentsAvgAgeBO> mappedResult = List.of(new PhysicalEquipmentsAvgAgeBO());

        when(taskRepository.findByInventoryAndLastCreationDate(any(Inventory.class))).thenReturn(Optional.of(task));
        when(avgAgeViewRepository.findPhysicalEquipmentAvgAgeIndicators(task.getId())).thenReturn(repositoryResult);
        when(mapper.inPhysicalEquipmentAvgAgetoDto(repositoryResult)).thenReturn(mappedResult);

        List<PhysicalEquipmentsAvgAgeBO> result = service.getPhysicalEquipmentAvgAge(inventoryId);

        assertEquals(mappedResult, result);
        verify(taskRepository).findByInventoryAndLastCreationDate(any(Inventory.class));
        verify(avgAgeViewRepository).findPhysicalEquipmentAvgAgeIndicators(task.getId());
        verify(mapper).inPhysicalEquipmentAvgAgetoDto(repositoryResult);
    }

    @Test
    void getPhysicalEquipmentAvgAgeThrowsExceptionWhenTaskNotFound() {
        Long inventoryId = 1L;

        when(taskRepository.findByInventoryAndLastCreationDate(any(Inventory.class))).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getPhysicalEquipmentAvgAge(inventoryId));
        verify(taskRepository).findByInventoryAndLastCreationDate(any(Inventory.class));
        verifyNoInteractions(avgAgeViewRepository, mapper);
    }

    @Test
    void getPhysicalEquipmentsLowImpactReturnsMappedIndicatorsWhenValidInputs() {
        String subscriber = "subscriber";
        Long organizationId = 1L;
        Long inventoryId = 2L;
        Organization organization = new Organization();
        organization.setName("OrgName");
        List<InPhysicalEquipmentLowImpactView> repositoryResult = List.of(InPhysicalEquipmentLowImpactView.builder().build());
        List<PhysicalEquipmentLowImpactBO> mappedResult = List.of(new PhysicalEquipmentLowImpactBO());

        when(organizationService.getOrganizationById(organizationId)).thenReturn(organization);
        when(lowImpactViewRepository.findPhysicalEquipmentLowImpactIndicatorsByOrgId(inventoryId)).thenReturn(repositoryResult);
        when(mapper.inPhysicalEquipmentLowImpacttoDTO(repositoryResult)).thenReturn(mappedResult);

        List<PhysicalEquipmentLowImpactBO> result = service.getPhysicalEquipmentsLowImpact(subscriber, organizationId, inventoryId);

        assertEquals(mappedResult, result);
        verify(organizationService).getOrganizationById(organizationId);
        verify(lowImpactViewRepository).findPhysicalEquipmentLowImpactIndicatorsByOrgId(inventoryId);
        verify(mapper).inPhysicalEquipmentLowImpacttoDTO(repositoryResult);
    }

    @Test
    void getPhysicalEquipmentElecConsumptionReturnsMappedIndicatorsWhenValidInputs() {
        Long taskId = 1L;
        Long criteriaNumber = 2L;
        List<InPhysicalEquipmentElecConsumptionView> repositoryResult = List.of(InPhysicalEquipmentElecConsumptionView.builder().elecConsumption(32.0).build());
        List<PhysicalEquipmentElecConsumptionBO> mappedResult = List.of(new PhysicalEquipmentElecConsumptionBO());

        when(elecConsumptionViewRepository.findPhysicalEquipmentElecConsumptionIndicators(taskId, criteriaNumber)).thenReturn(repositoryResult);
        when(mapper.inPhysicalEquipmentElecConsumptionToDto(repositoryResult)).thenReturn(mappedResult);

        List<PhysicalEquipmentElecConsumptionBO> result = service.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber);

        assertEquals(mappedResult, result);
        verify(elecConsumptionViewRepository).findPhysicalEquipmentElecConsumptionIndicators(taskId, criteriaNumber);
        verify(mapper).inPhysicalEquipmentElecConsumptionToDto(repositoryResult);
    }
}
