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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    AsyncEvaluatingService asyncEvaluatingService;

    private Context context;
    private Task task;

    @BeforeEach
    void setup() {
        context = mock(Context.class);
        task = mock(Task.class);

        when(task.getId()).thenReturn(101L);
        when(context.log()).thenReturn("ORG/WS/DS");

        // These are not used in all tests -> lenient
        lenient().when(context.getOrganization()).thenReturn("ORG");
        lenient().when(context.getWorkspaceId()).thenReturn(999L);
    }

    @Test
    void execute_shouldRunEvaluateAiFlow_whenContextIsAiTrue() throws Exception {
        when(context.isAi()).thenReturn(true);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        asyncEvaluatingService.execute(context, task);

        verify(taskRepository).updateTaskState(
                eq(101L),
                eq(TaskStatus.IN_PROGRESS.toString()),
                any(),
                eq("0%")
        );

        verify(evaluateAiService).doEvaluateAi(eq(context), eq(task), eq(exportDir));
        verify(evaluateService, never()).doEvaluate(any(), any(), any());

        verify(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        verify(exportService).clean(eq(101L));

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

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        asyncEvaluatingService.execute(context, task);

        verify(taskRepository).updateTaskState(
                eq(101L),
                eq(TaskStatus.IN_PROGRESS.toString()),
                any(),
                eq("0%")
        );

        verify(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));
        verify(evaluateAiService, never()).doEvaluateAi(any(), any(), any());

        verify(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        verify(exportService).clean(eq(101L));

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.COMPLETED.toString()),
                eq("100%"),
                anyList()
        );

        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenEvaluateThrowsAsyncTaskException() throws Exception {
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
        assertFalse(details.isEmpty());
        assertTrue(details.stream().anyMatch(s -> s.contains("Start task")));
        assertTrue(details.stream().anyMatch(s -> s.contains("boom-async")));

        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenEvaluateThrowsRuntimeException() throws Exception {
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
    void execute_shouldMarkFailed_whenExportUploadThrowsUncheckedIOException() throws Exception {
        when(context.isAi()).thenReturn(false);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));

        doThrow(new UncheckedIOException("boom-io", new java.io.IOException("boom-io")))
                .when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));

        asyncEvaluatingService.execute(context, task);

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
        assertTrue(details.stream().anyMatch(s -> s.contains("boom-io")));

        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldAlwaysSetDetails_evenWhenCreateExportDirectoryFails() throws Exception {

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
    void execute_shouldMarkFailed_whenEvaluateAiThrowsAsyncTaskException() throws Exception {
        when(context.isAi()).thenReturn(true);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doThrow(new AsyncTaskException("boom-ai"))
                .when(evaluateAiService).doEvaluateAi(eq(context), eq(task), eq(exportDir));

        asyncEvaluatingService.execute(context, task);

        verify(evaluateAiService).doEvaluateAi(eq(context), eq(task), eq(exportDir));
        verify(evaluateService, never()).doEvaluate(any(), any(), any());

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
    void execute_shouldMarkFailed_whenCleanThrowsException() throws Exception {
        when(context.isAi()).thenReturn(false);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));

        doNothing().when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));

        doThrow(new RuntimeException("clean-fail"))
                .when(exportService).clean(eq(101L));

        asyncEvaluatingService.execute(context, task);

        verify(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        verify(exportService).clean(eq(101L));

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );

        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldMarkFailed_whenWorkspaceIdIsNull() throws Exception {
        when(context.isAi()).thenReturn(false);
        when(context.getWorkspaceId()).thenReturn(null); // will trigger NPE inside execute()

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        asyncEvaluatingService.execute(context, task);

        // since it fails before upload, these should NOT be called
        verify(exportService, never()).uploadExportZip(anyLong(), anyString(), anyString());
        verify(exportService, never()).clean(anyLong());

        // should mark FAILED
        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );

        verify(task).setDetails(anyList());
    }


    @Test
    void execute_shouldThrow_whenUpdateTaskFinalStateFails() throws Exception {
        when(context.isAi()).thenReturn(false);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));
        doNothing().when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        doNothing().when(exportService).clean(eq(101L));

        doThrow(new RuntimeException("final-state-fail"))
                .when(taskRepository).updateTaskFinalState(eq(101L), anyString(), anyString(), anyList());

        assertThrows(RuntimeException.class, () -> asyncEvaluatingService.execute(context, task));

        verify(taskRepository).updateTaskFinalState(eq(101L), anyString(), anyString(), anyList());
        verify(task).setDetails(anyList());
    }

    @Test
    void execute_shouldThrow_whenUpdateFinalStateCompletedThrows() throws Exception {
        when(context.isAi()).thenReturn(false);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));
        doNothing().when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        doNothing().when(exportService).clean(eq(101L));

        doThrow(new RuntimeException("final-fail"))
                .when(taskRepository)
                .updateTaskFinalState(eq(101L), eq(TaskStatus.COMPLETED.toString()), eq("100%"), anyList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> asyncEvaluatingService.execute(context, task));

        assertEquals("final-fail", ex.getMessage());

        // upload + clean already happened before final update
        verify(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        verify(exportService).clean(eq(101L));

        // completed final update attempted
        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.COMPLETED.toString()),
                eq("100%"),
                anyList()
        );

        // details should still be set (depends on your finally block)
        verify(task).setDetails(anyList());
    }


    @Test
    void execute_shouldMarkFailed_whenCleanThrowsAfterUpload() throws Exception {
        when(context.isAi()).thenReturn(false);

        Path exportDir = Path.of("target/test-export/101");
        when(exportService.createExportDirectory(101L)).thenReturn(exportDir);

        doNothing().when(evaluateService).doEvaluate(eq(context), eq(task), eq(exportDir));

        doNothing().when(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));

        doThrow(new RuntimeException("clean-crash"))
                .when(exportService).clean(eq(101L));

        asyncEvaluatingService.execute(context, task);

        verify(exportService).uploadExportZip(eq(101L), eq("ORG"), eq("999"));
        verify(exportService).clean(eq(101L));

        verify(taskRepository).updateTaskFinalState(
                eq(101L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );

        verify(task).setDetails(anyList());
    }

}
