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
import static org.mockito.Mockito.*;

/**
 * Tests the orchestration logic of {@link StuckTaskCleanupService}: enabled/disabled
 * flag handling, fetching IN_PROGRESS tasks and delegating each one to
 * {@link StuckTaskRowUpdater} (mocked here), then aggregating the counts.
 * <p>
 * The per-task stuck-detection branching logic itself is covered by the
 * StuckTaskRowUpdaterTest test class, since it now runs in its own transactional bean.
 */
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
    void failStuckTasks_whenNoInProgressTasks_shouldNotDelegateAnything() {
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(Collections.emptyList());

        stuckTaskCleanupService.failStuckTasks();

        verifyNoInteractions(stuckTaskRowUpdater);
    }

    @Test
    void failStuckTasks_shouldDelegateEachTaskToRowUpdaterInIsolation() {
        LocalDateTime now = LocalDateTime.now();
        Task taskInit = createTask(1L, "EVALUATING", now.minusMinutes(10), null);
        Task taskProgress = createTask(2L, "EVALUATING", now.minusMinutes(10), now.minusMinutes(30));
        Task taskStuck = createTask(3L, "EVALUATING", now.minusMinutes(90), now.minusMinutes(90));

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(taskInit, taskProgress, taskStuck));
        when(stuckTaskRowUpdater.processTask(eq(taskInit), any())).thenReturn(TaskCheckResult.INITIALIZED);
        when(stuckTaskRowUpdater.processTask(eq(taskProgress), any())).thenReturn(TaskCheckResult.UPDATED);
        when(stuckTaskRowUpdater.processTask(eq(taskStuck), any())).thenReturn(TaskCheckResult.FAILED);

        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater, times(1)).processTask(eq(taskInit), any());
        verify(stuckTaskRowUpdater, times(1)).processTask(eq(taskProgress), any());
        verify(stuckTaskRowUpdater, times(1)).processTask(eq(taskStuck), any());
    }

    @Test
    void failStuckTasks_whenRowUpdaterReturnsNone_shouldNotFailOverallRun() {
        Task task = createTask(1L, "EVALUATING", LocalDateTime.now(), LocalDateTime.now());

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));
        when(stuckTaskRowUpdater.processTask(eq(task), any())).thenReturn(TaskCheckResult.NONE);

        // Must not throw
        stuckTaskCleanupService.failStuckTasks();

        verify(stuckTaskRowUpdater, times(1)).processTask(eq(task), any());
    }

    // ──────────────── isStuckTaskCheckEnabled() ────────────────

    @Test
    void isStuckTaskCheckEnabled_whenTrue_shouldReturnTrue() {
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskCheckEnabled", true);
        assertThat(stuckTaskCleanupService.isStuckTaskCheckEnabled()).isTrue();
    }

    @Test
    void isStuckTaskCheckEnabled_whenFalse_shouldReturnFalse() {
        ReflectionTestUtils.setField(stuckTaskCleanupService, "stuckTaskCheckEnabled", false);
        assertThat(stuckTaskCleanupService.isStuckTaskCheckEnabled()).isFalse();
    }
}



