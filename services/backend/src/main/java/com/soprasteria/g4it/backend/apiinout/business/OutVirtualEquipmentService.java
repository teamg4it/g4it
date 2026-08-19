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
import com.soprasteria.g4it.backend.apiinout.mapper.OutVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@AllArgsConstructor
public class OutVirtualEquipmentService {

    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private TaskRepository taskRepository;
    private OutVirtualEquipmentMapper outVirtualEquipmentMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get virtual equipments by inventory id
     * Find by last task
     *
     * @param inventory the inventory
     * @return list of aggregated virtual equipments
     */
    /*public List<OutVirtualEquipmentRest> getByInventory(final Inventory inventory) {

        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);

        if (task.isEmpty()) {
            return List.of();
        }

        return outVirtualEquipmentMapper.toRest(
                outVirtualEquipmentRepository.findByTaskId(task.get().getId())
        );

    }*/
    @Transactional(readOnly = true)
    public List<OutVirtualEquipmentRest> getByInventory(final Inventory inventory) {

        Optional<Task> task =
                taskRepository.findByInventoryAndLastCreationDate(inventory);

        if (task.isEmpty()) {
            return List.of();
        }

        final Long taskId = task.get().getId();

        int pageNumber = 0;

        List<OutVirtualEquipmentRest> result = new ArrayList<>();

        logMemory("START", pageNumber);

        while (true) {

            logMemory("BEFORE_FETCH", pageNumber);

            Pageable page = PageRequest.of(
                    pageNumber,
                    Constants.BATCH_SIZE_50000
            );

            List<OutVirtualEquipment> virtualEquipments =
                    outVirtualEquipmentRepository
                            .findByTaskIdOrderByIdAsc(taskId, page);

            log.info(
                    "Fetched page={}, records={}",
                    pageNumber,
                    virtualEquipments.size()
            );

            logMemory("AFTER_FETCH", pageNumber);

            if (virtualEquipments.isEmpty()) {
                break;
            }

            List<OutVirtualEquipmentRest> mapped =
                    outVirtualEquipmentMapper.toRest(virtualEquipments);

            log.info(
                    "out_virtual_equipment Mapped page={}, entityRecords={}, dtoRecords={}",
                    pageNumber,
                    virtualEquipments.size(),
                    mapped.size()
            );

            logMemory("AFTER_MAPPING", pageNumber);

            result.addAll(mapped);

            log.info(
                    "Result accumulated out_virtual_equipment: page={}, totalResultRecords={}",
                    pageNumber,
                    result.size()
            );

            logMemory("AFTER_ADD_TO_RESULT", pageNumber);

            virtualEquipments.clear();
            mapped.clear();

            entityManager.clear();

            logMemory("AFTER_CLEAR", pageNumber);

            pageNumber++;
        }

        log.info(
                "Final result size out_virtual_equipment ={}, pages={}",
                result.size(),
                pageNumber
        );

        logMemory("END", pageNumber);
        return result;
    }


    /**
     * Get virtual  equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceVersionUid the digital service uid
     * @return list of aggregated virtual equipments
     */
    /*public List<OutVirtualEquipmentRest> getByDigitalServiceVersionUid(final String digitalServiceVersionUid) {
        DigitalServiceVersion digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid).orElseThrow();

        Optional<Task> task = taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion);
        if (task.isEmpty()) {
            return List.of();
        }

        return outVirtualEquipmentMapper.toRest(
                outVirtualEquipmentRepository.findByTaskId(task.get().getId())
        );

    }*/
    @Transactional(readOnly = true)
    public List<OutVirtualEquipmentRest> getByDigitalServiceVersionUid(
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

        final Long taskId = task.get().getId();

        int pageNumber = 0;
        List<OutVirtualEquipmentRest> result = new ArrayList<>();

        while (true) {
            Pageable page = PageRequest.of(
                    pageNumber,
                    Constants.BATCH_SIZE_50000
            );

            List<OutVirtualEquipment> virtualEquipments =
                    outVirtualEquipmentRepository
                            .findByTaskIdOrderByIdAsc(taskId, page);

            if (virtualEquipments.isEmpty()) {
                break;
            }

            result.addAll(
                    outVirtualEquipmentMapper.toRest(virtualEquipments)
            );

            virtualEquipments.clear();
            entityManager.clear();

            pageNumber++;
        }
        return result;
    }

    private void logMemory(String stage, int pageNumber) {
        Runtime runtime = Runtime.getRuntime();

        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();

        log.info(
                "Memory [{}] - page={}, used={} MB, total={} MB, max={} MB, free={} MB",
                stage,
                pageNumber,
                usedMemory / (1024 * 1024),
                totalMemory / (1024 * 1024),
                maxMemory / (1024 * 1024),
                runtime.freeMemory() / (1024 * 1024)
        );
    }

}
