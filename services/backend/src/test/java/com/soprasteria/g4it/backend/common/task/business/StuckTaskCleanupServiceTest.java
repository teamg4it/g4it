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
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    }

    @Test
    void failStuckTasks_whenNoInProgressTasks_shouldNotSaveAnything() {
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(Collections.emptyList());

        stuckTaskCleanupService.failStuckTasks();

        verify(taskRepository, never()).updateProgressLastChangedDate(any(), any());
        verify(taskRepository, never()).updateStuckTaskFailed(any(), any(), any(), any(), any());
    }

    // ── Case 1: PLCD is null – first scheduler check ──

    @Test
    void failStuckTasks_whenPlcdIsNull_shouldInitializePlcdAndKeepInProgress() {
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(30);
        Task task = createTask(1L, "EVALUATING", lastUpdate, null);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskRepository, times(1)).updateProgressLastChangedDate(eq(1L), dateCaptor.capture());

        assertThat(dateCaptor.getValue()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.SECONDS));
        verify(taskRepository, never()).updateStuckTaskFailed(any(), any(), any(), any(), any());
    }

    @Test
    void failStuckTasks_whenPlcdIsNull_saveShouldThrow_shouldContinueProcessingOtherTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = createTask(1L, "EVALUATING", now.minusMinutes(10), null);
        LocalDateTime stuckDate = now.minusMinutes(90);
        Task task2 = createTask(2L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task1, task2));
        doThrow(new RuntimeException("DB error")).when(taskRepository)
                .updateProgressLastChangedDate(eq(1L), any());

        // Must not propagate exception
        stuckTaskCleanupService.failStuckTasks();

        verify(taskRepository, times(1)).updateProgressLastChangedDate(eq(1L), any());
        verify(taskRepository, times(1)).updateStuckTaskFailed(eq(2L), any(), any(), any(), any());
    }

    // ── Case 2: LUD > PLCD – task is progressing ──

    @Test
    void failStuckTasks_whenTaskHasProgressed_shouldUpdatePlcdAndKeepInProgress() {
        LocalDateTime plcd = LocalDateTime.now().minusMinutes(60);
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(20);
        Task task = createTask(2L, "EVALUATING", lastUpdate, plcd);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskRepository, times(1)).updateProgressLastChangedDate(eq(2L), dateCaptor.capture());

        assertThat(dateCaptor.getValue()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.SECONDS));
        verify(taskRepository, never()).updateStuckTaskFailed(any(), any(), any(), any(), any());
    }

    @Test
    void failStuckTasks_whenProgressUpdateSaveThrows_shouldContinueProcessingOtherTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = createTask(1L, "EVALUATING", now.minusMinutes(10), now.minusMinutes(30));
        LocalDateTime stuckDate = now.minusMinutes(90);
        Task task2 = createTask(2L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task1, task2));
        doThrow(new RuntimeException("DB error")).when(taskRepository)
                .updateProgressLastChangedDate(eq(1L), any());

        stuckTaskCleanupService.failStuckTasks();

        verify(taskRepository, times(1)).updateProgressLastChangedDate(eq(1L), any());
        verify(taskRepository, times(1)).updateStuckTaskFailed(eq(2L), any(), any(), any(), any());
    }

    // ── Case 3: LUD == PLCD – task is stuck ──

    @Test
    void failStuckTasks_whenTaskIsStuck_shouldMarkAsFailed() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(taskRepository, times(1)).updateStuckTaskFailed(eq(3L), statusCaptor.capture(), any(), any(), any());

        assertThat(statusCaptor.getValue()).isEqualTo(TaskStatus.FAILED.toString());
    }

    @Test
    void failStuckTasks_whenTaskIsStuck_shouldPopulateErrorsWithSchedulerMarker() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<List<String>> errorsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), any(), errorsCaptor.capture());

        List<String> errors = errorsCaptor.getValue();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("FAILED BY SCHEDULER");
    }

    @Test
    void failStuckTasks_whenTaskIsStuck_detailsShouldContainStuckMinutes() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), detailsCaptor.capture(), any());

        List<String> details = detailsCaptor.getValue();
        assertThat(details).isNotEmpty();
        // Detail message mentions the duration in minutes
        assertThat(details.get(0)).contains("minutes");
    }

    @Test
    void failStuckTasks_whenTaskIsStuck_lastUpdateDateShouldBeRefreshed() {
        LocalDateTime stuckDate = LocalDateTime.now().minusHours(2);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<LocalDateTime> lastUpdateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), lastUpdateCaptor.capture(), any(), any());

        assertThat(lastUpdateCaptor.getValue()).isAfterOrEqualTo(before);
    }

    @Test
    void failStuckTasks_whenTaskIsStuck_existingDetailsShouldBePreserved() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        task.setDetails(new ArrayList<>(List.of("pre-existing detail")));

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), detailsCaptor.capture(), any());

        List<String> details = detailsCaptor.getValue();
        assertThat(details).hasSizeGreaterThan(1);
        assertThat(details).contains("pre-existing detail");
    }

    @Test
    void failStuckTasks_whenTaskIsStuck_nullDetailsShouldBeHandledGracefully() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);
        task.setDetails(null);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<List<String>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).updateStuckTaskFailed(eq(3L), any(), any(), detailsCaptor.capture(), any());

        assertThat(detailsCaptor.getValue()).isNotNull().isNotEmpty();
    }

    // ── Case 3 edge: LUD < PLCD ──

    @Test
    void failStuckTasks_whenLastUpdateBeforePlcd_shouldAlsoMarkAsFailed() {
        // Unusual edge case: LUD < PLCD – service treats this the same as stuck
        LocalDateTime plcd = LocalDateTime.now().minusMinutes(30);
        LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(60); // LUD < PLCD
        Task task = createTask(4L, "EVALUATING", lastUpdate, plcd);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));

        stuckTaskCleanupService.failStuckTasks();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(taskRepository, times(1)).updateStuckTaskFailed(eq(4L), statusCaptor.capture(), any(), any(), any());

        assertThat(statusCaptor.getValue()).isEqualTo(TaskStatus.FAILED.toString());
    }

    // ── failTask() save exception ──

    @Test
    void failStuckTasks_whenFailTaskSaveThrows_shouldNotPropagateException() {
        LocalDateTime stuckDate = LocalDateTime.now().minusMinutes(90);
        Task task = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(task));
        doThrow(new RuntimeException("DB failure")).when(taskRepository)
                .updateStuckTaskFailed(any(), any(), any(), any(), any());

        // Must not propagate
        stuckTaskCleanupService.failStuckTasks();
    }

    // ── Mixed scenario ──

    @Test
    void failStuckTasks_withMixedTasks_shouldHandleEachCaseIndependently() {
        LocalDateTime now = LocalDateTime.now();

        Task taskInit    = createTask(1L, "EVALUATING", now.minusMinutes(10), null);
        Task taskProgress = createTask(2L, "EVALUATING", now.minusMinutes(10), now.minusMinutes(30));
        LocalDateTime stuckDate = now.minusMinutes(90);
        Task taskStuck   = createTask(3L, "EVALUATING", stuckDate, stuckDate);

        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(List.of(taskInit, taskProgress, taskStuck));

        stuckTaskCleanupService.failStuckTasks();

        // One targeted update per task, split between progress-updates and fail-updates
        verify(taskRepository, times(2)).updateProgressLastChangedDate(any(), any());
        verify(taskRepository, times(1)).updateStuckTaskFailed(any(), any(), any(), any(), any());
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
