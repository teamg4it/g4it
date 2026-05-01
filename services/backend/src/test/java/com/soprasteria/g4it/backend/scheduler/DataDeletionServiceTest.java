/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.UserRoleWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.utils.AzureEmailService;
import com.soprasteria.g4it.backend.common.utils.Constants;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @Mock
    private UserWorkspaceRepository userWorkspaceRepository;
    @Mock
    private UserRoleWorkspaceRepository userRoleWorkspaceRepository;
    @Mock
    DigitalServiceVersionRepository digitalServiceVersionRepository;
    @BeforeEach
    void setUp() {
        try {
            var mocks = MockitoAnnotations.openMocks(this);
            // Set retention days via reflection since @Value is not processed in unit tests
            setField("dataRetentiondDay", 90);
            setField("firstReminderDay", 30);
            setField("secondReminderDay", 2);
            setField("inventoryLink", "http://dummy-inventory-link/{id}");
            setField("ecoMindAiLink", "http://dummy-ecomindai-link/{id}");
            setField("digitalServiceLink", "http://dummy-ecomindai-link/{id}");
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

        // Add these lines to mock user/role for inventory reminder
        UserWorkspace userWorkspace = mockUserWorkspaceWithRole();
        when(userWorkspaceRepository.findByWorkspace(any())).thenReturn(List.of(userWorkspace));
        UserRoleWorkspace userRoleWorkspace = mockUserRoleWorkspace("ROLE_INVENTORY_WRITE");
        when(userRoleWorkspaceRepository.findByUserWorkspaces(userWorkspace)).thenReturn(List.of(userRoleWorkspace));

        when(messageSource.getMessage(eq("email.body"), any(), eq(Locale.ENGLISH))).thenReturn("bodyEn");
        when(messageSource.getMessage(eq("email.body"), any(), eq(Locale.FRANCE))).thenReturn("bodyFr");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.ENGLISH))).thenReturn("subjectEn");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.FRANCE))).thenReturn("subjectFr");

        dataDeletionService.executeDeletion();

        verify(azureEmailService, atLeastOnce()).sendEmail(
                "user@example.com",
                "subjectEn / subjectFr",
                "bodyEn\n\nbodyFr"
        );
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

    @Test
    void testHandleInventoryDeletion_sendsSecondReminder() {
        Workspace workspace = mockWorkspace();
        when(workspaceRepository.findAllByStatusIn(anyList())).thenReturn(List.of(workspace));
        var inventory = mockInventory(LocalDateTime.now().minusDays(88)); // retention=90, secondReminder=2
        when(inventoryRepository.findByWorkspace(any())).thenReturn(List.of(inventory));
        UserWorkspace userWorkspace = mockUserWorkspaceWithRole();
        when(userWorkspaceRepository.findByWorkspace(any())).thenReturn(List.of(userWorkspace));
        UserRoleWorkspace userRoleWorkspace = mockUserRoleWorkspace("ROLE_INVENTORY_WRITE");
        when(userRoleWorkspaceRepository.findByUserWorkspaces(any())).thenReturn(List.of(userRoleWorkspace));
        when(messageSource.getMessage(anyString(), any(), eq(Locale.ENGLISH))).thenReturn("bodyEn");
        when(messageSource.getMessage(anyString(), any(), eq(Locale.FRANCE))).thenReturn("bodyFr");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.ENGLISH))).thenReturn("subjectEn");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.FRANCE))).thenReturn("subjectFr");

        dataDeletionService.executeDeletion();

        verify(azureEmailService, atLeastOnce()).sendEmail(
                "user@example.com",
                "subjectEn / subjectFr",
                "bodyEn\n\nbodyFr"
        );
    }

    @Test
    void testHandleDigitalServiceDeletion_sendsFirstReminderForAI() {
        Workspace workspace = mockWorkspace();
        when(workspaceRepository.findAllByStatusIn(anyList())).thenReturn(List.of(workspace));

        DigitalServiceVersion digitalServiceVersion = mockDigitalServiceVersion("DSV1");
        var digitalService = mock(com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService.class);
        when(digitalService.getLastUpdateDate()).thenReturn(LocalDateTime.now().minusDays(60));
        when(digitalService.isAi()).thenReturn(true);
        when(digitalService.getUid()).thenReturn("UID1");
        when(digitalService.getName()).thenReturn("DS1");
        when(digitalServiceRepository.findByWorkspace(any())).thenReturn(List.of(digitalService));
        when(digitalServiceVersionRepository.findByDigitalServiceUid(anyString())).thenReturn(List.of(digitalServiceVersion));

        UserWorkspace userWorkspace = mockUserWorkspaceWithRole();
        UserRoleWorkspace userRoleWorkspace = mockUserRoleWorkspace("ROLE_ECO_MIND_AI_WRITE");
        when(userWorkspaceRepository.findByWorkspace(any())).thenReturn(List.of(userWorkspace));
        when(userRoleWorkspaceRepository.findByUserWorkspaces(userWorkspace))
                .thenReturn(List.of(userRoleWorkspace));

        when(messageSource.getMessage(anyString(), any(), eq(Locale.ENGLISH))).thenReturn("bodyEn");
        when(messageSource.getMessage(anyString(), any(), eq(Locale.FRANCE))).thenReturn("bodyFr");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.ENGLISH))).thenReturn("subjectEn");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.FRANCE))).thenReturn("subjectFr");

        dataDeletionService.executeDeletion();

        verify(azureEmailService, atLeastOnce()).sendEmail(
                "user@example.com",
                "subjectEn / subjectFr",
                "bodyEn\n\nbodyFr"
        );
    }



    @Test
    void testGetWorkspaceUsers_returnsEmptyWhenNoRolesMatch() throws Exception {
        Workspace workspace = mockWorkspace();
        UserWorkspace userWorkspace = mockUserWorkspaceWithRole();
        when(userWorkspaceRepository.findByWorkspace(any())).thenReturn(List.of(userWorkspace));
        // Fix: stub for each UserWorkspace, not for a list
        UserRoleWorkspace userRoleWorkspace = mockUserRoleWorkspace("OTHER_ROLE");
        when(userRoleWorkspaceRepository.findByUserWorkspaces(userWorkspace))
                .thenReturn(List.of(userRoleWorkspace));

        var method = DataDeletionService.class.getDeclaredMethod("getWorkspaceUsers", Workspace.class, String.class);
        method.setAccessible(true);
        List<User> users = (List<User>) method.invoke(dataDeletionService, workspace, Constants.ROLE_INVENTORY_WRITE);

        assertTrue(users.isEmpty());
    }



    @Test
    void testGetWorkspaceUsers_returnsAdmin() throws Exception {
        Workspace workspace = mockWorkspace();
        UserWorkspace userWorkspace = mockUserWorkspaceWithRole();
        when(userWorkspaceRepository.findByWorkspace(any())).thenReturn(List.of(userWorkspace));
        UserRoleWorkspace userRoleWorkspace = mockUserRoleWorkspace("ROLE_INVENTORY_WRITE");
        when(userRoleWorkspaceRepository.findByUserWorkspaces(userWorkspace))
                .thenReturn(List.of(userRoleWorkspace));

        var method = DataDeletionService.class.getDeclaredMethod("getWorkspaceUsers", Workspace.class, String.class);
        method.setAccessible(true);
        List<User> users = (List<User>) method.invoke(dataDeletionService, workspace, Constants.ROLE_INVENTORY_WRITE);

        assertFalse(users.isEmpty());
    }


    @Test
    void testSendRetentionReminderEmail_formatsFrenchBody() throws Exception {
        var method = DataDeletionService.class.getDeclaredMethod("sendRetentionReminderEmail", String.class, String.class, String.class, Integer.class, String.class);
        method.setAccessible(true);
        when(messageSource.getMessage(eq("email.body"), any(), eq(Locale.ENGLISH))).thenReturn("bodyEn");
        when(messageSource.getMessage(eq("email.body"), any(), eq(Locale.FRANCE))).thenReturn("{0} {1} {2} {3}");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.ENGLISH))).thenReturn("subjectEn");
        when(messageSource.getMessage(eq("email.subject"), any(), eq(Locale.FRANCE))).thenReturn("subjectFr");

        method.invoke(dataDeletionService, "test@example.com", "item", "2024-01-01", 90, "link");

        verify(azureEmailService).sendEmail(anyString(), contains("subjectEn"), contains("item 2024-01-01 90 link"));
    }

    // --- Helper mocks for roles ---
    private UserWorkspace mockUserWorkspaceWithRole() {
        UserWorkspace uw = mock(UserWorkspace.class);
        User user = mock(User.class);
        when(uw.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("user@example.com");
        return uw;
    }

    private com.soprasteria.g4it.backend.apiuser.modeldb.UserRoleWorkspace mockUserRoleWorkspace(String role) {
        var urw = mock(com.soprasteria.g4it.backend.apiuser.modeldb.UserRoleWorkspace.class);
        var roles = mock(com.soprasteria.g4it.backend.apiuser.modeldb.Role.class);
        when(roles.getName()).thenReturn(role);
        when(urw.getRoles()).thenReturn(roles);
        return urw;
    }

    private DigitalServiceVersion mockDigitalServiceVersion(String uid) {
        DigitalServiceVersion dsv = mock(DigitalServiceVersion.class);
        when(dsv.getUid()).thenReturn(uid);
        return dsv;
    }

}
