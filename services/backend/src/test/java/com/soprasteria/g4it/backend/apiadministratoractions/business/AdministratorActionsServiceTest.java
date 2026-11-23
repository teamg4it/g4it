/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.AllEvaluationStatusRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AdministratorActionsService}, in particular the resilience
 * fix in {@code evaluateAllDigitalServices()} that prevents one failing
 * digital-service from aborting the whole batch (Checkmarx Improper Exception
 * Handling finding).
 */
@ExtendWith(MockitoExtension.class)
class AdministratorActionsServiceTest {

    @Mock
    private EvaluatingService evaluatingService;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private InPhysicalEquipmentRepository physicalEquipmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private AdministratorActionsService administratorActionsService;

    private Workspace buildWorkspace(final long id) {
        Organization organization = Organization.builder().name("ORG").build();
        return Workspace.builder().id(id).name("WS").organization(organization).build();
    }

    private DigitalService buildDigitalService(final String uid, final Workspace workspace) {
        return DigitalService.builder().uid(uid).name("DS-" + uid).workspace(workspace).build();
    }

    private InPhysicalEquipment buildEquipment(final String type) {
        return InPhysicalEquipment.builder().type(type).build();
    }

    @Test
    void evaluateAllDigitalServices_withNetworkEquipmentAndNoPriorTask_triggersEvaluation() {
        Workspace workspace = buildWorkspace(1L);
        DigitalService digitalService = buildDigitalService("ds-1", workspace);

        when(digitalServiceRepository.findAll()).thenReturn(List.of(digitalService));
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(physicalEquipmentRepository.findByDigitalServiceUid("ds-1"))
                .thenReturn(List.of(buildEquipment("Network")));
        when(taskRepository.findByDigitalService(digitalService)).thenReturn(Optional.empty());

        AllEvaluationStatusRest result = administratorActionsService.evaluateAllDigitalServices();

        assertNotNull(result);
        assertEquals("success", result.getResponse());
        verify(evaluatingService, times(1)).evaluatingDigitalService("ORG", 1L, "ds-1");
    }

    @Test
    void evaluateAllDigitalServices_withoutNetworkEquipment_skipsEvaluationAndCountsMissing() {
        Workspace workspace = buildWorkspace(1L);
        DigitalService digitalService = buildDigitalService("ds-1", workspace);

        when(digitalServiceRepository.findAll()).thenReturn(List.of(digitalService));
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(physicalEquipmentRepository.findByDigitalServiceUid("ds-1"))
                .thenReturn(List.of(buildEquipment("Server")));
        when(taskRepository.findByDigitalService(digitalService)).thenReturn(Optional.empty());

        administratorActionsService.evaluateAllDigitalServices();

        verify(evaluatingService, never()).evaluatingDigitalService(anyString(), any(), anyString());
    }

    @Test
    void evaluateAllDigitalServices_alreadyCompletedToday_skipsReEvaluation() {
        Workspace workspace = buildWorkspace(1L);
        DigitalService digitalService = buildDigitalService("ds-1", workspace);

        Task completedToday = Task.builder()
                .status("COMPLETED")
                .lastUpdateDate(LocalDateTime.now())
                .build();

        when(digitalServiceRepository.findAll()).thenReturn(List.of(digitalService));
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(physicalEquipmentRepository.findByDigitalServiceUid("ds-1"))
                .thenReturn(List.of(buildEquipment("Network")));
        when(taskRepository.findByDigitalService(digitalService)).thenReturn(Optional.of(completedToday));

        administratorActionsService.evaluateAllDigitalServices();

        verify(evaluatingService, never()).evaluatingDigitalService(anyString(), any(), anyString());
    }

    @Test
    void evaluateAllDigitalServices_withNoPhysicalEquipment_doesNotEvaluate() {
        Workspace workspace = buildWorkspace(1L);
        DigitalService digitalService = buildDigitalService("ds-1", workspace);

        when(digitalServiceRepository.findAll()).thenReturn(List.of(digitalService));
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(physicalEquipmentRepository.findByDigitalServiceUid("ds-1")).thenReturn(List.of());

        administratorActionsService.evaluateAllDigitalServices();

        verify(taskRepository, never()).findByDigitalService(any());
        verify(evaluatingService, never()).evaluatingDigitalService(anyString(), any(), anyString());
    }

    @Test
    void evaluateAllDigitalServices_whenOneDigitalServiceFails_continuesProcessingOthers() {
        Workspace workspace = buildWorkspace(1L);
        DigitalService failingDigitalService = buildDigitalService("ds-failing", workspace);
        DigitalService okDigitalService = buildDigitalService("ds-ok", workspace);

        when(digitalServiceRepository.findAll()).thenReturn(List.of(failingDigitalService, okDigitalService));

        // Simulate the workspace lookup failing for the first digital-service (e.g. deleted workspace)
        when(workspaceRepository.findById(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(workspace));
        when(physicalEquipmentRepository.findByDigitalServiceUid("ds-ok"))
                .thenReturn(List.of(buildEquipment("Network")));
        when(taskRepository.findByDigitalService(okDigitalService)).thenReturn(Optional.empty());

        AllEvaluationStatusRest result = administratorActionsService.evaluateAllDigitalServices();

        // The whole batch must not abort: the second (healthy) digital-service is still evaluated
        assertNotNull(result);
        assertEquals("success", result.getResponse());
        verify(evaluatingService, times(1)).evaluatingDigitalService(eq("ORG"), eq(1L), eq("ds-ok"));
        verify(physicalEquipmentRepository, never()).findByDigitalServiceUid("ds-failing");
    }

    @Test
    void evaluateAllDigitalServices_withEmptyRepository_returnsSuccessWithoutError() {
        when(digitalServiceRepository.findAll()).thenReturn(List.of());

        AllEvaluationStatusRest result = administratorActionsService.evaluateAllDigitalServices();

        assertNotNull(result);
        assertEquals("success", result.getResponse());
        verifyNoInteractions(evaluatingService);
    }
}
