package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class AsyncEvaluatingServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    EvaluateService evaluateService;

    @Mock
    EvaluateAiService evaluateAiService;

    @Mock
    ExportService exportService;

    @Mock
    Context context;

    @Mock
    Task task;

    @InjectMocks
    AsyncEvaluatingService asyncEvaluatingService;

    @BeforeEach
    void setup() {
        when(task.getId()).thenReturn(101L);
        when(context.log()).thenReturn("ORG/WS/DS");
    }

    private void stubOrgAndWorkspace() {
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getWorkspaceId()).thenReturn(999L);
    }

    @Test
    void execute_shouldRunEvaluateAiFlow_whenContextIsAiTrue() throws Exception {
        when(context.isAi()).thenReturn(true);
        stubOrgAndWorkspace();

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        asyncEvaluatingService.execute(context, task);

        verify(taskRepository).updateTaskState(eq(101L), eq(TaskStatus.IN_PROGRESS.toString()), any(), eq("0%"));

        verify(evaluateAiService).doEvaluateAi(context, task, exportDir);
        verify(evaluateService, never()).doEvaluate(any(), any(), any());

        verify(exportService).uploadExportZip(101L, "ORG", "999");
        verify(exportService).clean(101L);

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.COMPLETED.toString()),
                eq("100%"),
                anyList()
        );
        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldRunEvaluateNormalFlow_whenContextIsAiFalse() throws Exception {
        when(context.isAi()).thenReturn(false);
        stubOrgAndWorkspace();

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        asyncEvaluatingService.execute(context, task);

        verify(taskRepository).updateTaskState(eq(101L), eq(TaskStatus.IN_PROGRESS.toString()), any(), eq("0%"));

        verify(evaluateService).doEvaluate(context, task, exportDir);
        verify(evaluateAiService, never()).doEvaluateAi(any(), any(), any());

        verify(exportService).uploadExportZip(101L, "ORG", "999");
        verify(exportService).clean(101L);

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.COMPLETED.toString()),
                eq("100%"),
                anyList()
        );
        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenEvaluateThrowsAsyncTaskException() {
        when(context.isAi()).thenReturn(false);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doThrow(new AsyncTaskException("boom-async"))
                .when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));

        asyncEvaluatingService.execute(context, task);

        verify(exportService, never()).uploadExportZip(anyLong(), anyString(), anyString());
        verify(exportService, never()).clean(anyLong());

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                detailsCaptor.capture()
        );

        List<String> details = detailsCaptor.getValue();
        assertNotNull(details);
        assertTrue(details.stream().anyMatch(s -> s.contains("Start task")));
        assertTrue(details.stream().anyMatch(s -> s.contains("boom-async")));

        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenEvaluateThrowsRuntimeException() {
        when(context.isAi()).thenReturn(false);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doThrow(new RuntimeException("boom-runtime"))
                .when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));

        asyncEvaluatingService.execute(context, task);

        verify(exportService, never()).uploadExportZip(anyLong(), anyString(), anyString());
        verify(exportService, never()).clean(anyLong());

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                detailsCaptor.capture()
        );

        List<String> details = detailsCaptor.getValue();
        assertNotNull(details);
        assertTrue(details.stream().anyMatch(s -> s.contains("boom-runtime")));

        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenExportUploadThrowsUncheckedIOException() {
        when(context.isAi()).thenReturn(false);
        stubOrgAndWorkspace();

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));

        doThrow(new UncheckedIOException("boom-io", new java.io.IOException("boom-io")))
                .when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));

        asyncEvaluatingService.execute(context, task);

        verify(exportService, never()).clean(anyLong());

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );
        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldAlwaysSetDetails_evenWhenCreateExportDirectoryFails() throws IOException {
        when(exportService.createExportDirectory(101L))
                .thenThrow(new RuntimeException("dir-fail"));

        asyncEvaluatingService.execute(context, task);

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );
        verify(task).setDetails(anyList());

        verify(evaluateService, never()).doEvaluate(any(), any(), any());
        verify(evaluateAiService, never()).doEvaluateAi(any(), any(), any());
        verify(exportService, never()).uploadExportZip(anyLong(), anyString(), anyString());
        verify(exportService, never()).clean(anyLong());
    }

    @Test
    void execute_shouldMarkFailed_whenEvaluateAiThrowsRuntimeException() throws Exception {
        when(context.isAi()).thenReturn(true);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doThrow(new RuntimeException("boom-ai-runtime"))
                .when(evaluateAiService).doEvaluateAi(eq(context), eq(task), eq(exportDir));

        asyncEvaluatingService.execute(context, task);

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );
        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenCleanThrowsException() {
        when(context.isAi()).thenReturn(false);
        stubOrgAndWorkspace();

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));
        doNothing().when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));

        doThrow(new RuntimeException("clean-fail"))
                .when(exportService).clean(eq(101L));

        asyncEvaluatingService.execute(context, task);

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );
        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenWorkspaceIdIsNull() {
        when(context.isAi()).thenReturn(false);
        when(context.getWorkspaceId()).thenReturn(null);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        asyncEvaluatingService.execute(context, task);

        verify(exportService, never()).uploadExportZip(anyLong(), anyString(), anyString());
        verify(exportService, never()).clean(anyLong());

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );
        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldThrow_whenUpdateTaskStateThrows() {
        doThrow(new RuntimeException("state-fail"))
                .when(taskRepository)
                .updateTaskState(eq(101L), eq(TaskStatus.IN_PROGRESS.toString()), any(), eq("0%"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> asyncEvaluatingService.execute(context, task));

        assertEquals("state-fail", ex.getMessage());

        verify(exportService, never()).createExportDirectory(anyLong());
        verify(taskRepository, never()).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());
    }

    @Test
    void execute_shouldUpdateTaskStateBeforeCallingEvaluate() {
        when(context.isAi()).thenReturn(false);
        stubOrgAndWorkspace();

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));
        doNothing().when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        doNothing().when(exportService).clean(eq(101L));

        asyncEvaluatingService.execute(context, task);

        InOrder inOrder = inOrder(taskRepository, exportService, evaluateService);

        inOrder.verify(taskRepository).updateTaskState(eq(101L), eq(TaskStatus.IN_PROGRESS.toString()), any(), eq("0%"));
        inOrder.verify(exportService).createExportDirectory(101L);
        inOrder.verify(evaluateService).doEvaluate(context, task, exportDir);
        inOrder.verify(exportService).uploadExportZip(101L, "ORG", "999");
        inOrder.verify(exportService).clean(101L);
    }

    @Test
    void execute_shouldFollowCorrectOrder_forAiFlow() throws Exception {
        when(context.isAi()).thenReturn(true);
        stubOrgAndWorkspace();

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateAiService).doEvaluateAi(eq(context), eq(task), eq(exportDir));
        doNothing().when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        doNothing().when(exportService).clean(eq(101L));

        asyncEvaluatingService.execute(context, task);

        InOrder inOrder = inOrder(taskRepository, exportService, evaluateAiService);

        inOrder.verify(taskRepository).updateTaskState(eq(101L), eq(TaskStatus.IN_PROGRESS.toString()), any(), eq("0%"));
        inOrder.verify(exportService).createExportDirectory(101L);
        inOrder.verify(evaluateAiService).doEvaluateAi(context, task, exportDir);
        inOrder.verify(exportService).uploadExportZip(101L, "ORG", "999");
        inOrder.verify(exportService).clean(101L);

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.COMPLETED.toString()),
                eq("100%"),
                anyList()
        );
    }
}
