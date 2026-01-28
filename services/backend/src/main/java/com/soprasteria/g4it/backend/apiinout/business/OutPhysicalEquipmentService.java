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
import com.soprasteria.g4it.backend.apiinout.mapper.OutPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Output physical equipment service
 */
@Service
@AllArgsConstructor
public class OutPhysicalEquipmentService {

    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private TaskRepository taskRepository;
    private OutPhysicalEquipmentMapper outPhysicalEquipmentMapper;

    /**
     * Get physical equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceVersionUid the digital service uid
     * @return the list of aggregated physical equipments
     */
    public List<OutPhysicalEquipmentRest> getByDigitalServiceVersionUid(final String digitalServiceVersionUid) {

        DigitalServiceVersion digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid).orElseThrow();
        Optional<Task> task = Optional.empty();
        for (int i = 0; i < 3 && task.isEmpty(); i++) {
            task = taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion);
            if (task.isEmpty()) {
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {/* ignore InterruptedException */ }
            }
        }
        if (task.isEmpty()) {
            return List.of();
        }
        return outPhysicalEquipmentMapper.toRest(
                outPhysicalEquipmentRepository.findByTaskId(task.get().getId())
        );

    }

    /**
     * Get physical equipments by digital service uid
     * Find by last task
     *
     * @param inventory the inventory
     * @return the list of aggregated physical equipments
     */
    public List<OutPhysicalEquipmentRest> getByInventory(final Inventory inventory) {

        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);

        if (task.isEmpty()) {
            return List.of();
        }

        return outPhysicalEquipmentMapper.toRest(
                outPhysicalEquipmentRepository.findByTaskId(task.get().getId())
        );

    }

}
