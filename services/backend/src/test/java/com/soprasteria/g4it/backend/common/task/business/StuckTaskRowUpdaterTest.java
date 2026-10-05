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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StuckTaskRowUpdaterTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private StuckTaskRowUpdater stuckTaskRowUpdater;

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

    // ── Case 1: PLCD is null ──

    @Test
    void processTask_whenPlcdIsNull_shouldInitializePlcdAndReturnInitialized() {
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(30);
        Task task = createTask(1L, "EVALUATING", lastUpdate, null);
        LocalDateTime now = LocalDateTime.now();

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.INITIALIZED);
        verify(taskRepository).updateProgressLastChangedDate(
                task.getId(), lastUpdate.truncatedTo(ChronoUnit.SECONDS));
        verify(taskRepository, never()).updateStuckTaskFailed(any(), any(), any(), anyList(), anyList());
    }

    @Test
    void processTask_whenPlcdIsNull_andUpdateThrows_shouldReturnNone() {
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(30);
        Task task = createTask(1L, "EVALUATING", lastUpdate, null);
        LocalDateTime now = LocalDateTime.now();

        doThrow(new RuntimeException("DB error"))
                .when(taskRepository).updateProgressLastChangedDate(any(), any());

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.NONE);
    }

    // ── Case 2: LUD > PLCD ──

    @Test
    void processTask_whenTaskHasProgressed_shouldUpdatePlcdAndReturnUpdated() {
        LocalDateTime plcd = LocalDateTime.now().minusMinutes(60);
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(20);
        Task task = createTask(2L, "EVALUATING", lastUpdate, plcd);
        LocalDateTime now = LocalDateTime.now();

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.UPDATED);
        verify(taskRepository).updateProgressLastChangedDate(
                task.getId(), lastUpdate.truncatedTo(ChronoUnit.SECONDS));
        verify(taskRepository, never()).updateStuckTaskFailed(any(), any(), any(), anyList(), anyList());
    }

    @Test
    void processTask_whenTaskHasProgressed_andUpdateThrows_shouldReturnNone() {
        LocalDateTime plcd = LocalDateTime.now().minusMinutes(60);
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(20);
        Task task = createTask(2L, "EVALUATING", lastUpdate, plcd);
        LocalDateTime now = LocalDateTime.now();

        doThrow(new RuntimeException("DB error"))
                .when(taskRepository).updateProgressLastChangedDate(any(), any());

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.NONE);
    }

    // ── Case 3: LUD == PLCD (stuck) ──

    @Test
    void processTask_whenTaskIsStuck_shouldFailTaskAndReturnFailed() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        LocalDateTime now = LocalDateTime.now();

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.FAILED);

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<String>> errorsCaptor = ArgumentCaptor.forClass(List.class);

        verify(taskRepository).updateStuckTaskFailed(
                eq(task.getId()), statusCaptor.capture(), any(LocalDateTime.class),
                detailsCaptor.capture(), errorsCaptor.capture());

        assertThat(statusCaptor.getValue()).isEqualTo(TaskStatus.FAILED.toString());
        assertThat(errorsCaptor.getValue()).hasSize(1);
        assertThat(errorsCaptor.getValue().get(0)).contains("FAILED BY SCHEDULER");
        assertThat(detailsCaptor.getValue()).isNotEmpty();
        assertThat(detailsCaptor.getValue().get(0)).contains("minutes");

        verify(taskRepository, never()).updateProgressLastChangedDate(any(), any());
    }

    @Test
    void processTask_whenTaskIsStuck_existingDetailsShouldBePreserved() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        task.setDetails(new ArrayList<>(List.of("pre-existing detail")));
        LocalDateTime now = LocalDateTime.now();

        stuckTaskRowUpdater.processTask(task, now);

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(
                eq(task.getId()), any(), any(), detailsCaptor.capture(), anyList());

        assertThat(detailsCaptor.getValue()).hasSizeGreaterThan(1);
        assertThat(detailsCaptor.getValue()).contains("pre-existing detail");
    }

    @Test
    void processTask_whenTaskIsStuck_nullDetailsShouldBeHandledGracefully() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        task.setDetails(null);
        LocalDateTime now = LocalDateTime.now();

        stuckTaskRowUpdater.processTask(task, now);

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(
                eq(task.getId()), any(), any(), detailsCaptor.capture(), anyList());

        assertThat(detailsCaptor.getValue()).isNotNull().isNotEmpty();
    }

    @Test
    void processTask_whenTaskIsStuck_lastUpdateDateArgumentShouldBeNow() {
        LocalDateTime stuckDate = LocalDateTime.now().minusHours(2);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        LocalDateTime now = LocalDateTime.now();

        stuckTaskRowUpdater.processTask(task, now);

        verify(taskRepository).updateStuckTaskFailed(
                eq(task.getId()), any(), eq(now), anyList(), anyList());
    }

    @Test
    void processTask_whenFailTaskSaveThrows_shouldNotPropagateExceptionAndStillReturnFailed() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        LocalDateTime now = LocalDateTime.now();

        doThrow(new RuntimeException("DB failure"))
                .when(taskRepository).updateStuckTaskFailed(any(), any(), any(), anyList(), anyList());

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        // Exception is swallowed inside failTask(); processTask still reports FAILED
        // since the stuck detection itself succeeded.
        assertThat(result).isEqualTo(TaskCheckResult.FAILED);
    }

    // ── Case 3 edge: LUD < PLCD ──

    @Test
    void processTask_whenLastUpdateBeforePlcd_shouldAlsoBeTreatedAsStuck() {
        LocalDateTime plcd = LocalDateTime.now().minusMinutes(30);
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(60); // LUD < PLCD
        Task task = createTask(4L, "EVALUATING", lastUpdate, plcd);
        LocalDateTime now = LocalDateTime.now();

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.FAILED);
        verify(taskRepository, times(1)).updateStuckTaskFailed(any(), any(), any(), anyList(), anyList());
    }

    @Test
    void processTask_shouldTruncateProgressLastChangedDateToSecondsBeforeComparing() {
        // lastUpdate and progressLastChanged differ only by nanoseconds -> after truncation should be equal (stuck)
        LocalDateTime base = LocalDateTime.now().minusMinutes(90).withNano(0);
        LocalDateTime lastUpdate = base.plusNanos(500);
        LocalDateTime plcd = base.plusNanos(999);
        Task task = createTask(5L, "EVALUATING", lastUpdate, plcd);
        LocalDateTime now = LocalDateTime.now();

        TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);

        assertThat(result).isEqualTo(TaskCheckResult.FAILED);
    }
}

