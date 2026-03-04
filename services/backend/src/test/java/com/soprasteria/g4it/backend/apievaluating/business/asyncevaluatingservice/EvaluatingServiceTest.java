package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaByType;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.task.model.BackgroundTask;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.task.TaskExecutor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluatingServiceTest {

    static final long USER_ID = 1;
    static final String ORGANIZATION = "organization";
    static final String WORKSPACE = "workspace";
    static final Long WORKSPACE_ID = 1L;
    static final Long INVENTORY_ID = 2L;
    static final String DIGITAL_SERVICE_VERSION_UID = "90651485-3f8b-49dd-a7be-753e4fe1fd36";

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
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CriteriaService criteriaService;

    @Mock
    private AuthService authService;
    @Mock
    private ExportService exportService;
    @Mock
    private AsyncEvaluatingService asyncEvaluatingService;


    @Test
    void evaluating_shouldThrow404_whenInventoryNotFound() {
        when(inventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.empty());

        G4itRestException ex = assertThrows(G4itRestException.class,
                () -> evaluatingService.evaluating(ORGANIZATION, WORKSPACE_ID, INVENTORY_ID));

        assertEquals("404", ex.getCode());
    }

    @Test
    void evaluating_shouldFallbackToDefaultCriteria_whenActiveCriteriaEmpty() {
        Inventory inventory = mock(Inventory.class);
        Workspace work = mock(Workspace.class);
        CriteriaByType criteriaByType = mock(CriteriaByType.class);

        when(inventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventory));
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(work);
        when(work.getName()).thenReturn(WORKSPACE);

        when(inventory.getVirtualEquipmentCount()).thenReturn(0L);
        when(inventory.getApplicationCount()).thenReturn(0L);
        when(inventory.getCriteria()).thenReturn(null);

        when(criteriaService.getSelectedCriteriaForInventory(any(), any(), any()))
                .thenReturn(criteriaByType);
        when(criteriaByType.active()).thenReturn(Collections.emptyList());

        UserBO userBO = UserBO.builder().id(USER_ID).email("x@y.com").domain("y.com").build();
        when(authService.getUser()).thenReturn(userBO);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        Task task = evaluatingService.evaluating(ORGANIZATION, WORKSPACE_ID, INVENTORY_ID);

        assertNotNull(task);
        assertNotNull(task.getCriteria());
        assertEquals(5, task.getCriteria().size()); // fallback default 5
        assertEquals(Constants.CRITERIA_LIST.subList(0, 5), task.getCriteria());

        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void restartEvaluating_shouldDoNothing_whenNoTasks() {
        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.EVALUATING.toString()))
                .thenReturn(Collections.emptyList());

        evaluatingService.restartEvaluating();

        verify(taskRepository, never()).updateTaskStateWithDetails(anyLong(), any(), any(), any(), anyList());
        verify(taskExecutor, never()).execute(any());
    }

    @Test
    void restartEvaluating_shouldSkip_whenInventoryIsNull() {
        Task task = mock(Task.class);

        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.EVALUATING.toString()))
                .thenReturn(List.of(task));

        when(task.getInventory()).thenReturn(null);

        evaluatingService.restartEvaluating();

        verify(taskExecutor, never()).execute(any());
        verify(taskRepository, never()).updateTaskState(anyLong(), any(), any(), any());
    }

    @Test
    void restartEvaluating_shouldSkip_whenLastUpdateIsRecent() {
        Task task = mock(Task.class);
        Inventory inventory = mock(Inventory.class);

        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.EVALUATING.toString()))
                .thenReturn(List.of(task));

        when(task.getInventory()).thenReturn(inventory);
        when(task.getLastUpdateDate()).thenReturn(LocalDateTime.now().minusMinutes(5)); // NOT eligible

        evaluatingService.restartEvaluating();

        verify(taskExecutor, never()).execute(any());
        verify(taskRepository, never()).updateTaskStateWithDetails(anyLong(), any(), any(), any(), anyList());
    }

    @Test
    void restartEvaluating_shouldSkip_whenTaskAlreadyRunningExceptionOccurs() {
        Task task = mock(Task.class);
        Inventory inventory = mock(Inventory.class);
        Workspace workspace = mock(Workspace.class);
        Organization org = mock(Organization.class);

        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.EVALUATING.toString()))
                .thenReturn(List.of(task));

        when(task.getInventory()).thenReturn(inventory);
        when(task.getLastUpdateDate()).thenReturn(LocalDateTime.now().minusMinutes(20));
        when(inventory.getWorkspace()).thenReturn(workspace);
        when(workspace.getOrganization()).thenReturn(org);
        when(org.getName()).thenReturn(ORGANIZATION);
        when(workspace.getId()).thenReturn(WORKSPACE_ID);
        when(workspace.getName()).thenReturn(WORKSPACE);

        when(inventory.getId()).thenReturn(INVENTORY_ID);
        when(inventory.getVirtualEquipmentCount()).thenReturn(0L);
        when(inventory.getApplicationCount()).thenReturn(0L);

        // manageInventoryTasks checks this list and throws task.already.running
        when(taskRepository.findByInventoryAndStatusAndType(any(), any(), any()))
                .thenReturn(List.of(mock(Task.class)));

        evaluatingService.restartEvaluating();

        verify(taskExecutor, never()).execute(any());
    }

    @Test
    void restartEvaluating_shouldRethrow_whenManageInventoryTasksThrowsOtherException() {
        Task task = mock(Task.class);
        Inventory inventory = mock(Inventory.class);

        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.EVALUATING.toString()))
                .thenReturn(List.of(task));

        when(task.getInventory()).thenReturn(inventory);
        when(task.getLastUpdateDate()).thenReturn(LocalDateTime.now().minusMinutes(20));

        // Exception happens inside manageInventoryTasks()
        when(taskRepository.findByInventoryAndStatusAndType(any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> evaluatingService.restartEvaluating());

        verify(taskExecutor, never()).execute(any());
    }


    @Test
    void evaluatingDigitalService_shouldThrow404_whenVersionNotFound() {
        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID))
                .thenReturn(Optional.empty());

        G4itRestException ex = assertThrows(G4itRestException.class,
                () -> evaluatingService.evaluatingDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_VERSION_UID));

        assertEquals("404", ex.getCode());
    }

    @Test
    void evaluatingDigitalService_shouldFallbackToDefaultCriteria_whenActiveCriteriaEmpty() {
        Workspace work = mock(Workspace.class);
        UserBO userBO = UserBO.builder().id(USER_ID).build();
        User user = User.builder().id(USER_ID).build();
        DigitalService digitalService = mock(DigitalService.class);
        DigitalServiceVersion digitalServiceVersion = mock(DigitalServiceVersion.class);
        CriteriaByType criteriaByType = mock(CriteriaByType.class);

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(digitalServiceVersion.getDigitalService()).thenReturn(digitalService);
        when(digitalServiceVersion.getDescription()).thenReturn("v1");
        when(digitalService.getUid()).thenReturn("ds-uid");
        when(digitalService.getName()).thenReturn("ds-name");
        when(digitalService.isAi()).thenReturn(false);

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(work);
        when(work.getName()).thenReturn(WORKSPACE);

        when(criteriaService.getSelectedCriteriaForDigitalService(any(), any(), any()))
                .thenReturn(criteriaByType);
        when(criteriaByType.active()).thenReturn(Collections.emptyList());

        when(authService.getUser()).thenReturn(userBO);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        // ***** CRITICAL FIX *****
        when(taskRepository.findById(100L)).thenReturn(Optional.of(
                Task.builder()
                        .id(100L)
                        .criteria(Constants.CRITERIA_LIST.subList(0, 5))
                        .build()
        ));

        Task task = evaluatingService.evaluatingDigitalService(
                ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_VERSION_UID);

        assertNotNull(task);
        assertEquals(5, task.getCriteria().size());

        verify(asyncEvaluatingService).execute(any(), any());
    }


    @Test
    void evaluating_shouldThrow409_whenTaskAlreadyRunningForInventory() {
        Inventory inventory = mock(Inventory.class);

        when(inventoryRepository.findById(10L)).thenReturn(Optional.of(inventory));
        when(taskRepository.findByInventoryAndStatusAndType(
                inventory,
                TaskStatus.IN_PROGRESS.toString(),
                TaskType.EVALUATING.toString()
        )).thenReturn(List.of(mock(Task.class)));

        G4itRestException ex = assertThrows(G4itRestException.class,
                () -> evaluatingService.evaluating("ORG", 1L, 10L));

        assertEquals("409", ex.getCode());
        assertEquals("task.already.running", ex.getMessage());

        verify(taskExecutor, never()).execute(any());
    }

    @Test
    void evaluating_shouldDeleteOldTasksAndCleanExport_whenMoreThanTwoTasksExist() {
        Inventory inventory = mock(Inventory.class);
        Workspace workspace = mock(Workspace.class);

        User user = mock(User.class);
        UserBO userBO = mock(UserBO.class);

        when(inventoryRepository.findById(10L)).thenReturn(Optional.of(inventory));
        when(inventory.getVirtualEquipmentCount()).thenReturn(1L);
        when(inventory.getApplicationCount()).thenReturn(1L);

        // No task already running
        when(taskRepository.findByInventoryAndStatusAndType(
                inventory,
                TaskStatus.IN_PROGRESS.toString(),
                TaskType.EVALUATING.toString()
        )).thenReturn(List.of());

        Task t1 = Task.builder().id(1L).build();
        Task t2 = Task.builder().id(2L).build();
        Task t3 = Task.builder().id(3L).build();
        Task t4 = Task.builder().id(4L).build();

        when(taskRepository.findByInventoryAndType(inventory, TaskType.EVALUATING.toString()))
                .thenReturn(List.of(t1, t2, t3, t4));

        when(workspaceService.getWorkspaceById(1L)).thenReturn(workspace);
        when(workspace.getName()).thenReturn("WS");

        when(criteriaService.getSelectedCriteriaForInventory(anyString(), anyLong(), any()).active())
                .thenReturn(List.of("C1"));

        when(authService.getUser()).thenReturn(userBO);
        when(userBO.getId()).thenReturn(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task task = inv.getArgument(0);
            task.setId(999L);
            return task;
        });

        evaluatingService.evaluating("ORG", 1L, 10L);

        verify(taskRepository).deleteTask(2L);
        verify(taskRepository).deleteTask(1L);

        verify(exportService).cleanExport(2L, "ORG", "1");
        verify(exportService).cleanExport(1L, "ORG", "1");

        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void evaluating_shouldUseDefault5Criteria_whenActiveCriteriaEmpty() {
        Inventory inventory = mock(Inventory.class);
        Workspace workspace = mock(Workspace.class);

        User user = mock(User.class);
        UserBO userBO = mock(UserBO.class);

        when(inventoryRepository.findById(10L)).thenReturn(Optional.of(inventory));
        when(inventory.getVirtualEquipmentCount()).thenReturn(1L);
        when(inventory.getApplicationCount()).thenReturn(1L);

        when(taskRepository.findByInventoryAndStatusAndType(any(), anyString(), anyString()))
                .thenReturn(List.of());

        when(taskRepository.findByInventoryAndType(any(), anyString()))
                .thenReturn(List.of()); // no cleanup

        when(workspaceService.getWorkspaceById(1L)).thenReturn(workspace);
        when(workspace.getName()).thenReturn("WS");

        when(criteriaService.getSelectedCriteriaForInventory(anyString(), anyLong(), any()).active())
                .thenReturn(List.of());

        when(authService.getUser()).thenReturn(userBO);
        when(userBO.getId()).thenReturn(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task task = inv.getArgument(0);
            task.setId(100L);
            return task;
        });

        evaluatingService.evaluating("ORG", 1L, 10L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        Task createdTask = captor.getValue();
        assertNotNull(createdTask.getCriteria());
        assertEquals(5, createdTask.getCriteria().size());

        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void restartEvaluating_shouldRestartEligibleTask_whenLastUpdateOlderThan15Min() {
        Task task = mock(Task.class);
        Inventory inventory = mock(Inventory.class);
        Workspace workspace = mock(Workspace.class);
        Organization org = mock(Organization.class);

        when(taskRepository.findByStatusAndType(
                TaskStatus.IN_PROGRESS.toString(),
                TaskType.EVALUATING.toString()
        )).thenReturn(List.of(task));

        when(task.getId()).thenReturn(101L);
        when(task.getInventory()).thenReturn(inventory);

        when(task.getLastUpdateDate()).thenReturn(LocalDateTime.now().minusMinutes(20));

        when(inventory.getWorkspace()).thenReturn(workspace);
        when(workspace.getOrganization()).thenReturn(org);
        when(org.getName()).thenReturn(ORGANIZATION);

        when(workspace.getId()).thenReturn(WORKSPACE_ID);
        when(workspace.getName()).thenReturn(WORKSPACE);

        when(inventory.getId()).thenReturn(INVENTORY_ID);
        when(inventory.getVirtualEquipmentCount()).thenReturn(1L);
        when(inventory.getApplicationCount()).thenReturn(0L);

        when(taskRepository.findByInventoryAndStatusAndType(
                inventory,
                TaskStatus.IN_PROGRESS.toString(),
                TaskType.EVALUATING.toString()
        )).thenReturn(Collections.emptyList());

        when(taskRepository.findByInventoryAndType(inventory, TaskType.EVALUATING.toString()))
                .thenReturn(Collections.emptyList());

        evaluatingService.restartEvaluating();

        ArgumentCaptor<LocalDateTime> toStartTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass((Class) List.class);

        verify(taskRepository).updateTaskStateWithDetails(
                eq(101L),
                eq(TaskStatus.TO_START.toString()),
                toStartTimeCaptor.capture(),
                eq("0%"),
                detailsCaptor.capture()
        );

        ArgumentCaptor<LocalDateTime> inProgressTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(taskRepository).updateTaskState(
                eq(101L),
                eq(TaskStatus.IN_PROGRESS.toString()),
                inProgressTimeCaptor.capture(),
                eq("0%")
        );

        verify(taskExecutor).execute(any(BackgroundTask.class));
    }


    @Test
    void evaluatingDigitalService_shouldUseActiveCriteria_whenActiveCriteriaPresent() {
        Workspace work = mock(Workspace.class);
        DigitalService digitalService = mock(DigitalService.class);
        DigitalServiceVersion digitalServiceVersion = mock(DigitalServiceVersion.class);
        CriteriaByType criteriaByType = mock(CriteriaByType.class);

        UserBO userBO = UserBO.builder().id(USER_ID).build();
        User user = User.builder().id(USER_ID).build();

        List<String> activeCriteria = List.of("C1", "C2");

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(digitalServiceVersion.getDigitalService()).thenReturn(digitalService);
        when(digitalServiceVersion.getDescription()).thenReturn("v1");
        when(digitalService.getUid()).thenReturn("ds-uid");
        when(digitalService.getName()).thenReturn("ds-name");
        when(digitalService.isAi()).thenReturn(false);

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(work);
        when(work.getName()).thenReturn(WORKSPACE);

        when(criteriaService.getSelectedCriteriaForDigitalService(any(), any(), any()))
                .thenReturn(criteriaByType);
        when(criteriaByType.active()).thenReturn(activeCriteria);

        when(authService.getUser()).thenReturn(userBO);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task t = i.getArgument(0);
            t.setId(200L);
            return t;
        });

        // ***** CRITICAL FIX *****
        when(taskRepository.findById(200L)).thenReturn(Optional.of(
                Task.builder()
                        .id(200L)
                        .criteria(activeCriteria)
                        .build()
        ));

        Task task = evaluatingService.evaluatingDigitalService(
                ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_VERSION_UID);

        assertNotNull(task);
        assertEquals(activeCriteria, task.getCriteria());

        verify(asyncEvaluatingService).execute(any(), any());
    }


}
