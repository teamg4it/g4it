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
import com.soprasteria.g4it.backend.apiinout.mapper.OutApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutApplicationRest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OutApplicationService {

    private OutApplicationRepository outApplicationRepository;
    private DigitalServiceRepository digitalServiceRepository;
    private TaskRepository taskRepository;
    private OutApplicationMapper outApplicationMapper;

    /**
     * Get applications by inventory id
     * Find by last task
     *
     * @param inventory the inventory
     * @return list of aggregated applications
     */
    public List<OutApplicationRest> getByInventory(final Inventory inventory) {


        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);

        if (task.isEmpty()) {
            return List.of();
        }

        return outApplicationMapper.toRest(
                outApplicationRepository.findByTaskId(task.get().getId())
        );

    }

    /**
     * Get virtual  equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceUid the digital service uid
     * @return list of aggregated virtual equipments
     */
    public List<OutApplicationRest> getByDigitalServiceUid(final String digitalServiceUid) {

        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow();
        Optional<Task> task = taskRepository.findByDigitalService(digitalService);

        if (task.isEmpty()) {
            return List.of();
        }

        return outApplicationMapper.toRest(
                outApplicationRepository.findByTaskId(task.get().getId())
        );

    }

}
