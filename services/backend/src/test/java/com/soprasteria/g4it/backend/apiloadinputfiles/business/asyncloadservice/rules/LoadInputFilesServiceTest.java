/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.LoadInputFilesService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.task.model.BackgroundTask;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadInputFilesServiceTest {

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
    private FileSystemService fileSystemService;

    @InjectMocks
    private LoadInputFilesService loadInputFilesService;

    @Mock
    private AuthService authService;

    @Test
    void loadFiles_createsTaskAndExecutesAsyncTask_whenValidInputProvided() {
        String subscriber = "testSubscriber";
        Long organizationId = 1L;
        Long inventoryId = 1L;
        List<MultipartFile> datacenters = List.of(mock(MultipartFile.class));
        List<MultipartFile> physicalEquipments = List.of(mock(MultipartFile.class));
        List<MultipartFile> virtualEquipments = List.of(mock(MultipartFile.class));
        List<MultipartFile> applications = List.of(mock(MultipartFile.class));

        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .virtualEquipmentCount(1L)
                .applicationCount(1L)
                .createdBy(User.builder().id(1L).firstName("test").lastName("user").email("test.user@gmail.com").build())
                .build();

        Workspace workspace = Workspace.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        UserBO userBO = UserBO.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(1L).firstName("fname").build();
        User user = User.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(1L).firstName("fname").build();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(workspaceService.getWorkspaceById(organizationId)).thenReturn(workspace);
        when(taskRepository.findByInventoryAndStatusAndType(any(), any(), any())).thenReturn(Collections.emptyList());
        when(authService.getUser()).thenReturn(userBO);
        when(userRepository.findById(userBO.getId())).thenReturn(Optional.ofNullable(user));

        Task result = loadInputFilesService.loadFiles(subscriber, organizationId, inventoryId, datacenters, physicalEquipments, virtualEquipments, applications);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void digitalServiceLoadFiles_createsTaskAndExecutesAsyncTask_whenValidInputProvided() {
        String subscriber = "testSubscriber";
        Long organizationId = 1L;
        String digitalServiceUid = "uid";
        List<MultipartFile> datacenters = List.of(mock(MultipartFile.class));
        List<MultipartFile> physicalEquipments = List.of(mock(MultipartFile.class));
        List<MultipartFile> virtualEquipments = List.of(mock(MultipartFile.class));

        DigitalService digitalService = DigitalService.builder()
                .uid(digitalServiceUid)
                .build();

        Workspace workspace = Workspace.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        UserBO userBO = UserBO.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(1L).firstName("fname").build();
        User user = User.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(1L).firstName("fname").build();

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.of(digitalService));
        when(workspaceService.getWorkspaceById(organizationId)).thenReturn(workspace);
        when(taskRepository.findByDigitalServiceAndStatusAndType(any(), any(), any())).thenReturn(Collections.emptyList());
        when(authService.getUser()).thenReturn(userBO);
        when(userRepository.findById(userBO.getId())).thenReturn(Optional.ofNullable(user));

        Task result = loadInputFilesService.loadDigitalServiceFiles(subscriber, organizationId, digitalServiceUid, datacenters, physicalEquipments, virtualEquipments);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void loadFiles_returnsEmptyTask_whenNoFilesProvided() {
        String subscriber = "testSubscriber";
        Long organizationId = 1L;
        Long inventoryId = 1L;

        Task result = loadInputFilesService.loadFiles(subscriber, organizationId, inventoryId, null, null, null, null);

        assertNotNull(result);
        assertNull(result.getId());
        verifyNoInteractions(taskRepository, taskExecutor);
    }

    @Test
    void restartInventory_LoadingFiles_restartsTasks_whenTasksAreStale() {
        Task staleTask = Task.builder()
                .id(1L)
                .lastUpdateDate(LocalDateTime.now().minusMinutes(20))
                .status(TaskStatus.IN_PROGRESS.toString())
                .type(TaskType.LOADING.toString())
                .inventory(Inventory.builder()
                        .id(1L)
                        .workspace(Workspace.builder()
                                .id(1L)
                                .name("Test Organization")
                                .organization(Organization.builder().name("testSubscriber").build())
                                .build())
                        .virtualEquipmentCount(1L)
                        .applicationCount(1L)
                        .build())
                .build();

        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString()))
                .thenReturn(List.of(staleTask));

        loadInputFilesService.restartLoadingFiles();

        verify(taskRepository).save(any(Task.class));
        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void restartDigitalService_LoadingFiles_restartsTasks_whenTasksAreStale() {
        Task staleTask = Task.builder()
                .id(1L)
                .lastUpdateDate(LocalDateTime.now().minusMinutes(20))
                .status(TaskStatus.IN_PROGRESS.toString())
                .type(TaskType.LOADING.toString())
                .digitalService(DigitalService.builder()
                        .uid("uid")
                        .workspace(Workspace.builder()
                                .id(1L)
                                .name("Test Organization")
                                .organization(Organization.builder().name("testSubscriber").build())
                                .build())
                        .build())
                .build();

        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString()))
                .thenReturn(List.of(staleTask));

        loadInputFilesService.restartLoadingFiles();

        verify(taskRepository).save(any(Task.class));
        verify(taskExecutor).execute(any(BackgroundTask.class));
    }

    @Test
    void restartLoadingFiles_doesNothing_whenNoStaleTasks() {
        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString()))
                .thenReturn(Collections.emptyList());

        loadInputFilesService.restartLoadingFiles();

        verifyNoInteractions(taskExecutor);
    }
}