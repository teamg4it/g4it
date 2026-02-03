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
import com.soprasteria.g4it.backend.apiinout.mapper.OutApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutApplicationRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutApplicationServiceTest {

    @InjectMocks
    private OutApplicationService outApplicationService;

    @Mock
    private OutApplicationRepository outApplicationRepository;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OutApplicationMapper outApplicationMapper;

    @Test
    void getByInventory_returnsEmptyList_whenNoTaskFound() {
        Inventory inventory = new Inventory();

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.empty());

        List<OutApplicationRest> result =
                outApplicationService.getByInventory(inventory);

        assertEquals(List.of(), result);

        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verifyNoInteractions(outApplicationRepository, outApplicationMapper);
    }

    @Test
    void getByInventory_returnsMappedApplications_whenTaskFound() {
        Inventory inventory = new Inventory();
        Task task = new Task();
        task.setId(1L);

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        when(outApplicationRepository.findByTaskId(1L))
                .thenReturn(List.of());

        when(outApplicationMapper.toRest(List.of()))
                .thenReturn(List.of(OutApplicationRest.builder().build()));

        List<OutApplicationRest> result =
                outApplicationService.getByInventory(inventory);

        assertEquals(1, result.size());

        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verify(outApplicationRepository).findByTaskId(1L);
        verify(outApplicationMapper).toRest(List.of());
    }

    @Test
    void getByDigitalServiceVersionUid_returnsMappedApplications_whenTaskFoundOnFirstTry() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        Task task = new Task();
        task.setId(1L);

        List<OutApplication> entities = List.of(new OutApplication());
        List<OutApplicationRest> mapped =
                List.of(OutApplicationRest.builder().build());

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.of(task));

        when(outApplicationRepository.findByTaskId(1L))
                .thenReturn(entities);

        when(outApplicationMapper.toRest(entities))
                .thenReturn(mapped);

        List<OutApplicationRest> result =
                outApplicationService.getByDigitalServiceVersionUid(uid);

        assertEquals(1, result.size());

        verify(digitalServiceVersionRepository).findById(uid);
        verify(taskRepository)
                .findTopByDigitalServiceVersionOrderByIdDesc(dsv);
        verify(outApplicationRepository).findByTaskId(1L);
        verify(outApplicationMapper).toRest(entities);
    }

}
