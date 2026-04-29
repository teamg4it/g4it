package com.soprasteria.g4it.backend.auditevent.business;

import com.soprasteria.g4it.backend.auditevent.model.AuditContext;
import com.soprasteria.g4it.backend.auditevent.model.AuditStatus;
import com.soprasteria.g4it.backend.auditevent.modeldb.AuditEvent;
import com.soprasteria.g4it.backend.auditevent.repository.AuditEventRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final ThreadLocal<Boolean> auditStarted =
            ThreadLocal.withInitial(() -> false);

    /**
     * Start audit event
     */
    public AuditEvent start(AuditContext ctx) {

        if (auditStarted.get()) {
            return null; // prevent duplicate
        }
        auditStarted.set(true);
        System.out.println("AUDIT START CALLED: " + System.currentTimeMillis());
        AuditEvent event = AuditEvent.builder()
                .user(getUserReference(ctx.getUserId()))
                .userEmail(ctx.getUserEmail())
                .organization(ctx.getOrganization())
                .workspace(getWorkspaceReference(ctx.getWorkspaceId()))
                .action(ctx.getAction())
                .endpoint(ctx.getEndpoint())
                .startTime(Instant.now())
                .status(AuditStatus.IN_PROGRESS)
                .build();

        return auditEventRepository.save(event);
    }

    /**
     * Mark success
     */
    public void success(AuditEvent event) {
        event.setEndTime(Instant.now());
        event.setStatus(AuditStatus.SUCCESS);
        auditEventRepository.save(event);
        auditStarted.remove();
    }

    /**
     * Mark failure
     */
    public void fail(AuditEvent event) {
        AuditEvent managed = auditEventRepository.findById(event.getId())
                .orElseThrow();

        managed.setEndTime(Instant.now());
        managed.setStatus(AuditStatus.FAILED);

        auditEventRepository.save(managed);
        auditStarted.remove();
    }

    /**
     * Use lazy references (no DB hit)
     */
    private User getUserReference(Long userId) {
        return userId == null ? null : entityManager.getReference(User.class, userId);
    }

    private Workspace getWorkspaceReference(Long workspaceId) {
        return workspaceId == null ? null : entityManager.getReference(Workspace.class, workspaceId);
    }
}