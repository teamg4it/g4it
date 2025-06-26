/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apiinout.mapper.OutVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OutVirtualEquipmentService {

    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    private TaskRepository taskRepository;
    private OutVirtualEquipmentMapper outVirtualEquipmentMapper;

    /**
     * Get virtual equipments by inventory id
     * Find by last task
     *
     * @param inventory the inventory
     * @return list of aggregated virtual equipments
     */
    public List<OutVirtualEquipmentRest> getByInventory(final Inventory inventory) {

        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);

        if (task.isEmpty()) {
            return List.of();
        }

        return outVirtualEquipmentMapper.toRest(
                outVirtualEquipmentRepository.findByTaskId(task.get().getId())
        );

    }

    /**
     * Get virtual  equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceUid the digital service uid
     * @return list of aggregated virtual equipments
     */
    public List<OutVirtualEquipmentRest> getByDigitalServiceUid(final String digitalServiceUid) {

        Optional<Task> task = taskRepository.findByDigitalServiceUidAndLastCreationDate(digitalServiceUid);
        if (task.isEmpty()) {
            return List.of();
        }

        return outVirtualEquipmentMapper.toRest(
                outVirtualEquipmentRepository.findByTaskId(task.get().getId())
        );

    }

}
