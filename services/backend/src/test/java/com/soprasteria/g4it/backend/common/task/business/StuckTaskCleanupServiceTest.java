/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.business;

import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StuckTaskCleanupServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private StuckTaskCleanupService stuckTaskCleanupService;

    @BeforeEach
    void setUp() {
        // Set default configuration values
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskTimeoutHours", 2.0);
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskCheckEnabled", true);
    }

    @Test
    void failStuckTasks_shouldDoNothingWhenCheckIsDisabled() {
        // Given
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskCheckEnabled", false);

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        verify(taskRepository, never()).findByStatus(any());
    }

    @Test
    void failStuckTasks_shouldDoNothingWhenNoInProgressTasks() {
        // Given
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(Collections.emptyList());

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        verify(taskRepository).findByStatus(TaskStatus.IN_PROGRESS.toString());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void failStuckTasks_shouldNotFailTasksWithinTimeout() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task recentTask = createTask(1L, TaskType.LOADING, now.minusMinutes(30)); // Only 30 minutes old

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(recentTask));

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        verify(taskRepository).findByStatus(TaskStatus.IN_PROGRESS.toString());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void failStuckTasks_shouldFailTasksExceedingTimeout() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask = createTask(1L, TaskType.LOADING, now.minusHours(3)); // 3 hours old (timeout is 2)

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(stuckTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been inactive for 3 hour(s) and has been automatically terminated.");

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertEquals(TaskStatus.FAILED.toString(), savedTask.getStatus());
        assertNotNull(savedTask.getDetails());
        assertFalse(savedTask.getDetails().isEmpty());
        assertNotNull(savedTask.getErrors());
        assertFalse(savedTask.getErrors().isEmpty());
    }

    @Test
    void failStuckTasks_shouldFailMultipleStuckTasks() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask1 = createTask(1L, TaskType.LOADING, now.minusHours(3));
        Task stuckTask2 = createTask(2L, TaskType.EVALUATING, now.minusHours(5));
        Task recentTask = createTask(3L, TaskType.LOADING, now.minusMinutes(30));

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(Arrays.asList(stuckTask1, stuckTask2, recentTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been stuck");

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        verify(taskRepository, times(2)).save(any(Task.class)); // Only 2 stuck tasks should be saved
    }

    @Test
    void failStuckTasks_shouldUpdateLastUpdateDate() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask = createTask(1L, TaskType.LOADING, now.minusHours(3));
        LocalDateTime originalLastUpdate = stuckTask.getLastUpdateDate();

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(stuckTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been stuck");

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertTrue(savedTask.getLastUpdateDate().isAfter(originalLastUpdate));
    }

    @Test
    void failStuckTasks_shouldAddErrorMessageToDetails() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask = createTask(1L, TaskType.LOADING, now.minusHours(3));
        String expectedErrorMessage = "Task has been inactive for 3 hour(s)";

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(stuckTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn(expectedErrorMessage);

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertNotNull(savedTask.getDetails());
        assertTrue(savedTask.getDetails().stream()
                .anyMatch(detail -> detail.contains(expectedErrorMessage)));
    }

    @Test
    void failStuckTasks_shouldHandleTaskWithNullLastUpdateDate() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask = Task.builder()
                .id(1L)
                .type(TaskType.LOADING.toString())
                .status(TaskStatus.IN_PROGRESS.toString())
                .creationDate(now.minusHours(3))
                .lastUpdateDate(null) // Null last update date
                .details(new ArrayList<>())
                .build();

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(stuckTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been stuck");

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        verify(taskRepository).save(any(Task.class)); // Should use creation date as fallback
    }

    @Test
    void failStuckTasks_shouldPreserveCurrentProgressPercentage() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask = createTask(1L, TaskType.LOADING, now.minusHours(3));
        stuckTask.setProgressPercentage("45%");
        String originalProgress = stuckTask.getProgressPercentage();

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(stuckTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been stuck");

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertEquals(originalProgress, savedTask.getProgressPercentage());
    }

    @Test
    void failStuckTasks_shouldHandleExceptionGracefully() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask1 = createTask(1L, TaskType.LOADING, now.minusHours(3));
        Task stuckTask2 = createTask(2L, TaskType.EVALUATING, now.minusHours(4));

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(Arrays.asList(stuckTask1, stuckTask2));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been stuck");

        // Make first save throw exception, second one succeeds
        when(taskRepository.save(any(Task.class)))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(stuckTask2);

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then - should still process the second task despite first one failing
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    @Test
    void failStuckTasks_shouldUseConfiguredTimeout() {
        // Given
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskTimeoutHours", 0.5); // 30 minutes
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask = createTask(1L, TaskType.LOADING, now.minusMinutes(45)); // 45 minutes old

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(stuckTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been stuck");

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        verify(taskRepository).save(any(Task.class)); // Should be marked as stuck with 30-minute timeout
    }

    @Test
    void failStuckTasks_shouldNotFailTaskJustWithinTimeout() {
        // Given
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskTimeoutHours", 1.0);
        LocalDateTime now = LocalDateTime.now();
        Task task = createTask(1L, TaskType.LOADING, now.minusMinutes(59)); // Just under 1 hour

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        verify(taskRepository, never()).save(any());
    }

    @Test
    void failStuckTasks_shouldInitializeDetailsListIfNull() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Task stuckTask = Task.builder()
                .id(1L)
                .type(TaskType.LOADING.toString())
                .status(TaskStatus.IN_PROGRESS.toString())
                .creationDate(now.minusHours(1))
                .lastUpdateDate(now.minusHours(3))
                .details(null) // Null details
                .build();

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(stuckTask));
        when(messageSource.getMessage(eq("task.stuck.timeout"), any(), any(), any()))
                .thenReturn("Task has been stuck");

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertNotNull(savedTask.getDetails()); // Should be initialized
    }

    @Test
    void getStuckTaskTimeoutHours_shouldReturnConfiguredValue() {
        // Given
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskTimeoutHours", 3.5);

        // When
        double timeout = stuckTaskCleanupService.getStuckTaskTimeoutHours();

        // Then
        assertEquals(3.5, timeout);
    }

    @Test
    void isStuckTaskCheckEnabled_shouldReturnConfiguredValue() {
        // Given
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskCheckEnabled", false);

        // When
        boolean enabled = stuckTaskCleanupService.isStuckTaskCheckEnabled();

        // Then
        assertFalse(enabled);
    }

    /**
     * Helper method to create a task for testing
     */
    private Task createTask(Long id, TaskType type, LocalDateTime lastUpdateDate) {
        return Task.builder()
                .id(id)
                .type(type.toString())
                .status(TaskStatus.IN_PROGRESS.toString())
                .creationDate(lastUpdateDate.minusHours(1))
                .lastUpdateDate(lastUpdateDate)
                .progressPercentage("0%")
                .details(new ArrayList<>())
                .build();
    }
}

