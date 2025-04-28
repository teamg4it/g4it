/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.LoadInputFilesService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.AsyncLoadFilesService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
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
    private OrganizationService organizationService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private AsyncLoadFilesService asyncLoadFilesService;

    @Mock
    private FileSystemService fileSystemService;

    @InjectMocks
    private LoadInputFilesService loadInputFilesService;

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

        Organization organization = Organization.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(organizationService.getOrganizationById(organizationId)).thenReturn(organization);
        when(taskRepository.findByInventoryAndStatusAndType(any(), any(), any())).thenReturn(Collections.emptyList());

        Task result = loadInputFilesService.loadFiles(subscriber, organizationId, inventoryId, datacenters, physicalEquipments, virtualEquipments, applications);

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
    void restartLoadingFiles_restartsTasks_whenTasksAreStale() {
        Task staleTask = Task.builder()
                .id(1L)
                .lastUpdateDate(LocalDateTime.now().minusMinutes(20))
                .status(TaskStatus.IN_PROGRESS.toString())
                .type(TaskType.LOADING.toString())
                .inventory(Inventory.builder()
                        .id(1L)
                        .organization(Organization.builder()
                                .id(1L)
                                .name("Test Organization")
                                .subscriber(Subscriber.builder().name("testSubscriber").build())
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
    void restartLoadingFiles_doesNothing_whenNoStaleTasks() {
        when(taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString()))
                .thenReturn(Collections.emptyList());

        loadInputFilesService.restartLoadingFiles();

        verifyNoInteractions(taskExecutor);
    }
}