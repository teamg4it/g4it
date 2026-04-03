/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.utils.AzureEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataDeletionServiceTest {
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryDeleteService inventoryDeleteService;
    @Mock
    private DigitalServiceService digitalServiceService;
    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private AzureEmailService azureEmailService;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private DataDeletionService dataDeletionService;

    @BeforeEach
    void setUp() {
        try {
            var mocks = MockitoAnnotations.openMocks(this);
            // Set retention days via reflection since @Value is not processed in unit tests
            setField("dataRetentiondDay", 90);
            setField("firstReminderDay", 30);
            setField("secondReminderDay", 2);
            setField("inventoryLink", "http://dummy-inventory-link/{id}"); // Fix for NPE
            if (mocks != null) {
                mocks.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(String fieldName, Object value) {
        try {
            var field = DataDeletionService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(dataDeletionService, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testExecuteDeletion_inventoryDeletedWhenRetentionExceeded() {
        Workspace workspace = mockWorkspace();
        when(workspaceRepository.findAllByStatusIn(anyList())).thenReturn(List.of(workspace));
        var inventory = mockInventory(LocalDateTime.now().minusDays(91));
        when(inventoryRepository.findByWorkspace(any())).thenReturn(List.of(inventory));
        when(digitalServiceRepository.findByWorkspace(any())).thenReturn(Collections.emptyList());

        dataDeletionService.executeDeletion();

        verify(inventoryDeleteService).deleteInventory(anyString(), anyLong(), anyLong());
        verify(azureEmailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testExecuteDeletion_inventoryReminderSent() {
        Workspace workspace = mockWorkspace();
        when(workspaceRepository.findAllByStatusIn(anyList())).thenReturn(List.of(workspace));
        // 60 days since last update, retention=90, firstReminder=30
        var inventory = mockInventory(LocalDateTime.now().minusDays(60));
        when(inventoryRepository.findByWorkspace(any())).thenReturn(List.of(inventory));
        when(digitalServiceRepository.findByWorkspace(any())).thenReturn(Collections.emptyList());
        when(messageSource.getMessage(eq("email.body"), any(), eq(Locale.ENGLISH))).thenReturn("bodyEn");
        when(messageSource.getMessage(eq("email.body"), any(), eq(Locale.FRANCE))).thenReturn("bodyFr");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.ENGLISH))).thenReturn("subjectEn");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.FRANCE))).thenReturn("subjectFr");

        dataDeletionService.executeDeletion();

        verify(azureEmailService, atLeastOnce()).sendEmail(anyString(), contains("subjectEn"), contains("bodyEn"));
        verify(inventoryDeleteService, never()).deleteInventory(anyString(), anyLong(), anyLong());
    }

    @Test
    void testExecuteDeletion_digitalServiceDeletedWhenRetentionExceeded() {
        Workspace workspace = mockWorkspace();
        when(workspaceRepository.findAllByStatusIn(anyList())).thenReturn(List.of(workspace));
        when(inventoryRepository.findByWorkspace(any())).thenReturn(Collections.emptyList());
        // Use DigitalService instead of DigitalServiceBO
        var digitalService = mock(com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService.class);
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@example.com");
        when(digitalService.getUser()).thenReturn(user);
        when(digitalService.getName()).thenReturn("DS1");
        when(digitalService.getLastUpdateDate()).thenReturn(LocalDateTime.now().minusDays(91));
        when(digitalService.getUid()).thenReturn("UID1");
        when(digitalServiceRepository.findByWorkspace(any())).thenReturn(List.of(digitalService));

        dataDeletionService.executeDeletion();

        verify(digitalServiceService).deleteDigitalService(anyString());
        verify(azureEmailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testExecuteDeletion_noActionWhenNothingToDeleteOrRemind() {
        Workspace workspace = mockWorkspace();
        when(workspaceRepository.findAllByStatusIn(anyList())).thenReturn(List.of(workspace));
        when(inventoryRepository.findByWorkspace(any())).thenReturn(Collections.emptyList());
        when(digitalServiceRepository.findByWorkspace(any())).thenReturn(Collections.emptyList());

        dataDeletionService.executeDeletion();

        verifyNoInteractions(inventoryDeleteService);
        verifyNoInteractions(digitalServiceService);
        verifyNoInteractions(azureEmailService);
    }

    // --- Helper mocks ---
    private Workspace mockWorkspace() {
        Workspace ws = mock(Workspace.class);
        Organization org = mock(Organization.class);
        when(org.getName()).thenReturn("ORG");
        when(org.getDataRetentionDay()).thenReturn(null);
        when(ws.getOrganization()).thenReturn(org);
        when(ws.getId()).thenReturn(1L);
        when(ws.getDataRetentionDay()).thenReturn(null);
        return ws;
    }

    private com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory mockInventory(LocalDateTime lastUpdate) {
        var inv = mock(com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory.class);
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@example.com");
        when(inv.getCreatedBy()).thenReturn(user);
        when(inv.getName()).thenReturn("Inventory1");
        when(inv.getLastUpdateDate()).thenReturn(lastUpdate);
        when(inv.getId()).thenReturn(100L);
        return inv;
    }
}
