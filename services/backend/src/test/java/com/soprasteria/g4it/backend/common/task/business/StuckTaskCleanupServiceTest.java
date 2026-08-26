/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.business;

import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StuckTaskCleanupServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private StuckTaskRowUpdater stuckTaskRowUpdater;

    @InjectMocks
    private StuckTaskCleanupService stuckTaskCleanupService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskCheckEnabled", true);
    }

    // ─────────────────────── helpers ───────────────────────

    private Task createTask(Long id, String type, LocalDateTime lastUpdateDate,
                            LocalDateTime progressLastChangedDate) {
        return Task.builder()
                .id(id)
                .type(type)
                .status(TaskStatus.IN_PROGRESS.toString())
                .creationDate(lastUpdateDate.minusHours(1))
                .lastUpdateDate(lastUpdateDate)
                .progressPercentage("0%")
                .progressLastChangedDate(progressLastChangedDate)
                .details(new ArrayList<>())
                .build();
    }

    // ──────────────── failStuckTasks() ────────────────

    @Test
    void failStuckTasks_whenCheckDisabled_shouldSkipAllProcessing() {
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskCheckEnabled", false);

        stuckTaskCleanupService.failStuckTasks();

        verifyNoInteractions(taskRepository);
        verifyNoInteractions(stuckTaskRowUpdater);
    }

    @Test
    void failStuckTasks_whenNoInProgressTasks_shouldNotDelegateToRowUpdater() {
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(Collections.emptyList());

        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater, never()).processTask(any(), any());
    }

    @Test
    void failStuckTasks_shouldDelegateEachTaskToRowUpdaterInOrder() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = createTask(1L, "EVALUATING", now.minusMinutes(10), null);
        Task task2 = createTask(2L, "EVALUATING", now.minusMinutes(10), now.minusMinutes(30));

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task1, task2));
        when(stuckTaskRowUpdater.processTask(any(Task.class), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.INITIALIZED);

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(stuckTaskRowUpdater, times(2)).processTask(taskCaptor.capture(), any(LocalDateTime.class));
        assertThat(taskCaptor.getAllValues()).extracting(Task::getId).containsExactly(1L, 2L);
    }

    @Test
    void failStuckTasks_shouldPassTheSameNowInstantToEveryTask() {
        Task task1 = createTask(1L, "EVALUATING", LocalDateTime.now(), null);
        Task task2 = createTask(2L, "EVALUATING", LocalDateTime.now(), LocalDateTime.now().minusMinutes(30));

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task1, task2));
        when(stuckTaskRowUpdater.processTask(any(Task.class), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.NONE);

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(stuckTaskRowUpdater, times(2)).processTask(any(Task.class), nowCaptor.capture());
        assertThat(nowCaptor.getAllValues()).hasSize(2);
        assertThat(nowCaptor.getAllValues().get(0)).isEqualTo(nowCaptor.getAllValues().get(1));
    }

    @Test
    void failStuckTasks_whenRowUpdaterReturnsInitialized_shouldCompleteWithoutError() {
        Task task = createTask(1L, "EVALUATING", LocalDateTime.now(), null);
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));
        when(stuckTaskRowUpdater.processTask(eq(task), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.INITIALIZED);

        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater).processTask(eq(task), any(LocalDateTime.class));
    }

    @Test
    void failStuckTasks_whenRowUpdaterReturnsUpdated_shouldCompleteWithoutError() {
        Task task = createTask(2L, "EVALUATING", LocalDateTime.now(), LocalDateTime.now().minusMinutes(30));
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));
        when(stuckTaskRowUpdater.processTask(eq(task), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.UPDATED);

        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater).processTask(eq(task), any(LocalDateTime.class));
    }

    @Test
    void failStuckTasks_whenRowUpdaterReturnsFailed_shouldCompleteWithoutError() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));
        when(stuckTaskRowUpdater.processTask(eq(task), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.FAILED);

        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater).processTask(eq(task), any(LocalDateTime.class));
    }

    @Test
    void failStuckTasks_whenRowUpdaterReturnsNone_shouldCompleteWithoutError() {
        Task task = createTask(4L, "EVALUATING", LocalDateTime.now(), LocalDateTime.now());
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));
        when(stuckTaskRowUpdater.processTask(eq(task), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.NONE);

        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater).processTask(eq(task), any(LocalDateTime.class));
    }

    @Test
    void failStuckTasks_withMixedResults_shouldProcessEachTaskIndependently() {
        LocalDateTime now = LocalDateTime.now();

        Task taskInit = createTask(1L, "EVALUATING", now.minusMinutes(10), null);
        Task taskProgress = createTask(2L, "EVALUATING", now.minusMinutes(10), now.minusMinutes(30));
        LocalDateTime stuckDate = now.minusMinutes(90);
        Task taskStuck = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(taskInit, taskProgress, taskStuck));
        when(stuckTaskRowUpdater.processTask(eq(taskInit), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.INITIALIZED);
        when(stuckTaskRowUpdater.processTask(eq(taskProgress), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.UPDATED);
        when(stuckTaskRowUpdater.processTask(eq(taskStuck), any(LocalDateTime.class)))
                .thenReturn(TaskCheckResult.FAILED);

        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater, times(3)).processTask(any(Task.class), any(LocalDateTime.class));
    }
}
