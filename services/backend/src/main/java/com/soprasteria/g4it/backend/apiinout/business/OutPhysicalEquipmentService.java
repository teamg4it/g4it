/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
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
    private DigitalServiceRepository digitalServiceRepository;
    private TaskRepository taskRepository;
    private OutPhysicalEquipmentMapper outPhysicalEquipmentMapper;

    /**
     * Get physical equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceUid the digital service uid
     * @return the list of aggregated physical equipments
     */
    public List<OutPhysicalEquipmentRest> getByDigitalServiceUid(final String digitalServiceUid) {

        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow();
        Optional<Task> task = taskRepository.findByDigitalServiceAndLastCreationDate(digitalService);
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
