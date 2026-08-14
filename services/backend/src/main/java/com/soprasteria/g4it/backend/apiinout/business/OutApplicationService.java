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
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.BatchProcessorUtil;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutApplicationRest;
import lombok.AllArgsConstructor;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@Service
@AllArgsConstructor
public class OutApplicationService {

    private OutApplicationRepository outApplicationRepository;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private TaskRepository taskRepository;
    private OutApplicationMapper outApplicationMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BatchProcessorUtil batchProcessorUtil;

    /**
     * Get applications by inventory id
     * Find by last task
     *
     * @param inventory the inventory
     * @return list of aggregated applications
     */
    /*public List<OutApplicationRest> getByInventory(final Inventory inventory) {


        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);

        if (task.isEmpty()) {
            return List.of();
        }

        return outApplicationMapper.toRest(
                outApplicationRepository.findByTaskId(task.get().getId())
        );

    }*/

    @Transactional(readOnly = true)
    public List<OutApplicationRest> getByInventory(final Inventory inventory) {

        Optional<Task> task =
                taskRepository.findByInventoryAndLastCreationDate(inventory);

        if (task.isEmpty()) {
            return List.of();
        }

        List<OutApplicationRest> result = new ArrayList<>();

        batchProcessorUtil.processInBatches(
                pageable -> outApplicationRepository
                        .findByTaskIdOrderByIdAsc(
                                task.get().getId(),
                                pageable),
                5000,
                batch -> {
                    result.addAll(
                            outApplicationMapper.toRest(batch)
                    );

                    entityManager.clear();
                });

        return result;
    }

    /**
     * Get virtual  equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceVersionUid the digital service uid
     * @return list of aggregated virtual equipments
     */

    /*public List<OutApplicationRest> getByDigitalServiceVersionUid(final String digitalServiceVersionUid) {

        DigitalServiceVersion digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid).orElseThrow();
        Optional<Task> task = taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion);
        if (task.isEmpty()) {
            return List.of();
        }

        return outApplicationMapper.toRest(
                outApplicationRepository.findByTaskId(task.get().getId())
        );

    }*/
    @Transactional(readOnly = true)
    public List<OutApplicationRest> getByDigitalServiceVersionUid(
            final String digitalServiceVersionUid) {

        DigitalServiceVersion digitalServiceVersion =
                digitalServiceVersionRepository
                        .findById(digitalServiceVersionUid)
                        .orElseThrow();

        Optional<Task> task =
                taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(
                        digitalServiceVersion);

        if (task.isEmpty()) {
            return List.of();
        }

        List<OutApplicationRest> result = new ArrayList<>();

        final Long taskId = task.get().getId();

        batchProcessorUtil.processInBatches(
                pageable -> outApplicationRepository
                        .findByTaskIdOrderByIdAsc(taskId, pageable),
                5000,
                batch -> {
                    result.addAll(
                            outApplicationMapper.toRest(batch)
                    );

                    entityManager.clear();
                });

        return result;
    }

}
