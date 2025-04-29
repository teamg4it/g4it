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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveServiceTest {

    @Mock
    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Mock
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    @Mock
    private OutApplicationRepository outApplicationRepository;

    @Mock
    private AggregationToOutput aggregationToOutput;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SaveService saveService;

    @Test
    void saveOutPhysicalEquipments_savesAllEntriesWhenAggregationIsNotEmpty() {
        Map<List<String>, AggValuesBO> aggregation = Map.of(
                List.of("key1"), new AggValuesBO(),
                List.of("key2"), new AggValuesBO()
        );
        RefShortcutBO refShortcutBO = new RefShortcutBO(
                null, null,
                null,
                null
        );
        Long taskId = 1L;

        when(aggregationToOutput.mapPhysicalEquipment(any(), any(), eq(taskId), eq(refShortcutBO)))
                .thenReturn(new OutPhysicalEquipment());

        int result = saveService.saveOutPhysicalEquipments(aggregation, taskId, refShortcutBO);

        verify(outPhysicalEquipmentRepository, times(1)).saveAll(anyList());
        assertEquals(2, result);
    }

    @Test
    void saveOutPhysicalEquipments_doesNotSaveWhenAggregationIsEmpty() {
        Map<List<String>, AggValuesBO> aggregation = Map.of();
        RefShortcutBO refShortcutBO = new RefShortcutBO(
                null, null,
                null,
                null
        );
        Long taskId = 1L;

        int result = saveService.saveOutPhysicalEquipments(aggregation, taskId, refShortcutBO);
        
        verify(taskRepository, never()).updateLastUpdateDate(anyLong(), any(LocalDateTime.class));
        assertEquals(0, result);
    }

    @Test
    void saveOutVirtualEquipments_handlesBatchProcessingCorrectly() {
        Map<List<String>, AggValuesBO> aggregation = new HashMap<>();
        for (int i = 0; i < Constants.BATCH_SIZE + 1; i++) {
            aggregation.put(List.of("key" + i), new AggValuesBO());
        }
        RefShortcutBO refShortcutBO = new RefShortcutBO(
                null, null,
                null,
                null
        );
        Long taskId = 1L;

        when(aggregationToOutput.mapVirtualEquipment(any(), any(), eq(taskId), eq(refShortcutBO)))
                .thenReturn(new OutVirtualEquipment());

        int result = saveService.saveOutVirtualEquipments(aggregation, taskId, refShortcutBO);

        verify(outVirtualEquipmentRepository, times(2)).saveAll(anyList());
        assertEquals(Constants.BATCH_SIZE + 1, result);
    }

    @Test
    void saveOutApplications_savesAllEntriesWhenAggregationIsNotEmpty() {
        Map<List<String>, AggValuesBO> aggregation = Map.of(
                List.of("key1"), new AggValuesBO(),
                List.of("key2"), new AggValuesBO()
        );
        RefShortcutBO refShortcutBO = new RefShortcutBO(null, null, null, null);
        Long taskId = 1L;

        when(aggregationToOutput.mapApplication(any(), any(), eq(taskId), eq(refShortcutBO)))
                .thenReturn(new OutApplication());

        int result = saveService.saveOutApplications(aggregation, taskId, refShortcutBO);

        verify(outApplicationRepository, times(1)).saveAll(anyList());
        assertEquals(2, result);
    }

    @Test
    void saveOutCloudVirtualEquipments_savesAllEntriesWhenAggregationIsNotEmpty() {
        Map<List<String>, AggValuesBO> aggregation = Map.of(
                List.of("key1"), new AggValuesBO(),
                List.of("key2"), new AggValuesBO()
        );
        Long taskId = 1L;

        when(aggregationToOutput.mapCloudVirtualEquipment(any(), any(), eq(taskId)))
                .thenReturn(new OutVirtualEquipment());

        int result = saveService.saveOutCloudVirtualEquipments(aggregation, taskId);

        verify(outVirtualEquipmentRepository, times(1)).saveAll(anyList());
        assertEquals(2, result);
    }
}
