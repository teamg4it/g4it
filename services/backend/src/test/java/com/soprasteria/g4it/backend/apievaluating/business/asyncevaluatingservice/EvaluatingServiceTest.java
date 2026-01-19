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
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
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
    @Mock
    private ExportService exportService;

    // ------------------ NEW TESTS START ------------------

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
        UserBO userBO = UserBO.builder().id(USER_ID).email("test@soprasteria.com").domain("soprasteria.com").build();
        User user = User.builder().id(USER_ID).build();
        DigitalService digitalService = mock(DigitalService.class);
        DigitalServiceVersion digitalServiceVersion = mock(DigitalServiceVersion.class);
        CriteriaByType criteriaByType = mock(CriteriaByType.class);

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.of(digitalServiceVersion));
        when(digitalServiceVersion.getDigitalService()).thenReturn(digitalService);
        when(digitalServiceVersion.getDescription()).thenReturn("v1");
        when(digitalService.getUid()).thenReturn("ds-uid");
        when(digitalService.getName()).thenReturn("ds-name");

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(work);
        when(work.getName()).thenReturn(WORKSPACE);

        when(digitalServiceVersion.getCriteria()).thenReturn(null);

        when(criteriaService.getSelectedCriteriaForDigitalService(any(), any(), any()))
                .thenReturn(criteriaByType);
        when(criteriaByType.active()).thenReturn(Collections.emptyList());

        when(authService.getUser()).thenReturn(userBO);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArguments()[0]);

        Task task = evaluatingService.evaluatingDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_VERSION_UID);

        assertNotNull(task);
        assertEquals(Constants.CRITERIA_LIST.subList(0, 5), task.getCriteria());

        verify(digitalServiceRepository).save(any(DigitalService.class));
        verify(digitalServiceVersionRepository).save(any(DigitalServiceVersion.class));
        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    // ------------------ NEW TESTS END ------------------
}
