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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the per-task stuck-detection branching logic in {@link StuckTaskRowUpdater}.
 * Each call to {@link StuckTaskRowUpdater#processTask} runs in its own REQUIRES_NEW
 * transaction in production; here it is exercised directly against a mocked repository.
 */
@ExtendWith(MockitoExtension.class)
class StuckTaskRowUpdaterTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private StuckTaskRowUpdater stuckTaskRowUpdater;

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

    // ── Case 1: PLCD is null – first scheduler check ──

    @Test
    void processTask_whenPlcdIsNull_shouldInitializePlcdAndReturnInitialized() {
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(30);
        Task task = createTask(1L, "EVALUATING", lastUpdate, null);

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        assertThat(result).isEqualTo(TaskCheckResult.INITIALIZED);

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskRepository, times(1)).updateProgressLastChangedDate(eq(1L), dateCaptor.capture());
        assertThat(dateCaptor.getValue()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.SECONDS));
        verify(taskRepository, never()).updateStuckTaskFailed(any(), any(), any(), any(), any());
    }

    @Test
    void processTask_whenPlcdIsNull_updateThrows_shouldReturnNoneAndNotPropagate() {
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(10);
        Task task = createTask(1L, "EVALUATING", lastUpdate, null);
        doThrow(new RuntimeException("DB error")).when(taskRepository)
                .updateProgressLastChangedDate(eq(1L), any());

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        assertThat(result).isEqualTo(TaskCheckResult.NONE);
    }

    // ── Case 2: LUD > PLCD – task is progressing ──

    @Test
    void processTask_whenTaskHasProgressed_shouldUpdatePlcdAndReturnUpdated() {
        LocalDateTime plcd = LocalDateTime.now().minusMinutes(60);
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(20);
        Task task = createTask(2L, "EVALUATING", lastUpdate, plcd);

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        assertThat(result).isEqualTo(TaskCheckResult.UPDATED);

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskRepository, times(1)).updateProgressLastChangedDate(eq(2L), dateCaptor.capture());
        assertThat(dateCaptor.getValue()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.SECONDS));
        verify(taskRepository, never()).updateStuckTaskFailed(any(), any(), any(), any(), any());
    }

    @Test
    void processTask_whenProgressUpdateThrows_shouldReturnNoneAndNotPropagate() {
        LocalDateTime now = LocalDateTime.now();
        Task task = createTask(1L, "EVALUATING", now.minusMinutes(10), now.minusMinutes(30));
        doThrow(new RuntimeException("DB error")).when(taskRepository)
                .updateProgressLastChangedDate(eq(1L), any());

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.NONE);
    }

    // ── Case 3: LUD == PLCD – task is stuck ──

    @Test
    void processTask_whenTaskIsStuck_shouldMarkAsFailedAndReturnFailed() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        assertThat(result).isEqualTo(TaskCheckResult.FAILED);

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(taskRepository, times(1)).updateStuckTaskFailed(eq(3L), statusCaptor.capture(), any(), any(), any());
        assertThat(statusCaptor.getValue()).isEqualTo(TaskStatus.FAILED.toString());
    }

    @Test
    void processTask_whenTaskIsStuck_shouldPopulateErrorsWithSchedulerMarker() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        ArgumentCaptor<List<String>> errorsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), any(), errorsCaptor.capture());

        List<String> errors = errorsCaptor.getValue();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("FAILED BY SCHEDULER");
    }

    @Test
    void processTask_whenTaskIsStuck_detailsShouldContainStuckMinutes() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), detailsCaptor.capture(), any());

        List<String> details = detailsCaptor.getValue();
        assertThat(details).isNotEmpty();
        assertThat(details.get(0)).contains("minutes");
    }

    @Test
    void processTask_whenTaskIsStuck_lastUpdateDateShouldBeRefreshed() {
        LocalDateTime stuckDate = LocalDateTime.now().minusHours(2);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        ArgumentCaptor<LocalDateTime> lastUpdateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), lastUpdateCaptor.capture(), any(), any());

        assertThat(lastUpdateCaptor.getValue()).isAfterOrEqualTo(before);
    }

    @Test
    void processTask_whenTaskIsStuck_existingDetailsShouldBePreserved() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        task.setDetails(new ArrayList<>(List.of("pre-existing detail")));

        stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), detailsCaptor.capture(), any());

        List<String> details = detailsCaptor.getValue();
        assertThat(details).hasSizeGreaterThan(1);
        assertThat(details).contains("pre-existing detail");
    }

    @Test
    void processTask_whenTaskIsStuck_nullDetailsShouldBeHandledGracefully() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        task.setDetails(null);

        stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), detailsCaptor.capture(), any());

        assertThat(detailsCaptor.getValue()).isNotNull().isNotEmpty();
    }

    // ── Case 3 edge: LUD < PLCD ──

    @Test
    void processTask_whenLastUpdateBeforePlcd_shouldAlsoMarkAsFailed() {
        // Unusual edge case: LUD < PLCD - service treats this the same as stuck
        LocalDateTime plcd = LocalDateTime.now().minusMinutes(30);
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(60); // LUD < PLCD
        Task task = createTask(4L, "EVALUATING", lastUpdate, plcd);

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        assertThat(result).isEqualTo(TaskCheckResult.FAILED);

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(taskRepository, times(1)).updateStuckTaskFailed(eq(4L), statusCaptor.capture(), any(), any(), any());
        assertThat(statusCaptor.getValue()).isEqualTo(TaskStatus.FAILED.toString());
    }

    // ── failTask() save exception ──

    @Test
    void processTask_whenFailTaskUpdateThrows_shouldNotPropagateException() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        doThrow(new RuntimeException("DB failure")).when(taskRepository)
                .updateStuckTaskFailed(any(), any(), any(), any(), any());

        // Must not propagate
        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, LocalDateTime.now());

        assertThat(result).isEqualTo(TaskCheckResult.FAILED);
    }
}

