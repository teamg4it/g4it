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
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutPhysicalEquipmentServiceTest {

    @Mock
    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private OutPhysicalEquipmentMapper outPhysicalEquipmentMapper;

    @InjectMocks
    private OutPhysicalEquipmentService outPhysicalEquipmentService;


    @Test
    void getByDigitalServiceUid_returnsMappedList_whenTaskFoundOnFirstTry() {
        String uid = "valid-uid";

        DigitalServiceVersion digitalServiceVersion = new DigitalServiceVersion();
        digitalServiceVersion.setUid(uid);

        Task task = new Task();
        task.setId(1L);

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(digitalServiceVersion));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion))
                .thenReturn(Optional.of(task));

        when(outPhysicalEquipmentRepository.findByTaskId(task.getId()))
                .thenReturn(List.of());

        when(outPhysicalEquipmentMapper.toRest(anyList()))
                .thenReturn(List.of(OutPhysicalEquipmentRest.builder().build()));

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByDigitalServiceVersionUid(uid);

        assertFalse(result.isEmpty());

        verify(digitalServiceVersionRepository).findById(uid);
        verify(taskRepository)
                .findTopByDigitalServiceVersionOrderByIdDesc(digitalServiceVersion);
        verify(outPhysicalEquipmentRepository).findByTaskId(task.getId());
        verify(outPhysicalEquipmentMapper).toRest(anyList());
    }


    @Test
    void getByInventory_returnsEmptyList_whenTaskNotFound() {
        Inventory inventory = new Inventory();

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.empty());

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByInventory(inventory);

        assertTrue(result.isEmpty());

        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verifyNoInteractions(outPhysicalEquipmentRepository, outPhysicalEquipmentMapper);
    }

    @Test
    void getByInventory_returnsMappedList_whenTaskFound() {
        Inventory inventory = new Inventory();
        Task task = new Task();
        task.setId(1L);

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        when(outPhysicalEquipmentRepository.findByTaskId(task.getId()))
                .thenReturn(List.of());

        when(outPhysicalEquipmentMapper.toRest(anyList()))
                .thenReturn(List.of(OutPhysicalEquipmentRest.builder().build()));

        List<OutPhysicalEquipmentRest> result =
                outPhysicalEquipmentService.getByInventory(inventory);

        assertFalse(result.isEmpty());

        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verify(outPhysicalEquipmentRepository).findByTaskId(task.getId());
        verify(outPhysicalEquipmentMapper).toRest(anyList());
    }
}
