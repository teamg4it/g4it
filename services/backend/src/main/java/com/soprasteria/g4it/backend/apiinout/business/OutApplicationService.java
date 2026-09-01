/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.OutApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutApplicationRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class OutApplicationService {

    private OutApplicationRepository outApplicationRepository;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private TaskRepository taskRepository;
    private OutApplicationMapper outApplicationMapper;
    private EntityManager entityManager;

    /**
     * Get applications by inventory id
     * Find by last task
     *
     * @param inventory the inventory
     * @return list of aggregated applications
     */
    @Transactional(readOnly = true)
    public List<OutApplicationRest> getByInventory(final Inventory inventory) {
        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);
        return task.map(t -> getApplicationsByTaskId(t.getId())).orElse(List.of());
    }

    /**
     * Get applications by digital service uid
     * Find by last task
     *
     * @param digitalServiceVersionUid the digital service uid
     * @return list of aggregated applications
     */
    @Transactional(readOnly = true)
    public List<OutApplicationRest> getByDigitalServiceVersionUid(
            final String digitalServiceVersionUid) {
        DigitalServiceVersion digitalServiceVersion =
                digitalServiceVersionRepository
                        .findById(digitalServiceVersionUid)
                        .orElseThrow();

        Optional<Task> task =
                taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion);

        return task.map(t -> getApplicationsByTaskId(t.getId())).orElse(List.of());
    }

    /**
     * Fetch applications by task id with pagination
     *
     * @param taskId the task id
     * @return list of aggregated applications
     */
    private List<OutApplicationRest> getApplicationsByTaskId(final Long taskId) {
        int pageNumber = 0;
        List<OutApplicationRest> result = new ArrayList<>();

        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE_50000);
            List<OutApplication> outApplications = 
                    outApplicationRepository.findByTaskIdOrderByIdAsc(taskId, page);

            if (outApplications.isEmpty()) {
                break;
            }

            result.addAll(outApplicationMapper.toRest(outApplications));
            entityManager.clear();
            pageNumber++;
        }
        return result;
    }

}
