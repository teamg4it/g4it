/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apiinout.mapper.OutNonItActivityMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutNonItActivity;
import com.soprasteria.g4it.backend.apiinout.repository.OutNonItActivityRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutNonItActivityRest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OutNonItActivityService {

    private final OutNonItActivityRepository outNonItActivityRepository;
    private final TaskRepository taskRepository;
    private final OutNonItActivityMapper outNonItActivityMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<OutNonItActivityRest> getByInventory(final Inventory inventory) {
        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);
        return task.map(t -> getNonItActivitiesByTaskId(t.getId())).orElse(List.of());
    }

    public List<OutNonItActivityRest> getNonItActivitiesByTaskId(final Long taskId) {
        int pageNumber = 0;
        List<OutNonItActivityRest> result = new ArrayList<>();
        log.info("Fetching OutNonItActivities for taskId: {} batchsize: {}", taskId, Constants.BATCH_SIZE_50000);
        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE_50000);
            List<OutNonItActivity> nonItActivities = outNonItActivityRepository.findByTaskIdOrderByIdAsc(taskId, page);
            if (nonItActivities.isEmpty()) {
                break;
            }
            result.addAll(outNonItActivityMapper.toRest(nonItActivities));
            entityManager.clear();
            pageNumber++;
        }
        return result;
    }
}
