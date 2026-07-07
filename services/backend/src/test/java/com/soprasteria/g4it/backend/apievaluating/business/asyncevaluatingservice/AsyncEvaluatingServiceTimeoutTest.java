/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.business.TaskTimeoutMonitor;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.TaskTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncEvaluatingServiceTimeoutTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EvaluateService evaluateService;

    @Mock
    private EvaluateAiService evaluateAiService;

    @Mock
    private ExportService exportService;

    @Mock
    private TaskTimeoutMonitor taskTimeoutMonitor;

    @Mock
    private MessageSource messageSource;

    @Mock
    private Context mockContext;

    @InjectMocks
    private AsyncEvaluatingService asyncEvaluatingService;

    private Context context;
    private Task task;

    @BeforeEach
    void setUp() {
        context = Context.builder()
                .organization("test-org")
                .workspaceId(1L)
                .workspaceName("test-workspace")
                .inventoryId(1L)
                .locale(Locale.ENGLISH)
                .datetime(LocalDateTime.now())
                .build();

        task = Task.builder()
                .id(1L)
                .type(TaskType.EVALUATING.toString())
                .status(TaskStatus.TO_START.toString())
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .details(new ArrayList<>())
                .build();
    }

    @Test
    void execute_shouldCheckTimeoutAtStrategicPoints() throws Exception {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(exportService.createExportDirectory(anyLong())).thenReturn(null);
        doNothing().when(taskTimeoutMonitor).checkTaskTimeout(anyLong());
        doNothing().when(taskRepository).updateTaskState(anyLong(), anyString(), any(), anyString());
        doNothing().when(taskRepository).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());

        // When
        asyncEvaluatingService.execute(context, task);

        // Then - Verify timeout was checked at 4 strategic points
        verify(taskTimeoutMonitor, times(4)).checkTaskTimeout(1L);
    }

    @Test
    void execute_shouldHandleTimeoutException() throws Exception {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doThrow(new TaskTimeoutException("Task timed out"))
                .when(taskTimeoutMonitor).checkTaskTimeout(anyLong());
        when(messageSource.getMessage(eq("import.timeout"), isNull(), any(Locale.class)))
                .thenReturn("Evaluation process timed out");
        doNothing().when(taskRepository).updateTaskState(anyLong(), anyString(), any(), anyString());
        doNothing().when(taskRepository).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());

        // When
        asyncEvaluatingService.execute(context, task);

        // Then
        verify(taskRepository).updateTaskFinalState(
                eq(1L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );
    }

    @Test
    void execute_shouldUseFallbackMessageWhenLocalizationFails() throws Exception {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doThrow(new TaskTimeoutException("Task timed out"))
                .when(taskTimeoutMonitor).checkTaskTimeout(anyLong());
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenThrow(new RuntimeException("Message not found"));
        doNothing().when(taskRepository).updateTaskState(anyLong(), anyString(), any(), anyString());
        doNothing().when(taskRepository).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());

        // When
        asyncEvaluatingService.execute(context, task);

        // Then - Should still mark as FAILED with fallback message
        verify(taskRepository).updateTaskFinalState(
                eq(1L),
                eq(TaskStatus.FAILED.toString()),
                eq("0%"),
                anyList()
        );
    }

    @Test
    void execute_shouldNotCheckTimeoutAfterExceptionInEvaluation() throws Exception {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(exportService.createExportDirectory(anyLong())).thenReturn(null);
        doThrow(new RuntimeException("Evaluation error"))
                .when(evaluateService).doEvaluate(any(), any(), any());
        doNothing().when(taskRepository).updateTaskState(anyLong(), anyString(), any(), anyString());
        doNothing().when(taskRepository).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());

        // When
        asyncEvaluatingService.execute(context, task);

        // Then - Only 2 timeout checks before the exception
        verify(taskTimeoutMonitor, times(2)).checkTaskTimeout(1L);
    }

    @Test
    void execute_shouldCheckTimeoutForAiEvaluation() throws Exception {
        // Given
        Context aiContext = Context.builder()
                .organization("test-org")
                .workspaceId(1L)
                .isAi(true)
                .locale(Locale.ENGLISH)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(exportService.createExportDirectory(anyLong())).thenReturn(null);
        doNothing().when(taskTimeoutMonitor).checkTaskTimeout(anyLong());
        doNothing().when(taskRepository).updateTaskState(anyLong(), anyString(), any(), anyString());
        doNothing().when(taskRepository).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());

        // When
        asyncEvaluatingService.execute(aiContext, task);

        // Then - Verify timeout was checked for AI evaluation
        verify(taskTimeoutMonitor, times(4)).checkTaskTimeout(1L);
        verify(evaluateAiService).doEvaluateAi(eq(aiContext), eq(task), any());
    }

    @Test
    void execute_shouldUseEnglishLocaleWhenContextLocaleIsNull() throws Exception {
        // Given
        Context noLocaleContext = Context.builder()
                .organization("test-org")
                .workspaceId(1L)
                .locale(null)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doThrow(new TaskTimeoutException("Task timed out"))
                .when(taskTimeoutMonitor).checkTaskTimeout(anyLong());
        when(messageSource.getMessage(eq("import.timeout"), isNull(), eq(Locale.ENGLISH)))
                .thenReturn("Evaluation process timed out");
        doNothing().when(taskRepository).updateTaskState(anyLong(), anyString(), any(), anyString());
        doNothing().when(taskRepository).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());

        // When
        asyncEvaluatingService.execute(noLocaleContext, task);

        // Then
        verify(messageSource).getMessage(eq("import.timeout"), isNull(), eq(Locale.ENGLISH));
    }

    @Test
    void execute_shouldCheckTimeoutBeforeAndAfterExport() throws Exception {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(exportService.createExportDirectory(anyLong())).thenReturn(null);
        doNothing().when(taskTimeoutMonitor).checkTaskTimeout(anyLong());
        doNothing().when(taskRepository).updateTaskState(anyLong(), anyString(), any(), anyString());
        doNothing().when(taskRepository).updateTaskFinalState(anyLong(), anyString(), anyString(), anyList());

        // When
        asyncEvaluatingService.execute(context, task);

        // Then - Verify order of operations
        var inOrder = inOrder(taskTimeoutMonitor, evaluateService, exportService);
        inOrder.verify(taskTimeoutMonitor).checkTaskTimeout(1L); // Before evaluation
        inOrder.verify(taskTimeoutMonitor).checkTaskTimeout(1L); // Before evaluation
        inOrder.verify(evaluateService).doEvaluate(any(), any(), any());
        inOrder.verify(taskTimeoutMonitor).checkTaskTimeout(1L); // After evaluation
        inOrder.verify(exportService).uploadExportZip(anyLong(), anyString(), anyString());
        inOrder.verify(taskTimeoutMonitor).checkTaskTimeout(1L); // After export
    }
}

