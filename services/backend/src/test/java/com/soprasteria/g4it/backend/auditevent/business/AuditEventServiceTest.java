/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.auditevent.business;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import com.soprasteria.g4it.backend.auditevent.model.AuditContext;
import com.soprasteria.g4it.backend.auditevent.model.AuditEventType;
import com.soprasteria.g4it.backend.auditevent.model.AuditStatus;
import com.soprasteria.g4it.backend.auditevent.modeldb.AuditEvent;
import com.soprasteria.g4it.backend.auditevent.repository.AuditEventRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.common.functional.SupplierWithException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AuditEventService auditEventService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(auditEventService, "entityManager", entityManager);
    }

    @Test
    void execute_success() throws Exception {

        AuditContext ctx = AuditContext.builder()
                .userId(1L)
                .userEmail("test@mail.com")
                .organization("ORG")
                .workspaceId(2L)
                .action(AuditEventType.IMPORT_WORKSPACE_REFERENTIAL)
                .endpoint("endpoint")
                .build();

        User user = new User();
        Workspace workspace = new Workspace();

        when(entityManager.getReference(User.class, 1L)).thenReturn(user);
        when(entityManager.getReference(Workspace.class, 2L)).thenReturn(workspace);

        AtomicReference<AuditEvent> savedEvent = new AtomicReference<>();

        when(auditEventRepository.save(any())).thenAnswer(invocation -> {
            AuditEvent e = invocation.getArgument(0);
            savedEvent.set(e);
            return e;
        });

        String result = auditEventService.execute(ctx, () -> "OK");

        assertEquals("OK", result);
        assertEquals(AuditStatus.SUCCESS, savedEvent.get().getStatus());
        assertNotNull(savedEvent.get().getEndTime());

        verify(auditEventRepository, times(2)).save(any());
    }

    @Test
    void execute_runtimeException() {

        AuditContext ctx = AuditContext.builder()
                .userId(1L)
                .workspaceId(2L)
                .action(AuditEventType.IMPORT_WORKSPACE_REFERENTIAL)
                .endpoint("endpoint")
                .build();

        when(entityManager.getReference(eq(User.class), any()))
                .thenReturn(new User());

        when(entityManager.getReference(eq(Workspace.class), any()))
                .thenReturn(new Workspace());

        AtomicReference<AuditEvent> savedEvent = new AtomicReference<>();

        when(auditEventRepository.save(any())).thenAnswer(invocation -> {
            AuditEvent e = invocation.getArgument(0);
            savedEvent.set(e);
            return e;
        });

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> auditEventService.execute(ctx, () -> {
                    throw new RuntimeException("fail");
                })
        );

        assertEquals("fail", ex.getMessage());
        assertEquals(AuditStatus.FAILED, savedEvent.get().getStatus());
        assertNotNull(savedEvent.get().getEndTime());

        verify(auditEventRepository, times(2)).save(any());
    }

    @Test
    void execute_checkedException_wrapped() {

        AuditContext ctx = AuditContext.builder()
                .action(AuditEventType.IMPORT_WORKSPACE_REFERENTIAL)
                .endpoint("endpoint")
                .build();

        AtomicReference<AuditEvent> savedEvent = new AtomicReference<>();

        when(auditEventRepository.save(any())).thenAnswer(invocation -> {
            AuditEvent e = invocation.getArgument(0);
            savedEvent.set(e);
            return e;
        });

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> auditEventService.execute(ctx, () -> {
                    throw new Exception("checked");
                })
        );

        assertTrue(ex.getCause() instanceof Exception);
        assertEquals("checked", ex.getCause().getMessage());

        assertEquals(AuditStatus.FAILED, savedEvent.get().getStatus());

        verify(auditEventRepository, times(2)).save(any());
    }

    @Test
    void execute_nullUserAndWorkspace() throws Exception {

        AuditContext ctx = AuditContext.builder()
                .action(AuditEventType.IMPORT_WORKSPACE_REFERENTIAL)
                .endpoint("endpoint")
                .build();

        AtomicReference<AuditEvent> savedEvent = new AtomicReference<>();

        when(auditEventRepository.save(any())).thenAnswer(invocation -> {
            AuditEvent e = invocation.getArgument(0);
            savedEvent.set(e);
            return e;
        });

        String result = auditEventService.execute(ctx, () -> "OK");

        assertEquals("OK", result);
        assertNull(savedEvent.get().getUser());
        assertNull(savedEvent.get().getWorkspace());
    }
}