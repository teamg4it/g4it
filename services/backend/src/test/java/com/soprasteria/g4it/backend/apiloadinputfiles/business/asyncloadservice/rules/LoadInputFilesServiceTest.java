/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.LoadInputFilesService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.AsyncLoadFilesService;
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
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private FileSystemService fileSystemService;

    @Mock
    private AsyncLoadFilesService asyncLoadFilesService;

    @InjectMocks
    private LoadInputFilesService loadInputFilesService;

    @Mock
    private AuthService authService;

    private static final LocalDateTime referenceTime =
            LocalDateTime.of(2025, Month.JANUARY, 1, 12, 0);

    @BeforeEach
    void setup() throws Exception {

        String tempDir = System.getProperty("java.io.tmpdir");

        ReflectionTestUtils.setField(
                loadInputFilesService,
                "localWorkingFolder",
                tempDir);

        Files.createDirectories(
                Path.of(tempDir, "input", "inventory"));

        Files.createDirectories(
                Path.of(tempDir, "input", "digital-service"));
    }

    /*@Test
    void loadFiles_createsTaskAndExecutesAsyncTask_whenValidInputProvided() {
        String organization = "testOrganization";
        Long workspaceId = 1L;
        Long inventoryId = 1L;

        List<MultipartFile> datacenters = List.of(
                new MockMultipartFile(
                        "datacenters",
                        "datacenters.csv",
                        "text/csv",
                        "header1,header2\nvalue1,value2".getBytes()
                ));

        List<MultipartFile> physicalEquipments = List.of(
                new MockMultipartFile(
                        "physicalEquipments",
                        "physical.csv",
                        "text/csv",
                        "header1,header2\nvalue1,value2".getBytes()
                ));

        List<MultipartFile> virtualEquipments = List.of(
                new MockMultipartFile(
                        "virtualEquipments",
                        "virtual.csv",
                        "text/csv",
                        "header1,header2\nvalue1,value2".getBytes()
                ));

        List<MultipartFile> applications = List.of(
                new MockMultipartFile(
                        "applications",
                        "applications.csv",
                        "text/csv",
                        "header1,header2\nvalue1,value2".getBytes()
                ));

        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .virtualEquipmentCount(1L)
                .applicationCount(1L)
                .createdBy(User.builder()
                        .id(1L)
                        .firstName("test")
                        .lastName("user")
                        .email("test.user@gmail.com")
                        .build())
                .build();

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .name("Test Workspace")
                .build();

        UserBO userBO = UserBO.builder()
                .email("testuser@soprasteria.com")
                .domain("soprasteria.com")
                .id(1L)
                .firstName("fname")
                .build();

        User user = User.builder()
                .email("testuser@soprasteria.com")
                .domain("soprasteria.com")
                .id(1L)
                .firstName("fname")
                .build();

        when(inventoryRepository.findById(inventoryId))
                .thenReturn(Optional.of(inventory));
        when(workspaceService.getWorkspaceById(workspaceId))
                .thenReturn(workspace);
        when(taskRepository.findByInventoryAndStatusAndType(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(authService.getUser())
                .thenReturn(userBO);
        when(userRepository.findById(userBO.getId()))
                .thenReturn(Optional.of(user));

        Task result = loadInputFilesService.loadFiles(
                organization,
                workspaceId,
                inventoryId,
                datacenters,
                physicalEquipments,
                virtualEquipments,
                applications);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(asyncLoadFilesService).execute(any(), any());
    }*/

    @Test
    void digitalServiceLoadFiles_createsTaskAndExecutesAsyncTask_whenValidInputProvided() {
        String organization = "testOrganization";
        Long workspaceId = 1L;
        String digitalServiceUid = "uid";

        List<MultipartFile> datacenters = List.of(
                new MockMultipartFile(
                        "datacenters",
                        "datacenters.csv",
                        "text/csv",
                        "header1,header2\nvalue1,value2".getBytes()
                ));

        List<MultipartFile> physicalEquipments = List.of(
                new MockMultipartFile(
                        "physicalEquipments",
                        "physical.csv",
                        "text/csv",
                        "header1,header2\nvalue1,value2".getBytes()
                ));

        List<MultipartFile> virtualEquipments = List.of(
                new MockMultipartFile(
                        "virtualEquipments",
                        "virtual.csv",
                        "text/csv",
                        "header1,header2\nvalue1,value2".getBytes()
                ));

        DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder()
                .uid(digitalServiceUid)
                .build();

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .name("Test Workspace")
                .build();

        UserBO userBO = UserBO.builder()
                .email("testuser@soprasteria.com")
                .domain("soprasteria.com")
                .id(1L)
                .firstName("fname")
                .build();

        User user = User.builder()
                .email("testuser@soprasteria.com")
                .domain("soprasteria.com")
                .id(1L)
                .firstName("fname")
                .build();

        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(workspaceService.getWorkspaceById(workspaceId))
                .thenReturn(workspace);
        when(taskRepository.findByDigitalServiceVersionAndStatusAndType(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(authService.getUser())
                .thenReturn(userBO);
        when(userRepository.findById(userBO.getId()))
                .thenReturn(Optional.of(user));

        Task result = loadInputFilesService.loadDigitalServiceFiles(
                organization,
                workspaceId,
                digitalServiceUid,
                datacenters,
                physicalEquipments,
                virtualEquipments);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(asyncLoadFilesService).execute(any(), any());
    }

    /*@Test
    void loadFiles_returnsEmptyTask_whenNoFilesProvided() {
        String organization = "testOrganization";
        Long workspaceId = 1L;
        Long inventoryId = 1L;

        Task result = loadInputFilesService.loadFiles(organization, workspaceId, inventoryId, null, null, null, null,null);

        assertNotNull(result);
        assertNull(result.getId());
        verifyNoInteractions(taskRepository, taskExecutor);
    }*/

    @Test
    void restartInventory_LoadingFiles_restartsTasks_whenTasksAreStale() {
        Task staleTask = Task.builder()
                .id(1L)
                .lastUpdateDate(referenceTime.minusMinutes(20))
                .status(TaskStatus.IN_PROGRESS.toString())
                .type(TaskType.LOADING.toString())
                .inventory(Inventory.builder()
                        .id(1L)
                        .workspace(Workspace.builder()
                                .id(1L)
                                .name("Test Workspace")
                                .organization(Organization.builder().name("testOrganization").build())
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

        DigitalService digitalService = DigitalService.builder()
                .uid("dsuid")
                .workspace(Workspace.builder()
                        .id(1L)
                        .name("Test Workspace")
                        .organization(Organization.builder().name("testOrganization").build())
                        .build())
                .build();
        DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().uid("uid")
                .digitalService(digitalService).build();
        Task staleTask = Task.builder()
                .id(1L)
                .lastUpdateDate(referenceTime.minusMinutes(20))
                .status(TaskStatus.IN_PROGRESS.toString())
                .type(TaskType.LOADING.toString())
                .digitalServiceVersion(digitalServiceVersion)
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

    @Test
    void loadFiles_throwsInternalServerError_whenMultipartFileCannotBeRead() throws Exception {
        Long inventoryId = 1L;

        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .virtualEquipmentCount(0L)
                .applicationCount(0L)
                .build();

        Workspace workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .build();

        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.getOriginalFilename())
                .thenReturn("test.csv");

        when(multipartFile.getInputStream())
                .thenThrow(new java.io.IOException("Unable to read file"));

        when(inventoryRepository.findById(inventoryId))
                .thenReturn(Optional.of(inventory));

        when(taskRepository.findByInventoryAndStatusAndType(
                any(),
                any(),
                any()
        )).thenReturn(Collections.emptyList());

        when(workspaceService.getWorkspaceById(1L))
                .thenReturn(workspace);

        assertThrows(
                ResponseStatusException.class,
                () -> loadInputFilesService.loadFiles(
                        "testOrganization",
                        1L,
                        inventoryId,
                        List.of(multipartFile),
                        null,
                        null,
                        null,
                        null
                )
        );

        verify(multipartFile).getInputStream();
    }

    @Test
    void restartLoadingFiles_doesNothing_whenTaskIsNotStale() {
        // Given
        Task recentTask = Task.builder()
                .id(1L)
                .lastUpdateDate(LocalDateTime.now().minusMinutes(5))
                .status(TaskStatus.IN_PROGRESS.toString())
                .type(TaskType.LOADING.toString())
                .build();

        when(taskRepository.findByStatusAndType(
                TaskStatus.IN_PROGRESS.toString(),
                TaskType.LOADING.toString()
        )).thenReturn(List.of(recentTask));

        // When
        loadInputFilesService.restartLoadingFiles();

        // Then
        verify(taskRepository, never()).save(any());
        verify(taskExecutor, never()).execute(any());
    }

    @Test
    void loadFiles_shouldHandleOnlyDatacenterFile() {
        // Given
        String organization = "testOrganization";
        Long workspaceId = 1L;
        Long inventoryId = 1L;

        MultipartFile datacenter = new MockMultipartFile(
                "datacenters",
                "datacenter.csv",
                "text/csv",
                "header1,header2\nvalue1,value2".getBytes()
        );

        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .virtualEquipmentCount(0L)
                .applicationCount(0L)
                .build();

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .name("Test Workspace")
                .build();

        UserBO userBO = UserBO.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .build();

        when(inventoryRepository.findById(inventoryId))
                .thenReturn(Optional.of(inventory));

        when(taskRepository.findByInventoryAndStatusAndType(
                any(),
                any(),
                any()
        )).thenReturn(Collections.emptyList());

        when(workspaceService.getWorkspaceById(workspaceId))
                .thenReturn(workspace);

        when(authService.getUser())
                .thenReturn(userBO);

        when(userRepository.findById(userBO.getId()))
                .thenReturn(Optional.of(user));

        // When
        Task result = loadInputFilesService.loadFiles(
                organization,
                workspaceId,
                inventoryId,
                List.of(datacenter),
                null,
                null,
                null,
                null
        );

        // Then
        assertNotNull(result);

        verify(taskRepository).save(any(Task.class));
        verify(taskExecutor).execute(any(Runnable.class));
    }

    @Test
    void loadDigitalServiceFiles_throwsException_whenLoadingTaskAlreadyRunning() {
        // Given
        String digitalServiceUid = "uid";

        DigitalServiceVersion digitalServiceVersion =
                DigitalServiceVersion.builder()
                        .uid(digitalServiceUid)
                        .build();

        MultipartFile file = new MockMultipartFile(
                "datacenters",
                "datacenter.csv",
                "text/csv",
                "header1,header2\nvalue1,value2".getBytes()
        );

        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));

        when(taskRepository.findByDigitalServiceVersionAndStatusAndType(
                eq(digitalServiceVersion),
                eq(TaskStatus.IN_PROGRESS.toString()),
                eq(TaskType.LOADING.toString())
        )).thenReturn(List.of(mock(Task.class)));

        // When / Then
        assertThrows(
                G4itRestException.class,
                () -> loadInputFilesService.loadDigitalServiceFiles(
                        "testOrganization",
                        1L,
                        digitalServiceUid,
                        List.of(file),
                        null,
                        null
                )
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void loadFiles_throwsException_whenLoadingTaskAlreadyRunning() {
        // Given
        Long inventoryId = 1L;

        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .virtualEquipmentCount(0L)
                .applicationCount(0L)
                .build();

        MultipartFile file = new MockMultipartFile(
                "datacenters",
                "datacenter.csv",
                "text/csv",
                "header1,header2\nvalue1,value2".getBytes()
        );

        when(inventoryRepository.findById(inventoryId))
                .thenReturn(Optional.of(inventory));

        when(taskRepository.findByInventoryAndStatusAndType(
                eq(inventory),
                eq(TaskStatus.IN_PROGRESS.toString()),
                eq(TaskType.LOADING.toString())
        )).thenReturn(List.of(mock(Task.class)));

        // When / Then
        assertThrows(
                G4itRestException.class,
                () -> loadInputFilesService.loadFiles(
                        "testOrganization",
                        1L,
                        inventoryId,
                        List.of(file),
                        null,
                        null,
                        null,
                        null
                )
        );

        verify(taskRepository).findByInventoryAndStatusAndType(
                inventory,
                TaskStatus.IN_PROGRESS.toString(),
                TaskType.LOADING.toString()
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void loadFiles_returnsEmptyTask_whenNoFilesProvided() {
        // When
        Task result = loadInputFilesService.loadFiles(
                "testOrganization",
                1L,
                1L,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertNotNull(result);
        assertNull(result.getId());

        verifyNoInteractions(
                inventoryRepository,
                taskRepository,
                taskExecutor,
                asyncLoadFilesService
        );
    }

    @Test
    void loadFiles_returnsEmptyTask_whenNoFilesProvidedForGivenInventory() {
        // Given
        Long inventoryId = 1L;

        // When
        Task result = loadInputFilesService.loadFiles(
                "testOrganization",
                1L,
                inventoryId,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertNotNull(result);
        assertNull(result.getId());

        verifyNoInteractions(
                inventoryRepository,
                taskRepository,
                taskExecutor,
                asyncLoadFilesService
        );
    }


}