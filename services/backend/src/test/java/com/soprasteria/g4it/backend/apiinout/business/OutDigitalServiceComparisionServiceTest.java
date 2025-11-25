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
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionComparison;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutDigitalServiceComparisonServiceTest {

    @Mock
    private OutPhysicalEquipmentService outPhysicalEquipmentService;

    @Mock
    private OutVirtualEquipmentService outVirtualEquipmentService;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private OutDigitalServiceComparisonService outDigitalServiceComparisonService;

    // -------------------------------------------------------------------------
    // Test: Both versions have NO tasks → physical + virtual lists must be empty
    // -------------------------------------------------------------------------
    @Test
    void compareDigitalServiceVersions_returnsEmptyLists_whenNoTasksFound() {
        String versionAUid = "A";
        String versionBUid = "B";

        DigitalServiceVersion versionA = mock(DigitalServiceVersion.class);
        DigitalServiceVersion versionB = mock(DigitalServiceVersion.class);

        when(versionA.getDescription()).thenReturn("Version A");
        when(versionB.getDescription()).thenReturn("Version B");

        when(digitalServiceVersionRepository.findById(versionAUid)).thenReturn(Optional.of(versionA));
        when(digitalServiceVersionRepository.findById(versionBUid)).thenReturn(Optional.of(versionB));

        when(taskRepository.findByDigitalServiceVersionAndLastCreationDate(versionA)).thenReturn(Optional.empty());
        when(taskRepository.findByDigitalServiceVersionAndLastCreationDate(versionB)).thenReturn(Optional.empty());

        List<DigitalServiceVersionComparison> result =
                outDigitalServiceComparisonService.compareDigitalServiceVersions(versionAUid, versionBUid);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getPhysicalEquipment().isEmpty());
        assertTrue(result.get(0).getVirtualEquipment().isEmpty());
        assertTrue(result.get(1).getPhysicalEquipment().isEmpty());
        assertTrue(result.get(1).getVirtualEquipment().isEmpty());

        verify(taskRepository).findByDigitalServiceVersionAndLastCreationDate(versionA);
        verify(taskRepository).findByDigitalServiceVersionAndLastCreationDate(versionB);

        verifyNoInteractions(outPhysicalEquipmentService, outVirtualEquipmentService);
    }

    // -------------------------------------------------------------------------
    // Test: Both versions have tasks → physical + virtual lists returned
    // -------------------------------------------------------------------------
    @Test
    void compareDigitalServiceVersions_returnsMappedLists_whenTasksFound() {
        String versionAUid = "A";
        String versionBUid = "B";

        DigitalServiceVersion versionA = mock(DigitalServiceVersion.class);
        DigitalServiceVersion versionB = mock(DigitalServiceVersion.class);

        when(versionA.getDescription()).thenReturn("Version A");
        when(versionB.getDescription()).thenReturn("Version B");

        Task taskA = new Task();
        taskA.setId(1L);
        Task taskB = new Task();
        taskB.setId(2L);

        when(digitalServiceVersionRepository.findById(versionAUid)).thenReturn(Optional.of(versionA));
        when(digitalServiceVersionRepository.findById(versionBUid)).thenReturn(Optional.of(versionB));

        when(taskRepository.findByDigitalServiceVersionAndLastCreationDate(versionA)).thenReturn(Optional.of(taskA));
        when(taskRepository.findByDigitalServiceVersionAndLastCreationDate(versionB)).thenReturn(Optional.of(taskB));

        // --- Mock DTOs because constructors are protected ---
        OutPhysicalEquipmentRest physicalA = mock(OutPhysicalEquipmentRest.class);
        OutVirtualEquipmentRest virtualA = mock(OutVirtualEquipmentRest.class);
        OutPhysicalEquipmentRest physicalB = mock(OutPhysicalEquipmentRest.class);
        OutVirtualEquipmentRest virtualB = mock(OutVirtualEquipmentRest.class);

        when(outPhysicalEquipmentService.getByDigitalServiceVersionUid(versionAUid))
                .thenReturn(List.of(physicalA));
        when(outVirtualEquipmentService.getByDigitalServiceVersionUid(versionAUid))
                .thenReturn(List.of(virtualA));

        when(outPhysicalEquipmentService.getByDigitalServiceVersionUid(versionBUid))
                .thenReturn(List.of(physicalB));
        when(outVirtualEquipmentService.getByDigitalServiceVersionUid(versionBUid))
                .thenReturn(List.of(virtualB));

        List<DigitalServiceVersionComparison> result =
                outDigitalServiceComparisonService.compareDigitalServiceVersions(versionAUid, versionBUid);

        assertEquals(2, result.size());
        assertFalse(result.get(0).getPhysicalEquipment().isEmpty());
        assertFalse(result.get(0).getVirtualEquipment().isEmpty());
        assertFalse(result.get(1).getPhysicalEquipment().isEmpty());
        assertFalse(result.get(1).getVirtualEquipment().isEmpty());

        verify(outPhysicalEquipmentService).getByDigitalServiceVersionUid(versionAUid);
        verify(outVirtualEquipmentService).getByDigitalServiceVersionUid(versionAUid);
        verify(outPhysicalEquipmentService).getByDigitalServiceVersionUid(versionBUid);
        verify(outVirtualEquipmentService).getByDigitalServiceVersionUid(versionBUid);
    }

    // -------------------------------------------------------------------------
    // Test: Version not found → NoSuchElementException thrown
    // -------------------------------------------------------------------------
    @Test
    void compareDigitalServiceVersions_throwsException_whenVersionNotFound() {
        String versionAUid = "A";

        when(digitalServiceVersionRepository.findById(versionAUid)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> outDigitalServiceComparisonService.compareDigitalServiceVersions(versionAUid, "B")
        );

        verify(taskRepository, never()).findByDigitalServiceVersionAndLastCreationDate(any());
    }
}
