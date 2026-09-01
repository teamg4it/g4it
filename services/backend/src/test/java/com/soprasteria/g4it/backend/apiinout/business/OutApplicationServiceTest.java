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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private EntityManager entityManager;

    @Test
    void getByInventory_returnsEmptyList_whenNoTaskFound() {
        Inventory inventory = new Inventory();

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.empty());

        List<OutApplicationRest> result =
                outApplicationService.getByInventory(inventory);

        assertEquals(List.of(), result);

        verify(taskRepository)
                .findByInventoryAndLastCreationDate(inventory);

        verifyNoInteractions(
                outApplicationRepository,
                outApplicationMapper
        );
    }

    @Test
    void getByInventory_returnsMappedApplications_whenTaskFound() {
        Inventory inventory = new Inventory();

        Task task = new Task();
        task.setId(1L);

        List<OutApplication> applications =
                List.of(new OutApplication());

        List<OutApplicationRest> mapped =
                List.of(OutApplicationRest.builder().build());

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        doReturn(applications, List.of()).when(outApplicationRepository).findByTaskIdOrderByIdAsc(
                eq(1L),
                any(Pageable.class)
        );

        when(outApplicationMapper.toRest(applications))
                .thenReturn(mapped);

        List<OutApplicationRest> result =
                outApplicationService.getByInventory(inventory);

        assertEquals(mapped, result);

        verify(taskRepository)
                .findByInventoryAndLastCreationDate(inventory);

        verify(outApplicationRepository, atLeast(1))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outApplicationMapper)
                .toRest(applications);
    }

    @Test
    void getByDigitalServiceVersionUid_returnsMappedApplications_whenTaskFound() {
        String uid = "uid123";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        dsv.setUid(uid);

        Task task = new Task();
        task.setId(1L);

        List<OutApplication> applications =
                List.of(new OutApplication());

        List<OutApplicationRest> mapped =
                List.of(OutApplicationRest.builder().build());

        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(dsv));

        when(taskRepository.findTopByDigitalServiceVersionOrderByIdDesc(dsv))
                .thenReturn(Optional.of(task));

        doReturn(applications, List.of()).when(outApplicationRepository).findByTaskIdOrderByIdAsc(
                eq(1L),
                any(Pageable.class)
        );

        when(outApplicationMapper.toRest(applications))
                .thenReturn(mapped);

        List<OutApplicationRest> result =
                outApplicationService.getByDigitalServiceVersionUid(uid);

        assertEquals(mapped, result);

        verify(digitalServiceVersionRepository)
                .findById(uid);

        verify(taskRepository)
                .findTopByDigitalServiceVersionOrderByIdDesc(dsv);

        verify(outApplicationRepository, atLeast(1))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outApplicationMapper)
                .toRest(applications);
    }

    @Test
    void getByInventory_processesMultipleBatches() {
        Inventory inventory = new Inventory();

        Task task = new Task();
        task.setId(1L);

        List<OutApplication> batch1 =
                List.of(new OutApplication());

        List<OutApplication> batch2 =
                List.of(new OutApplication());

        List<OutApplicationRest> mapped1 =
                List.of(OutApplicationRest.builder().build());

        List<OutApplicationRest> mapped2 =
                List.of(OutApplicationRest.builder().build());

        when(taskRepository.findByInventoryAndLastCreationDate(inventory))
                .thenReturn(Optional.of(task));

        doReturn(batch1, batch2, List.of()).when(outApplicationRepository).findByTaskIdOrderByIdAsc(
                eq(1L), any(Pageable.class));

        when(outApplicationMapper.toRest(any(List.class)))
                .thenReturn(mapped1, mapped2);

        List<OutApplicationRest> result =
                outApplicationService.getByInventory(inventory);

        assertEquals(2, result.size());

        verify(outApplicationRepository, times(3))
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));

        verify(outApplicationMapper, times(2)).toRest(any(List.class));
    }
}