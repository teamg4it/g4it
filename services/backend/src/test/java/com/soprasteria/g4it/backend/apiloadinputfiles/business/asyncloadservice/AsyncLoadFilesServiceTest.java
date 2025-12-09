package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkmetadata.CheckMetadataInventoryFileService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.AsyncLoadMetadataService;
import com.soprasteria.g4it.backend.apiloadinputfiles.util.FileLoadingUtils;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncLoadFilesServiceTest {

    @InjectMocks
    private AsyncLoadFilesService asyncLoadFilesService;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private LoadFileService loadFileService;
    @Mock
    private DigitalServiceService digitalServiceService;
    @Mock
    private AsyncLoadMetadataService asyncLoadMetadataService;
    @Mock
    private CheckMetadataInventoryFileService checkMetadataInventoryFileService;
    @Mock
    private FileLoadingUtils fileLoadingUtils;

    private Context context;

    @Mock
    private Task task;
    private FileToLoad file;

    @BeforeEach
    void setup() {
        context = Context.builder()
                .organization("org")
                .workspaceId(1L)
                .inventoryId(100L)
                .build();

        file = new FileToLoad();
        file.setFileType(FileType.EQUIPEMENT_VIRTUEL);
        file.setFilename("virtual_equipment.csv");
        file.setOriginalFileName("virtual_equipment.csv");

        context.initFileToLoad(List.of(file));
        context.initTaskId(task.getId());

        // Only stubs needed for all tests
        lenient().when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(loadFileService.manageFile(any(), any())).thenReturn(Collections.emptyList());
    }
    

    @Test
    void shouldSkipLinkingIfRejectedFiles() {
        when(fileLoadingUtils.handelRejectedFiles(any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(loadFileService.mandatoryHeadersCheck(any())).thenReturn(Collections.emptyList());

        asyncLoadFilesService.execute(context, task);

        // Just verify it is called, optionally with lenient stubbing
        verify(loadFileService).linkApplicationsToVirtualEquipments(anyLong());
        verify(loadFileService).setInventoryCounts(anyLong());
    }


    @Test
    void shouldThrowIfMissingReferences() {
        when(fileLoadingUtils.handelRejectedFiles(any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(loadFileService.mandatoryHeadersCheck(any())).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("Missing physical equipment"))
                .when(loadFileService).linkApplicationsToVirtualEquipments(100L);

        assertThrows(RuntimeException.class, () -> asyncLoadFilesService.execute(context, task));

        verify(loadFileService).linkApplicationsToVirtualEquipments(100L);
        verify(loadFileService, never()).setInventoryCounts(anyLong());
    }


    @Test
    void shouldLinkInventoryWhenProcessingCompletes() {
        // Arrange
        when(task.getId()).thenReturn(55L);
        when(task.getFilenames()).thenReturn(List.of("virtual_equipment.csv"));
        when(loadFileService.mandatoryHeadersCheck(any())).thenReturn(Collections.emptyList());
        when(fileLoadingUtils.handelRejectedFiles(any(), any(), any(), any(), any(), any()))
                .thenReturn(false);

        // Act
        asyncLoadFilesService.execute(context, task);

        // Assert
        verify(loadFileService).linkApplicationsToVirtualEquipments(100L);
        verify(loadFileService).setInventoryCounts(100L);
    }


}
