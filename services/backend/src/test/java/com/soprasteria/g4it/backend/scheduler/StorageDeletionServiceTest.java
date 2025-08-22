/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileDeletionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageDeletionServiceTest {

    @InjectMocks
    StorageDeletionService storageDeletionService;

    @Mock
    WorkspaceRepository workspaceRepository;

    @Mock
    FileDeletionService fileDeletionService;

    @Test
    void testStorageDeletionService_zeroOrganization() {
        Mockito.when(workspaceRepository.findAllByStatusIn(List.of(OrganizationStatus.ACTIVE.name()))).thenReturn(List.of());
        storageDeletionService.executeDeletion();
        Mockito.verify(workspaceRepository).findAllByStatusIn(List.of(OrganizationStatus.ACTIVE.name()));
    }

    @Test
    void testStorageDeletionService_organizationAndSubscriberChosen() {

        ReflectionTestUtils.setField(storageDeletionService, "storageRetentionDayExport", 100);
        ReflectionTestUtils.setField(storageDeletionService, "storageRetentionDayOutput", 300);

        List<Workspace> workspaces = List.of(Workspace.builder()
                .storageRetentionDayExport(10) // value chosen
                .subscriber(Subscriber.builder()
                        .name("sub")
                        .storageRetentionDayExport(10000)
                        .storageRetentionDayOutput(30000) // value chosen
                        .build())
                .name("org")
                .status(OrganizationStatus.ACTIVE.name())
                .build());

        Mockito.when(workspaceRepository.findAllByStatusIn(List.of(OrganizationStatus.ACTIVE.name()))).thenReturn(workspaces);
        Mockito.when(fileDeletionService.deleteFiles(any(), any(), any(), any())).thenReturn(List.of());

        // EXECUTE
        storageDeletionService.executeDeletion();

        verify(fileDeletionService, times(1)).deleteFiles(any(), any(), eq(FileFolder.EXPORT), eq(10));
        verify(fileDeletionService, times(1)).deleteFiles(any(), any(), eq(FileFolder.OUTPUT), eq(30000));
    }

    @Test
    void testStorageDeletionService_defaultChosen() {

        ReflectionTestUtils.setField(storageDeletionService, "storageRetentionDayExport", 100);
        ReflectionTestUtils.setField(storageDeletionService, "storageRetentionDayOutput", 300); // value chosen

        List<Workspace> workspaces = List.of(Workspace.builder()
                .storageRetentionDayExport(10) // value chosen
                .subscriber(Subscriber.builder()
                        .name("sub")
                        .build())
                .name("org")
                .status(OrganizationStatus.ACTIVE.name())
                .build());

        Mockito.when(workspaceRepository.findAllByStatusIn(List.of(OrganizationStatus.ACTIVE.name()))).thenReturn(workspaces);
        Mockito.when(fileDeletionService.deleteFiles(any(), any(), any(), any())).thenReturn(List.of());

        // EXECUTE
        storageDeletionService.executeDeletion();

        verify(fileDeletionService, times(1)).deleteFiles(any(), any(), eq(FileFolder.EXPORT), eq(10));
        verify(fileDeletionService, times(1)).deleteFiles(any(), any(), eq(FileFolder.OUTPUT), eq(300));
    }

}
