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
import com.soprasteria.g4it.backend.apiinout.mapper.OutVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutVirtualEquipmentServiceTest {

    @InjectMocks
    private OutVirtualEquipmentService outVirtualEquipmentService;

    @Mock
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OutVirtualEquipmentMapper outVirtualEquipmentMapper;

    @Mock
    private EntityManager entityManager;

    @Test
    void getByInventory_returnsEmptyList_whenNoTaskFound() {
        Inventory inventory = new Inventory();

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.empty());

        List<OutVirtualEquipmentRest> result =
                outVirtualEquipmentService.getByInventory(inventory);

        assertEquals(List.of(), result);

        verify(taskRepository)
                .findByInventoryAndLastCreationDate(inventory);

        verifyNoInteractions(
                outVirtualEquipmentRepository,
                outVirtualEquipmentMapper
        );
    }

    @Test
    void getByInventory_returnsMappedVirtualEquipments_whenTaskFound() {
        Inventory inventory = new Inventory();

        Task task = new Task();
        task.setId(1L);

        List<OutVirtualEquipment> virtualEquipments =
                List.of(new OutVirtualEquipment());

        List<OutVirtualEquipmentRest> mapped =
                List.of(OutVirtualEquipmentRest.builder().build());

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        doReturn(virtualEquipments, List.of()).when(outVirtualEquipmentRepository)
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        when(outVirtualEquipmentMapper.toRest(virtualEquipments))
                .thenReturn(mapped);

        List<OutVirtualEquipmentRest> result =
                outVirtualEquipmentService.getByInventory(inventory);

        assertEquals(mapped, result);

        verify(taskRepository)
                .findByInventoryAndLastCreationDate(inventory);

        verify(outVirtualEquipmentRepository, atLeast(1))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outVirtualEquipmentMapper)
                .toRest(virtualEquipments);
    }

    @Test
    void getByDigitalServiceVersionUid_returnsEmptyList_whenNoTaskFound() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.empty());

        List<OutVirtualEquipmentRest> result =
                outVirtualEquipmentService.getByDigitalServiceVersionUid(uid);

        assertEquals(List.of(), result);

        verify(digitalServiceVersionRepository)
                .findById(uid);

        verify(taskRepository)
                .findTopByDigitalServiceVersionOrderByIdDesc(dsv);

        verifyNoInteractions(
                outVirtualEquipmentRepository,
                outVirtualEquipmentMapper
        );
    }

    @Test
    void getByDigitalServiceVersionUid_returnsMappedVirtualEquipments_whenTaskFound() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        Task task = new Task();
        task.setId(1L);

        List<OutVirtualEquipment> virtualEquipments =
                List.of(new OutVirtualEquipment());

        List<OutVirtualEquipmentRest> mapped =
                List.of(OutVirtualEquipmentRest.builder().build());

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.of(task));

        doReturn(virtualEquipments, List.of()).when(outVirtualEquipmentRepository)
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        when(outVirtualEquipmentMapper.toRest(virtualEquipments))
                .thenReturn(mapped);

        List<OutVirtualEquipmentRest> result =
                outVirtualEquipmentService.getByDigitalServiceVersionUid(uid);

        assertEquals(mapped, result);

        verify(digitalServiceVersionRepository)
                .findById(uid);

        verify(taskRepository)
                .findTopByDigitalServiceVersionOrderByIdDesc(dsv);

        verify(outVirtualEquipmentRepository, atLeast(1))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outVirtualEquipmentMapper)
                .toRest(virtualEquipments);
    }

    @Test
    void getByInventory_processesMultipleBatches() {
        Inventory inventory = new Inventory();

        Task task = new Task();
        task.setId(1L);

        List<OutVirtualEquipment> batch1 =
                List.of(new OutVirtualEquipment());

        List<OutVirtualEquipment> batch2 =
                List.of(new OutVirtualEquipment());

        List<OutVirtualEquipmentRest> mapped1 =
                List.of(OutVirtualEquipmentRest.builder().build());

        List<OutVirtualEquipmentRest> mapped2 =
                List.of(OutVirtualEquipmentRest.builder().build());

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        doReturn(batch1, batch2, List.of()).when(outVirtualEquipmentRepository)
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        when(outVirtualEquipmentMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<OutVirtualEquipmentRest> result =
                outVirtualEquipmentService.getByInventory(inventory);

        assertEquals(2, result.size());

        verify(outVirtualEquipmentRepository, times(3))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outVirtualEquipmentMapper, times(2)).toRest(any(List.class));
    }

    @Test
    void getByDigitalServiceVersionUid_processesMultipleBatches() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        Task task = new Task();
        task.setId(1L);

        List<OutVirtualEquipment> batch1 =
                List.of(new OutVirtualEquipment());

        List<OutVirtualEquipment> batch2 =
                List.of(new OutVirtualEquipment());

        List<OutVirtualEquipmentRest> mapped1 =
                List.of(OutVirtualEquipmentRest.builder().build());

        List<OutVirtualEquipmentRest> mapped2 =
                List.of(OutVirtualEquipmentRest.builder().build());

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.of(task));

        doReturn(batch1, batch2, List.of()).when(outVirtualEquipmentRepository)
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        when(outVirtualEquipmentMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<OutVirtualEquipmentRest> result =
                outVirtualEquipmentService.getByDigitalServiceVersionUid(uid);

        assertEquals(2, result.size());

        verify(outVirtualEquipmentRepository, times(3))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outVirtualEquipmentMapper, times(2)).toRest(any(List.class));
    }
}
