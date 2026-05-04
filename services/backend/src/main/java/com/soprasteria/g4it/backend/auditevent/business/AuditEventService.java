package com.soprasteria.g4it.backend.auditevent.business;

import com.soprasteria.g4it.backend.auditevent.model.AuditContext;
import com.soprasteria.g4it.backend.auditevent.model.AuditStatus;
import com.soprasteria.g4it.backend.auditevent.modeldb.AuditEvent;
import com.soprasteria.g4it.backend.auditevent.repository.AuditEventRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.common.functional.SupplierWithException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.Instant;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public <T> T execute(AuditContext ctx, SupplierWithException<T> action) {

        AuditEvent event = auditEventRepository.save(
                AuditEvent.builder()
                        .user(getUserReference(ctx.getUserId()))
                        .userEmail(ctx.getUserEmail())
                        .organization(ctx.getOrganization())
                        .workspace(getWorkspaceReference(ctx.getWorkspaceId()))
                        .action(ctx.getAction())
                        .endpoint(ctx.getEndpoint())
                        .startTime(Instant.now())
                        .status(AuditStatus.IN_PROGRESS)
                        .build()
        );

        try {
            T result = action.get();

            event.setEndTime(Instant.now());
            event.setStatus(AuditStatus.SUCCESS);
            auditEventRepository.save(event);

            return result;

        } catch (Exception e) {

            event.setEndTime(Instant.now());
            event.setStatus(AuditStatus.FAILED);
            auditEventRepository.save(event);

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e; // ✅ preserve original exception
            }

            throw new RuntimeException(e); // only wrap checked ones
        }
    }

    private User getUserReference(Long userId) {
        return userId == null ? null : entityManager.getReference(User.class, userId);
    }

    private Workspace getWorkspaceReference(Long workspaceId) {
        return workspaceId == null ? null : entityManager.getReference(Workspace.class, workspaceId);
    }
}
