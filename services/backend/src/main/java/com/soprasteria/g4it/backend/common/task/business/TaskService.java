/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.task.mapper.TaskMapper;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskRest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TaskService {

    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    private ExportService exportService;

    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Get task by taskId
     *
     * @param taskId the task id
     * @return the task
     */
    public TaskRest getTask(final long taskId) {

        final Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new G4itRestException("404", String.format("task %d not found", taskId)));

        return taskMapper.map(task);
    }

    /**
     * Create digital service task
     *
     * @param digitalService the digital service
     * @param criteria       the list of criterion
     * @return the task
     */
    public Task createDigitalServiceTask(DigitalService digitalService, List<String> criteria) {
        final LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findById(authService.getUser().getId()).orElseThrow();

        Task task = taskRepository.findByDigitalService(digitalService)
                .orElseGet(() -> Task.builder()
                        .digitalService(digitalService)
                        .type(TaskType.EVALUATING_DIGITAL_SERVICE.toString())
                        .createdBy(user)
                        .build());

        task.setProgressPercentage("0%");
        task.setStatus(TaskStatus.IN_PROGRESS.toString());
        task.setCreationDate(now);
        task.setLastUpdateDate(now);
        task.setCriteria(criteria);

        return taskRepository.save(task);
    }

    /**
     * Get the task from digital service uid
     *
     * @param digitalServiceUid the digital service uid
     * @return the task
     */
    public Optional<Task> getTask(String digitalServiceUid) {

        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow();
        return taskRepository.findByDigitalService(digitalService);
    }

    /**
     * Save task in database
     *
     * @param task the task
     */
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    /**
     * Delete EVALUATING tasks of an inventory
     *
     * @param inventoryId    the inventory id
     * @param subscriber     the subscriber
     * @param organizationId the organization id
     */
    @Transactional
    public void deleteEvaluatingTasksByInventoryId(String subscriber, Long organizationId, Long inventoryId) {
        // Retrieve EVALUATING tasks to be deleted
        List<Task> tasksToDelete = taskRepository.findByTypeAndInventoryId(TaskType.EVALUATING.toString(), inventoryId);
        if (tasksToDelete.isEmpty()) {
            return;
        }
        // Clean exports for each task
        tasksToDelete.forEach(task -> {
            exportService.cleanExport(task.getId(), subscriber, String.valueOf(organizationId));
        });

        taskRepository.deleteAll(tasksToDelete);
    }
}
