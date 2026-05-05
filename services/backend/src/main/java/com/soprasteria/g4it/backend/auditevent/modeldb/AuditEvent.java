/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.auditevent.modeldb;

import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.auditevent.model.AuditEventType;
import com.soprasteria.g4it.backend.auditevent.model.AuditStatus;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "audit_event")
public class AuditEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who triggered the event
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String userEmail;

    /**
     * Organization name (denormalized for audit safety)
     */
    private String organization;

    /**
     * Workspace context
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", referencedColumnName = "id")
    private Workspace workspace;

    /**
     * Combined action enum (IMPORT_WORKSPACE_REFERENTIAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEventType action;

    /**
     * Endpoint or method name
     */
    private String endpoint;

    /**
     * Execution timestamps
     */
    private Instant startTime;

    private Instant endTime;

    /**
     * Status (SUCCESS / FAILED)
     */
    @Enumerated(EnumType.STRING)
    private AuditStatus status;


}
