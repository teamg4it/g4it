/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apiinout.mapper.OutVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
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
class OutVirtualEquipmentServiceTest {

    @Mock
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OutVirtualEquipmentMapper outVirtualEquipmentMapper;

    @InjectMocks
    private OutVirtualEquipmentService outVirtualEquipmentService;

    @Test
    void getByInventory_returnsEmptyList_whenNoTaskFound() {
        Inventory inventory = new Inventory();
        when(taskRepository.findByInventoryAndLastCreationDate(inventory)).thenReturn(Optional.empty());

        List<OutVirtualEquipmentRest> result = outVirtualEquipmentService.getByInventory(inventory);

        assertTrue(result.isEmpty());
        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verifyNoInteractions(outVirtualEquipmentRepository, outVirtualEquipmentMapper);
    }

    @Test
    void getByInventory_returnsMappedVirtualEquipments_whenTaskFound() {
        Inventory inventory = new Inventory();
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findByInventoryAndLastCreationDate(inventory)).thenReturn(Optional.of(task));
        when(outVirtualEquipmentRepository.findByTaskId(task.getId())).thenReturn(List.of());
        when(outVirtualEquipmentMapper.toRest(anyList())).thenReturn(List.of(OutVirtualEquipmentRest.builder().build()));

        List<OutVirtualEquipmentRest> result = outVirtualEquipmentService.getByInventory(inventory);

        assertFalse(result.isEmpty());
        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verify(outVirtualEquipmentRepository).findByTaskId(task.getId());
        verify(outVirtualEquipmentMapper).toRest(anyList());
    }

    @Test
    void getByDigitalServiceUid_returnsEmptyList_whenNoTaskFound() {
        String digitalServiceUid = "test-uid";
        when(taskRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(Optional.empty());

        List<OutVirtualEquipmentRest> result = outVirtualEquipmentService.getByDigitalServiceUid(digitalServiceUid);

        assertTrue(result.isEmpty());
        verify(taskRepository).findByDigitalServiceUid(digitalServiceUid);
        verifyNoInteractions(outVirtualEquipmentRepository, outVirtualEquipmentMapper);
    }

    @Test
    void getByDigitalServiceUid_returnsMappedVirtualEquipments_whenTaskFound() {
        String digitalServiceUid = "test-uid";
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(Optional.of(task));
        when(outVirtualEquipmentRepository.findByTaskId(task.getId())).thenReturn(List.of());
        when(outVirtualEquipmentMapper.toRest(anyList())).thenReturn(List.of(OutVirtualEquipmentRest.builder().build()));

        List<OutVirtualEquipmentRest> result = outVirtualEquipmentService.getByDigitalServiceUid(digitalServiceUid);

        assertFalse(result.isEmpty());
        verify(taskRepository).findByDigitalServiceUid(digitalServiceUid);
        verify(outVirtualEquipmentRepository).findByTaskId(task.getId());
        verify(outVirtualEquipmentMapper).toRest(anyList());
    }
}