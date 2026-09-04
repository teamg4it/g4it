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
import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output physical equipment service
 */
@Slf4j
@Service
@AllArgsConstructor
public class OutPhysicalEquipmentService {

    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private TaskRepository taskRepository;
    private OutPhysicalEquipmentMapper outPhysicalEquipmentMapper;
    private EntityManager entityManager;

    /**
     * Get physical equipments by digital service uid
     * Find by last task
     *
     * @param digitalServiceVersionUid the digital service uid
     * @return the list of aggregated physical equipments
     */
    @Transactional(readOnly = true)
    public List<OutPhysicalEquipmentRest> getByDigitalServiceVersionUid(
            final String digitalServiceVersionUid) {
        DigitalServiceVersion digitalServiceVersion = digitalServiceVersionRepository
                .findById(digitalServiceVersionUid)
                .orElseThrow();

        Optional<Task> task = taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion);
        
        return task.map(t -> getPhysicalEquipmentsByTaskId(t.getId(), true)).orElse(List.of());
    }

    /**
     * Get physical equipments by inventory
     * Find by last task
     *
     * @param inventory the inventory
     * @return the list of aggregated physical equipments
     */
    @Transactional(readOnly = true)
    public List<OutPhysicalEquipmentRest> getByInventory(final Inventory inventory) {
        Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(inventory);
        return task.map(t -> getPhysicalEquipmentsByTaskId(t.getId(), false)).orElse(List.of());
    }

    /**
     * Fetch physical equipments by task id with pagination
     *
     * @param taskId the task id
     * @param useOrderedQuery whether to use the ordered query (findByTaskIdOrderByIdAsc)
     * @return list of aggregated physical equipments
     */
    private List<OutPhysicalEquipmentRest> getPhysicalEquipmentsByTaskId(
            final Long taskId, 
            final boolean useOrderedQuery) {
        int pageNumber = 0;
        List<OutPhysicalEquipmentRest> result = new ArrayList<>();

        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE_50000);
            List<OutPhysicalEquipment> physicalEquipments = useOrderedQuery
                    ? outPhysicalEquipmentRepository.findByTaskIdOrderByIdAsc(taskId, page)
                    : outPhysicalEquipmentRepository.findByTaskId(taskId, page);

            if (physicalEquipments.isEmpty()) {
                break;
            }

            result.addAll(outPhysicalEquipmentMapper.toRest(physicalEquipments));
            
            if (!useOrderedQuery) {
                log.info(
                        "Processed out_physical_equipment page={}, records={}, totalResult={}",
                        pageNumber,
                        physicalEquipments.size(),
                        result.size()
                );
                physicalEquipments.clear();
            }
            
            entityManager.clear();
            pageNumber++;
        }
        return result;
    }

}
