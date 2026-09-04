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
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@AllArgsConstructor
public class OutVirtualEquipmentService {

    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private TaskRepository taskRepository;
    private OutVirtualEquipmentMapper outVirtualEquipmentMapper;
    private EntityManager entityManager;

    /**
     * Get virtual equipments by inventory id
     * Find by last task
     *
     * @param inventory the inventory
     * @return list of aggregated virtual equipments
     */
    @Transactional(readOnly = true)
    public List<OutVirtualEquipmentRest> getByInventory(final Inventory inventory) {
        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);
        return task.map(t -> getVirtualEquipmentsByTaskId(t.getId())).orElse(List.of());
    }

    /**
     * Get virtual equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceVersionUid the digital service uid
     * @return list of aggregated virtual equipments
     */
    @Transactional(readOnly = true)
    public List<OutVirtualEquipmentRest> getByDigitalServiceVersionUid(
            final String digitalServiceVersionUid) {

        DigitalServiceVersion digitalServiceVersion =
                digitalServiceVersionRepository
                        .findById(digitalServiceVersionUid)
                        .orElseThrow();

        Optional<Task> task =
                taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion);
        
        return task.map(t -> getVirtualEquipmentsByTaskId(t.getId())).orElse(List.of());
    }

    /**
     * Fetch virtual equipments by task id with pagination
     *
     * @param taskId the task id
     * @return list of aggregated virtual equipments
     */
    private List<OutVirtualEquipmentRest> getVirtualEquipmentsByTaskId(final Long taskId) {
        int pageNumber = 0;
        List<OutVirtualEquipmentRest> result = new ArrayList<>();

        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE_10000);
            List<OutVirtualEquipment> virtualEquipments =
                    outVirtualEquipmentRepository.findByTaskIdOrderByIdAsc(taskId, page);

            if (virtualEquipments.isEmpty()) {
                break;
            }

            result.addAll(outVirtualEquipmentMapper.toRest(virtualEquipments));
            entityManager.clear();
            pageNumber++;
        }
        return result;
    }
}
