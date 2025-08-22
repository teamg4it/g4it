/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaByType;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.BackgroundTask;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluatingServiceTest {
    static final long USER_ID = 1;
    static final String SUBSCRIBER = "subscriber";
    static final String ORGANIZATION = "organization";
    static final Long ORGANIZATION_ID = 1L;
    static final Long INVENTORY_ID = 2L;
    static final String DIGITAL_SERVICE_UID = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
    static final List<String> CRITERIA = List.of("criteria1", "criteria2");

    @InjectMocks
    private EvaluatingService evaluatingService;

    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private CriteriaService criteriaService;
    @Mock
    private AuthService authService;
    @Mock
    private AsyncEvaluatingService asyncEvaluatingService;

    @Test
    void evaluating_shouldCreateAndReturnTask() {

        final Inventory inventory = mock(Inventory.class);
        final Workspace org = mock(Workspace.class);
        final CriteriaByType criteriaByType = mock(CriteriaByType.class);

        when(inventory.getVirtualEquipmentCount()).thenReturn(1L);
        when(inventory.getApplicationCount()).thenReturn(1L);
        when(inventory.getCriteria()).thenReturn(null);
        when(org.getName()).thenReturn(ORGANIZATION);
        when(criteriaByType.active()).thenReturn(CRITERIA);

        // Test data setup
        UserBO userBO = UserBO.builder()
                .email("testuser@soprasteria.com")
                .domain("soprasteria.com")
                .id(USER_ID)
                .firstName("fname")
                .build();

        final User user = User.builder().id(USER_ID).build();

        // Stub repository and service methods
        when(inventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventory));
        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(org);
        when(criteriaService.getSelectedCriteriaForInventory(any(), any(), any())).thenReturn(criteriaByType);
        when(criteriaByType.active()).thenReturn(CRITERIA);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(authService.getUser()).thenReturn(userBO);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        Task result = evaluatingService.evaluating(SUBSCRIBER, ORGANIZATION_ID, INVENTORY_ID);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void evaluatingDigitalService_shouldCreateAndReturnTask() {

        Workspace org = mock(Workspace.class);
        UserBO userBO = UserBO.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(USER_ID).firstName("fname").build();
        User user = User.builder().id(USER_ID).build();
        DigitalService digitalService = mock(DigitalService.class);
        CriteriaByType criteriaByType = mock(CriteriaByType.class);

        when(criteriaByType.active()).thenReturn(CRITERIA);
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        when(digitalService.getName()).thenReturn("digitalService");
        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(org);
        when(org.getName()).thenReturn(ORGANIZATION);
        when(criteriaService.getSelectedCriteriaForDigitalService(any(), any(), any())).thenReturn(criteriaByType);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(authService.getUser()).thenReturn(userBO);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        when(digitalService.getCriteria()).thenReturn(null);

        Task result = evaluatingService.evaluatingDigitalService(SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        assertNotNull(result);
        verify(asyncEvaluatingService).execute(any(Context.class), any(Task.class));
        verify(digitalServiceRepository).save(any(DigitalService.class));
    }

    @Test
    void evaluatingDigitalServiceAi_shouldCreateAndReturnTask() {

        Workspace org = mock(Workspace.class);
        UserBO userBO = UserBO.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(USER_ID).firstName("fname").build();
        User user = User.builder().id(USER_ID).build();
        DigitalService digitalService = mock(DigitalService.class);
        CriteriaByType criteriaByType = mock(CriteriaByType.class);

        when(criteriaByType.active()).thenReturn(CRITERIA);
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        when(digitalService.getName()).thenReturn("digitalService");
        when(digitalService.isAi()).thenReturn(true);
        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(org);
        when(org.getName()).thenReturn(ORGANIZATION);
        when(criteriaService.getSelectedCriteriaForDigitalService(any(), any(), any())).thenReturn(criteriaByType);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(authService.getUser()).thenReturn(userBO);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        when(digitalService.getCriteria()).thenReturn(null);

        Task result = evaluatingService.evaluatingDigitalService(SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        assertNotNull(result);
        verify(asyncEvaluatingService).execute(any(Context.class), any(Task.class));
        verify(digitalServiceRepository).save(any(DigitalService.class));
    }

    @Test
    void evaluating_shouldThrowIfTaskAlreadyRunning() {

        Task task = mock(Task.class);
        final Inventory inventory = mock(Inventory.class);

        when(inventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventory));
        when(taskRepository.findByInventoryAndStatusAndType(any(), any(), any()))
                .thenReturn(Collections.singletonList(task));

        assertThrows(G4itRestException.class,
                () -> evaluatingService.evaluating(SUBSCRIBER, ORGANIZATION_ID, INVENTORY_ID));
    }

    @Test
    void restartEvaluating_shouldRestartEligibleTasks() {
        // Arrange
        Task task = mock(Task.class);
        final Inventory inventory = mock(Inventory.class);
        final Workspace org = mock(Workspace.class);
        final Subscriber subscriber = mock(Subscriber.class);

        when(taskRepository.findByStatusAndType(any(), any()))
                .thenReturn(Collections.singletonList(task));
        when(task.getLastUpdateDate()).thenReturn(LocalDateTime.now().minusMinutes(20));
        when(task.getInventory()).thenReturn(inventory);
        when(inventory.getWorkspace()).thenReturn(org);
        when(org.getSubscriber()).thenReturn(subscriber);
        when(org.getSubscriber().getName()).thenReturn(SUBSCRIBER);
        when(org.getId()).thenReturn(ORGANIZATION_ID);
        when(org.getName()).thenReturn(ORGANIZATION);
        when(inventory.getId()).thenReturn(INVENTORY_ID);
        when(inventory.getVirtualEquipmentCount()).thenReturn(0L);
        when(inventory.getApplicationCount()).thenReturn(0L);

        evaluatingService.restartEvaluating();

        verify(taskRepository).save(any(Task.class));
        verify(taskExecutor).execute(any(BackgroundTask.class));
    }
}
