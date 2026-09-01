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
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutPhysicalEquipmentServiceTest {

    @InjectMocks
    private OutPhysicalEquipmentService outPhysicalEquipmentService;

    @Mock
    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OutPhysicalEquipmentMapper outPhysicalEquipmentMapper;

    @Mock
    private EntityManager entityManager;

    @Test
    void getByDigitalServiceVersionUid_returnsEmptyList_whenNoTaskFound() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.empty());

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByDigitalServiceVersionUid(uid);

        assertEquals(List.of(), result);

        verify(digitalServiceVersionRepository).findById(uid);
        verify(taskRepository).findTopByDigitalServiceVersionOrderByIdDesc(dsv);
        verifyNoInteractions(outPhysicalEquipmentRepository, outPhysicalEquipmentMapper);
    }

    @Test
    void getByDigitalServiceVersionUid_returnsMappedEquipments_whenTaskFound() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        Task task = new Task();
        task.setId(1L);

        List<OutPhysicalEquipment> equipments =
                List.of(new OutPhysicalEquipment());

        List<OutPhysicalEquipmentRest> mapped =
                List.of(OutPhysicalEquipmentRest.builder().build());

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.of(task));

        doReturn(equipments, List.of()).when(outPhysicalEquipmentRepository)
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        when(outPhysicalEquipmentMapper.toRest(equipments))
                .thenReturn(mapped);

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByDigitalServiceVersionUid(uid);

        assertEquals(mapped, result);

        verify(digitalServiceVersionRepository).findById(uid);
        verify(taskRepository).findTopByDigitalServiceVersionOrderByIdDesc(dsv);
        verify(outPhysicalEquipmentRepository, atLeast(1))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));
        verify(outPhysicalEquipmentMapper).toRest(equipments);
    }

    @Test
    void getByDigitalServiceVersionUid_processesMultipleBatches() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        Task task = new Task();
        task.setId(1L);

        List<OutPhysicalEquipment> batch1 =
                List.of(new OutPhysicalEquipment());

        List<OutPhysicalEquipment> batch2 =
                List.of(new OutPhysicalEquipment());

        List<OutPhysicalEquipmentRest> mapped1 =
                List.of(OutPhysicalEquipmentRest.builder().build());

        List<OutPhysicalEquipmentRest> mapped2 =
                List.of(OutPhysicalEquipmentRest.builder().build());

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.of(task));

        doReturn(batch1, batch2, List.of()).when(outPhysicalEquipmentRepository)
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        when(outPhysicalEquipmentMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByDigitalServiceVersionUid(uid);

        assertEquals(2, result.size());

        verify(outPhysicalEquipmentRepository, times(3))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outPhysicalEquipmentMapper, times(2))
                .toRest(any(List.class));
    }

    @Test
    void getByInventory_returnsEmptyList_whenNoTaskFound() {
        Inventory inventory = new Inventory();

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.empty());

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByInventory(inventory);

        assertEquals(List.of(), result);

        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verifyNoInteractions(outPhysicalEquipmentRepository, outPhysicalEquipmentMapper);
    }

    @Test
    void getByInventory_returnsMappedEquipments_whenTaskFound() {
        Inventory inventory = new Inventory();

        Task task = new Task();
        task.setId(1L);

        OutPhysicalEquipment equipment = new OutPhysicalEquipment();

        List<OutPhysicalEquipmentRest> mapped =
                List.of(OutPhysicalEquipmentRest.builder().build());

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        when(outPhysicalEquipmentRepository.findByTaskId(eq(1L), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(equipment)))
                .thenReturn(List.of());

        when(outPhysicalEquipmentMapper.toRest(any(List.class)))
                .thenReturn(mapped);

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByInventory(inventory);

        assertEquals(mapped, result);

        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verify(outPhysicalEquipmentRepository, atLeast(1))
                .findByTaskId(eq(1L), any(Pageable.class));
        verify(outPhysicalEquipmentMapper).toRest(any(List.class));
    }

    @Test
    void getByInventory_processesMultipleBatches() {
        Inventory inventory = new Inventory();

        Task task = new Task();
        task.setId(1L);

        OutPhysicalEquipment equipment1 = new OutPhysicalEquipment();
        OutPhysicalEquipment equipment2 = new OutPhysicalEquipment();

        List<OutPhysicalEquipmentRest> mapped1 =
                List.of(OutPhysicalEquipmentRest.builder().build());

        List<OutPhysicalEquipmentRest> mapped2 =
                List.of(OutPhysicalEquipmentRest.builder().build());

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        when(outPhysicalEquipmentRepository.findByTaskId(eq(1L), any(Pageable.class)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(equipment1)))
                .thenAnswer(invocation -> new ArrayList<>(List.of(equipment2)))
                .thenReturn(List.of());

        when(outPhysicalEquipmentMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByInventory(inventory);

        assertEquals(2, result.size());

        verify(outPhysicalEquipmentRepository, times(3))
                .findByTaskId(eq(1L), any(Pageable.class));

        verify(outPhysicalEquipmentMapper, times(2))
                .toRest(any(List.class));
    }
}
