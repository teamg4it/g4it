/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SaveService {

    @Autowired
    OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Autowired
    OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    @Autowired
    OutApplicationRepository outApplicationRepository;

    @Autowired
    AggregationToOutput aggregationToOutput;

    @Autowired
    TaskRepository taskRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save physical equipments output objects
     *
     * @param aggregation   the aggregation map
     * @param taskId        the task id
     * @param refShortcutBO the ref shortcut BO
     * @return the size of saved data
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveOutPhysicalEquipments(Map<List<String>, AggValuesBO> aggregation, Long taskId, RefShortcutBO refShortcutBO) {
        List<OutPhysicalEquipment> outPhysicalEquipments = new ArrayList<>(Constants.BATCH_SIZE);
        Iterator<Map.Entry<List<String>, AggValuesBO>> iterator = aggregation.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<List<String>, AggValuesBO> pair = iterator.next();
            outPhysicalEquipments.add(aggregationToOutput.mapPhysicalEquipment(pair.getKey(), pair.getValue(), taskId, refShortcutBO));
            i++;
            if (i >= Constants.BATCH_SIZE) {
                outPhysicalEquipmentRepository.saveAll(outPhysicalEquipments);
                taskRepository.updateLastUpdateDate(taskId, LocalDateTime.now());
                outPhysicalEquipments.clear();
                flushAndClearEntityManager();
                i = 0;
            }
        }
        outPhysicalEquipmentRepository.saveAll(outPhysicalEquipments);
        outPhysicalEquipments.clear();
        int totalSaved = finalizeSaveAndCleanup(aggregation);
        // Populate level field from referential data in batches to avoid transaction timeout on large datasets
        populateLevelInBatches(taskId);
        return totalSaved;
    }

    /**
     * Batch the expensive populateLevelFromReferential operation to prevent transaction timeout.
     * Processes records in chunks of 10k to keep transaction scope manageable.
     *
     * @param taskId the task id
     */
    private void populateLevelInBatches(Long taskId) {
        List<OutPhysicalEquipment> records = outPhysicalEquipmentRepository.findByTaskId(taskId);
        int totalRecords = records.size();

        log.info("Starting batched level population for taskId: {} with {} records", taskId, totalRecords);

        for (int offset = 0; offset < totalRecords; offset += Constants.BATCH_SIZE) {
            int batchEnd = Math.min(offset + Constants.BATCH_SIZE, totalRecords);
            log.debug("Populating level for records {}-{} of {}", offset, batchEnd, totalRecords);
            populateLevelBatch(taskId, offset, Constants.BATCH_SIZE);
            try {
                Thread.sleep(100); // Brief pause between batches to reduce database pressure
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Batch population interrupted", e);
                break;
            }
        }

        log.info("Completed batched level population for taskId: {}", taskId);
    }

    /**
     * Populate level for a single batch using separate transaction to avoid timeout.
     *
     * @param taskId the task id
     * @param offset the offset
     * @param limit  the batch size limit
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void populateLevelBatch(Long taskId, int offset, int limit) {
        outPhysicalEquipmentRepository.populateLevelFromReferentialBatch(taskId, offset, limit);
    }

    private void flushAndClearEntityManager() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Save virtual equipments output objects
     *
     * @param aggregation the aggregation map
     * @param taskId      the task id
     * @return the size of saved data
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveOutVirtualEquipments(Map<List<String>, AggValuesBO> aggregation, Long taskId, RefShortcutBO refShortcutBO) {
        List<OutVirtualEquipment> outVirtualEquipments = new ArrayList<>(Constants.BATCH_SIZE);
        Iterator<Map.Entry<List<String>, AggValuesBO>> iterator = aggregation.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<List<String>, AggValuesBO> pair = iterator.next();
            outVirtualEquipments.add(aggregationToOutput.mapVirtualEquipment(pair.getKey(), pair.getValue(), taskId, refShortcutBO));
            i++;
            if (i >= Constants.BATCH_SIZE) {
                outVirtualEquipmentRepository.saveAll(outVirtualEquipments);
                outVirtualEquipments.clear();
                flushAndClearEntityManager();
                i = 0;
            }
        }
        outVirtualEquipmentRepository.saveAll(outVirtualEquipments);
        outVirtualEquipments.clear();
        return finalizeSaveAndCleanup(aggregation);
    }

    private int finalizeSaveAndCleanup(Map<List<String>, AggValuesBO> aggregation) {
        flushAndClearEntityManager();
        int totalSaved = aggregation.size();
        aggregation.clear();
        return totalSaved;
    }

    /**
     * Save virtual equipments output objects
     *
     * @param aggregation the aggregation map
     * @param taskId      the task id
     * @return the size of saved data
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveOutApplications(Map<List<String>, AggValuesBO> aggregation, Long taskId, RefShortcutBO refShortcutBO) {
        List<OutApplication> outApplications = new ArrayList<>(Constants.BATCH_SIZE);
        Iterator<Map.Entry<List<String>, AggValuesBO>> iterator = aggregation.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<List<String>, AggValuesBO> pair = iterator.next();
            outApplications.add(aggregationToOutput.mapApplication(pair.getKey(), pair.getValue(), taskId, refShortcutBO));
            i++;
            if (i >= Constants.BATCH_SIZE) {
                outApplicationRepository.saveAll(outApplications);
                outApplications.clear();
                flushAndClearEntityManager();
                i = 0;
            }
        }
        outApplicationRepository.saveAll(outApplications);
        outApplications.clear();
        return finalizeSaveAndCleanup(aggregation);
    }

    /**
     * Save virtual equipments output objects for cloud
     *
     * @param aggregation the aggregation map
     * @param taskId      the task id
     * @return the size of saved data
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveOutCloudVirtualEquipments(Map<List<String>, AggValuesBO> aggregation, Long taskId) {
        List<OutVirtualEquipment> outVirtualEquipments = new ArrayList<>(Constants.BATCH_SIZE);
        Iterator<Map.Entry<List<String>, AggValuesBO>> iterator = aggregation.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<List<String>, AggValuesBO> pair = iterator.next();
            outVirtualEquipments.add(aggregationToOutput.mapCloudVirtualEquipment(pair.getKey(), pair.getValue(), taskId));
            i++;
            if (i >= Constants.BATCH_SIZE) {
                outVirtualEquipmentRepository.saveAll(outVirtualEquipments);
                outVirtualEquipments.clear();
                flushAndClearEntityManager();
                i = 0;
            }
        }
        outVirtualEquipmentRepository.saveAll(outVirtualEquipments);
        outVirtualEquipments.clear();
        return finalizeSaveAndCleanup(aggregation);
    }
}
