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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void failStuckTasks_shouldFailTaskWhenNoProgressDetected() {
        // Given
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(30);

        Task task = createTask(1L, TaskType.LOADING, lastUpdate);
        task.setProgressLastChangedDate(lastUpdate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        // When
        stuckTaskCleanupService.failStuckTasks();

        // Then
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        Task saved = captor.getValue();

        assertEquals(TaskStatus.FAILED.toString(), saved.getStatus());
        assertNotNull(saved.getErrors());
        assertFalse(saved.getErrors().isEmpty());
        assertNotNull(saved.getDetails());
    }

    @Test
    void failStuckTasks_shouldFailMultipleStuckTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = createTask(1L, TaskType.LOADING, now.minusMinutes(30));
        task1.setProgressLastChangedDate(task1.getLastUpdateDate());

        Task task2 = createTask(2L, TaskType.EVALUATING, now.minusMinutes(45));
        task2.setProgressLastChangedDate(task2.getLastUpdateDate());

        Task progressingTask = createTask(3L, TaskType.LOADING, now.minusMinutes(5));
        progressingTask.setProgressLastChangedDate(now.minusHours(1));

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task1, task2, progressingTask));

        stuckTaskCleanupService.failStuckTasks();

        verify(taskRepository, times(3)).save(any(Task.class));
    }

    @Test
    void failStuckTasks_shouldUpdateLastUpdateDateWhenTaskFails() {
        LocalDateTime original = LocalDateTime.now().minusMinutes(20);

        Task task = createTask(1L, TaskType.LOADING, original);
        task.setProgressLastChangedDate(original);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        assertTrue(captor.getValue().getLastUpdateDate().isAfter(original));
    }

    @Test
    void failStuckTasks_shouldAddErrorMessageToDetails() {
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(25);

        Task task = createTask(1L, TaskType.LOADING, lastUpdate);
        task.setProgressLastChangedDate(lastUpdate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        Task saved = captor.getValue();

        assertTrue(saved.getDetails().stream()
                .anyMatch(detail -> detail.contains("automatically terminated")));
    }

    @Test
    void failStuckTasks_shouldPreserveProgressPercentageWhenTaskFails() {
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(40);

        Task task = createTask(1L, TaskType.LOADING, lastUpdate);
        task.setProgressLastChangedDate(lastUpdate);
        task.setProgressPercentage("45%");

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        assertEquals("45%", captor.getValue().getProgressPercentage());
    }

    @Test
    void failStuckTasks_shouldHandleExceptionGracefully() {

        Task task = createTask(
                1L,
                TaskType.LOADING,
                LocalDateTime.now().minusMinutes(30));

        task.setProgressLastChangedDate(null);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        when(taskRepository.save(any(Task.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() ->
                stuckTaskCleanupService.failStuckTasks());

        verify(taskRepository).save(any(Task.class));
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

