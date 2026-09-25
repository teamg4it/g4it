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
import com.soprasteria.g4it.backend.apiinout.modeldb.OutAiService;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutAiServiceRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SaveServiceTest {

    @Mock
    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Mock
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    @Mock
    private OutAiServiceRepository outAiServiceRepository;

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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                saveService,
                "entityManager",
                entityManager
        );
    }

    @Test
    void saveOutPhysicalEquipments_shouldSaveAllEntries() {
        Map<List<String>, AggValuesBO> aggregation = new HashMap<>(Map.of(
                List.of("key1"), new AggValuesBO(),
                List.of("key2"), new AggValuesBO()
        ));

        RefShortcutBO refShortcutBO =
                new RefShortcutBO(null, null, null, null, null);

        Long taskId = 1L;

        when(aggregationToOutput.mapPhysicalEquipment(
                any(),
                any(),
                eq(taskId),
                eq(refShortcutBO)
        )).thenReturn(new OutPhysicalEquipment());

        int result = saveService.saveOutPhysicalEquipments(
                aggregation,
                taskId,
                refShortcutBO
        );

        verify(outPhysicalEquipmentRepository, times(1))
                .saveAll(anyList());

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(2, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutPhysicalEquipments_shouldNotUpdateLastUpdateDate_whenAggregationIsEmpty() {
        Map<List<String>, AggValuesBO> aggregation = new HashMap<>();

        RefShortcutBO refShortcutBO =
                new RefShortcutBO(null, null, null, null, null);

        Long taskId = 1L;

        int result = saveService.saveOutPhysicalEquipments(
                aggregation,
                taskId,
                refShortcutBO
        );

        verify(outPhysicalEquipmentRepository, times(1))
                .saveAll(anyList());

        verify(taskRepository, never())
                .updateLastUpdateDate(
                        anyLong(),
                        any(LocalDateTime.class)
                );

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(0, result);
    }

    @Test
    void saveOutPhysicalEquipments_shouldFlushWhenBatchSizeIsReached() {
        int batch = Constants.BATCH_SIZE;

        Map<List<String>, AggValuesBO> aggregation = new HashMap<>();

        for (int i = 0; i < batch + 1; i++) {
            aggregation.put(
                    List.of("key" + i),
                    new AggValuesBO()
            );
        }

        RefShortcutBO refShortcutBO =
                new RefShortcutBO(null, null, null, null, null);

        Long taskId = 1L;

        when(aggregationToOutput.mapPhysicalEquipment(
                any(),
                any(),
                eq(taskId),
                eq(refShortcutBO)
        )).thenReturn(new OutPhysicalEquipment());

        int result = saveService.saveOutPhysicalEquipments(
                aggregation,
                taskId,
                refShortcutBO
        );

        // One batch + remaining entry
        verify(outPhysicalEquipmentRepository, times(2))
                .saveAll(anyList());

        verify(taskRepository, times(1))
                .updateLastUpdateDate(
                        eq(taskId),
                        any(LocalDateTime.class)
                );

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(batch + 1, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutVirtualEquipments_shouldSaveAllEntries() {
        Map<List<String>, AggValuesBO> aggregation = new HashMap<>(Map.of(
                List.of("key1"), new AggValuesBO(),
                List.of("key2"), new AggValuesBO()
        ));

        RefShortcutBO refShortcutBO =
                new RefShortcutBO(null, null, null, null, null);

        Long taskId = 1L;

        when(aggregationToOutput.mapVirtualEquipment(
                any(),
                any(),
                eq(taskId),
                eq(refShortcutBO)
        )).thenReturn(new OutVirtualEquipment());

        int result = saveService.saveOutVirtualEquipments(
                aggregation,
                taskId,
                refShortcutBO
        );

        verify(outVirtualEquipmentRepository, times(1))
                .saveAll(anyList());

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(2, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutVirtualEquipments_shouldFlushWhenBatchSizeIsReached() {
        int batch = Constants.BATCH_SIZE;

        Map<List<String>, AggValuesBO> aggregation = new HashMap<>();

        for (int i = 0; i < batch + 1; i++) {
            aggregation.put(
                    List.of("key" + i),
                    new AggValuesBO()
            );
        }

        RefShortcutBO refShortcutBO =
                new RefShortcutBO(null, null, null, null, null);

        Long taskId = 1L;

        when(aggregationToOutput.mapVirtualEquipment(
                any(),
                any(),
                eq(taskId),
                eq(refShortcutBO)
        )).thenReturn(new OutVirtualEquipment());

        int result = saveService.saveOutVirtualEquipments(
                aggregation,
                taskId,
                refShortcutBO
        );

        verify(outVirtualEquipmentRepository, times(2))
                .saveAll(anyList());

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(batch + 1, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutApplications_shouldSaveAllEntries() {
        Map<List<String>, AggValuesBO> aggregation = new HashMap<>(Map.of(
                List.of("key1"), new AggValuesBO(),
                List.of("key2"), new AggValuesBO()
        ));

        RefShortcutBO refShortcutBO =
                new RefShortcutBO(null, null, null, null, null);

        Long taskId = 1L;

        when(aggregationToOutput.mapApplication(
                any(),
                any(),
                eq(taskId),
                eq(refShortcutBO)
        )).thenReturn(new OutApplication());

        int result = saveService.saveOutApplications(
                aggregation,
                taskId,
                refShortcutBO
        );

        verify(outApplicationRepository, times(1))
                .saveAll(anyList());

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(2, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutApplications_shouldFlushWhenBatchSizeIsReached() {
        int batch = Constants.BATCH_SIZE;

        Map<List<String>, AggValuesBO> aggregation = new HashMap<>();

        for (int i = 0; i < batch + 1; i++) {
            aggregation.put(
                    List.of("key" + i),
                    new AggValuesBO()
            );
        }

        RefShortcutBO refShortcutBO =
                new RefShortcutBO(null, null, null, null, null);

        Long taskId = 1L;

        when(aggregationToOutput.mapApplication(
                any(),
                any(),
                eq(taskId),
                eq(refShortcutBO)
        )).thenReturn(new OutApplication());

        int result = saveService.saveOutApplications(
                aggregation,
                taskId,
                refShortcutBO
        );

        verify(outApplicationRepository, times(2))
                .saveAll(anyList());

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(batch + 1, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutCloudVirtualEquipments_shouldSaveAllEntries() {
        Map<List<String>, AggValuesBO> aggregation = new HashMap<>(Map.of(
                List.of("key1"), new AggValuesBO(),
                List.of("key2"), new AggValuesBO()
        ));

        Long taskId = 1L;

        when(aggregationToOutput.mapCloudVirtualEquipment(
                any(),
                any(),
                eq(taskId)
        )).thenReturn(new OutVirtualEquipment());

        int result = saveService.saveOutCloudVirtualEquipments(
                aggregation,
                taskId
        );

        verify(outVirtualEquipmentRepository, times(1))
                .saveAll(anyList());

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(2, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutCloudVirtualEquipments_shouldFlushWhenBatchSizeIsReached() {
        int batch = Constants.BATCH_SIZE;

        Map<List<String>, AggValuesBO> aggregation = new HashMap<>();

        for (int i = 0; i < batch + 1; i++) {
            aggregation.put(
                    List.of("key" + i),
                    new AggValuesBO()
            );
        }

        Long taskId = 1L;

        when(aggregationToOutput.mapCloudVirtualEquipment(
                any(),
                any(),
                eq(taskId)
        )).thenReturn(new OutVirtualEquipment());

        int result = saveService.saveOutCloudVirtualEquipments(
                aggregation,
                taskId
        );

        verify(outVirtualEquipmentRepository, times(2))
                .saveAll(anyList());

        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();

        assertEquals(batch + 1, result);
        assertEquals(0, aggregation.size());
    }

    @Test
    void saveOutAiServices_shouldReturnZero_whenListIsEmpty() {
        List<OutAiService> outAiServices = new ArrayList<>();

        int result = saveService.saveOutAiServices(outAiServices);

        verify(outAiServiceRepository, never()).saveAll(anyList());
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();

        assertEquals(0, result);
    }

    @Test
    void saveOutAiServices_shouldSaveAllEntries_whenListIsNotEmpty() {
        List<OutAiService> outAiServices = new ArrayList<>();
        outAiServices.add(new OutAiService());
        outAiServices.add(new OutAiService());

        int result = saveService.saveOutAiServices(outAiServices);

        verify(outAiServiceRepository, times(1))
                .saveAll(anyList());

        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();

        assertEquals(2, result);
    }

    @Test
    void saveOutAiServices_shouldFlushWhenBatchSizeIsReached() {
        List<OutAiService> outAiServices = new ArrayList<>();

        for (int i = 0; i < Constants.BATCH_SIZE + 1; i++) {
            outAiServices.add(new OutAiService());
        }

        int result = saveService.saveOutAiServices(outAiServices);

        verify(outAiServiceRepository, times(2))
                .saveAll(anyList());

        verify(entityManager, times(2)).flush();
        verify(entityManager, times(2)).clear();

        assertEquals(Constants.BATCH_SIZE + 1, result);
    }
}