/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apiinout.mapper.OutAiServiceMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutAiService;
import com.soprasteria.g4it.backend.apiinout.repository.OutAiServiceRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiServiceRest;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class OutAiServiceService {

    private OutAiServiceRepository outAiServiceRepository;
    private TaskRepository taskRepository;
    private OutAiServiceMapper outAiServiceMapper;
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<OutAiServiceRest> getByInventory(final Inventory inventory) {
        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);
        return task.map(t -> getAiServicesByTaskId(t.getId())).orElse(List.of());
    }

    @Transactional(readOnly = true)
    private List<OutAiServiceRest> getAiServicesByTaskId(final Long taskId) {
        int pageNumber = 0;
        List<OutAiServiceRest> result = new ArrayList<>();

        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE_10000);
            List<OutAiService> aiServices = outAiServiceRepository.findByTaskIdOrderByIdAsc(taskId, page);
            if (aiServices.isEmpty()) {
                break;
            }
            result.addAll(outAiServiceMapper.toRest(aiServices));
            entityManager.clear();
            pageNumber++;
        }
        return result;
    }
}