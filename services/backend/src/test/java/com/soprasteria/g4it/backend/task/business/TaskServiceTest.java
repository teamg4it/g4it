/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.task.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.common.task.mapper.TaskMapper;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ExportService exportService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTask_returnsMappedTaskWhenTaskExists() {
        long taskId = 1L;
        Task task = new Task();
        TaskRest taskRest = TaskRest.builder().build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.map(task)).thenReturn(taskRest);

        TaskRest result = taskService.getTask(taskId);

        assertEquals(taskRest, result);
        verify(taskRepository).findById(taskId);
        verify(taskMapper).map(task);
    }

    @Test
    void getTask_throwsExceptionWhenTaskDoesNotExist() {
        long taskId = 1L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(G4itRestException.class, () -> taskService.getTask(taskId));
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(taskMapper);
    }

    @Test
    void createDigitalServiceTaskCreatesAndSavesNewTaskWhenNoExistingTask() {
        DigitalService digitalService = new DigitalService();
        digitalService.setUid("uid123");
        digitalService.setUser(User.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(1L).firstName("fname").build());
        List<String> criteria = List.of("criterion1", "criterion2");
        Task newTask = new Task();
        newTask.setDigitalServiceUid("uid123");

        when(taskRepository.findByDigitalServiceUid("uid123")).thenReturn(Optional.empty());
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        Task result = taskService.createDigitalServiceTask(digitalService, criteria);

        assertEquals(newTask, result);
        verify(taskRepository).findByDigitalServiceUid("uid123");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createDigitalServiceTaskUpdatesExistingTaskWhenTaskExists() {
        DigitalService digitalService = new DigitalService();
        digitalService.setUid("uid123");
        digitalService.setUser(User.builder().email("testuser@soprasteria.com").domain("soprasteria.com").id(1L).firstName("fname").build());
        List<String> criteria = List.of("criterion1", "criterion2");
        Task existingTask = new Task();
        existingTask.setDigitalServiceUid("uid123");

        when(taskRepository.findByDigitalServiceUid("uid123")).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.createDigitalServiceTask(digitalService, criteria);

        assertEquals(existingTask, result);
        verify(taskRepository).findByDigitalServiceUid("uid123");
        verify(taskRepository).save(existingTask);
    }

    @Test
    void deleteEvaluatingTasksByInventoryIdDeletesTasksWhenTasksExist() {
        String subscriber = "subscriber123";
        Long organizationId = 1L;
        Long inventoryId = 2L;
        Task task1 = new Task();
        task1.setId(1L);
        Task task2 = new Task();
        task2.setId(2L);
        List<Task> tasks = List.of(task1, task2);

        when(taskRepository.findByTypeAndInventoryId(TaskType.EVALUATING.toString(), inventoryId)).thenReturn(tasks);

        taskService.deleteEvaluatingTasksByInventoryId(subscriber, organizationId, inventoryId);

        verify(exportService).cleanExport(1L, subscriber, String.valueOf(organizationId));
        verify(exportService).cleanExport(2L, subscriber, String.valueOf(organizationId));
        verify(taskRepository).deleteAll(tasks);
    }

    @Test
    void deleteEvaluatingTasksByInventoryIdDoesNothingWhenNoTasksExist() {
        Long inventoryId = 2L;

        when(taskRepository.findByTypeAndInventoryId(TaskType.EVALUATING.toString(), inventoryId)).thenReturn(List.of());

        taskService.deleteEvaluatingTasksByInventoryId("subscriber123", 1L, inventoryId);

        verifyNoInteractions(exportService);
        verify(taskRepository, never()).deleteAll(anyList());
    }
}
