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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutApplicationServiceTest {

    @InjectMocks
    private OutApplicationService outApplicationService;

    @Mock
    private OutApplicationRepository outApplicationRepository;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private OutApplicationMapper outApplicationMapper;

    @Test
    void getByInventory_returnsEmptyList_whenNoTaskFound() {
        Inventory inventory = new Inventory();
        when(taskRepository.findByInventoryAndLastCreationDate(inventory)).thenReturn(Optional.empty());

        List<OutApplicationRest> result = outApplicationService.getByInventory(inventory);

        assertEquals(List.of(), result);
        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
    }

    @Test
    void getByInventory_returnsMappedApplications_whenTaskFound() {
        Inventory inventory = new Inventory();
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findByInventoryAndLastCreationDate(inventory)).thenReturn(Optional.of(task));
        when(outApplicationRepository.findByTaskId(1L)).thenReturn(List.of());
        when(outApplicationMapper.toRest(List.of())).thenReturn(List.of(OutApplicationRest.builder().build()));

        List<OutApplicationRest> result = outApplicationService.getByInventory(inventory);

        assertEquals(1, result.size());
        verify(taskRepository).findByInventoryAndLastCreationDate(inventory);
        verify(outApplicationRepository).findByTaskId(1L);
        verify(outApplicationMapper).toRest(List.of());
    }

    @Test
    void getByDigitalServiceUid_returnsEmptyList_whenNoTaskFound() {
        String digitalServiceVersionUid = "uid123";

        DigitalServiceVersion digitalServiceVersion = new DigitalServiceVersion();
        digitalServiceVersion.setUid(digitalServiceVersionUid);

        when(digitalServiceVersionRepository.findById(digitalServiceVersionUid))
                .thenReturn(Optional.of(digitalServiceVersion));

        when(taskRepository.findTopByDigitalServiceVersionAndTypeAndStatusOrderByIdDesc(
                any(DigitalServiceVersion.class),
                eq("EVALUATING_DIGITAL_SERVICE"),
                eq("COMPLETED")
        )).thenReturn(Optional.empty());

        List<OutApplicationRest> result =
                outApplicationService.getByDigitalServiceVersionUid(digitalServiceVersionUid);

        assertEquals(List.of(), result);

        verify(digitalServiceVersionRepository).findById(digitalServiceVersionUid);

        verify(taskRepository).findTopByDigitalServiceVersionAndTypeAndStatusOrderByIdDesc(
                any(DigitalServiceVersion.class),
                eq("EVALUATING_DIGITAL_SERVICE"),
                eq("COMPLETED")
        );

        // optional safety checks (since task not found, these should not be called)
        verifyNoInteractions(outApplicationRepository, outApplicationMapper);
    }


    @Test
    void getByDigitalServiceUid_returnsMappedApplications_whenTaskFound() {
        String digitalServiceVersionUid = "uid123";

        Task task = new Task();
        task.setId(1L);

        DigitalServiceVersion digitalServiceVersion = new DigitalServiceVersion();
        digitalServiceVersion.setUid(digitalServiceVersionUid);

        // mock DB entity list (1 record)
        List<OutApplication> outApplications = List.of(new OutApplication());

        // mock REST mapped list (1 record)
        List<OutApplicationRest> mapped = List.of(OutApplicationRest.builder().build());

        when(digitalServiceVersionRepository.findById(digitalServiceVersionUid))
                .thenReturn(Optional.of(digitalServiceVersion));

        when(taskRepository.findTopByDigitalServiceVersionAndTypeAndStatusOrderByIdDesc(
                any(DigitalServiceVersion.class),
                eq("EVALUATING_DIGITAL_SERVICE"),
                eq("COMPLETED")
        )).thenReturn(Optional.of(task));

        when(outApplicationRepository.findByTaskId(1L))
                .thenReturn(outApplications);

        when(outApplicationMapper.toRest(outApplications))
                .thenReturn(mapped);

        List<OutApplicationRest> result =
                outApplicationService.getByDigitalServiceVersionUid(digitalServiceVersionUid);

        assertEquals(1, result.size());

        verify(digitalServiceVersionRepository).findById(digitalServiceVersionUid);

        verify(taskRepository).findTopByDigitalServiceVersionAndTypeAndStatusOrderByIdDesc(
                any(DigitalServiceVersion.class),
                eq("EVALUATING_DIGITAL_SERVICE"),
                eq("COMPLETED")
        );

        verify(outApplicationRepository).findByTaskId(1L);
        verify(outApplicationMapper).toRest(outApplications);
    }


}
