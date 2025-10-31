/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Digital Service Shared Link Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "digital_service_shared_link")
public class DigitalServiceSharedLink {

    /**
     * Primary Key : auto generated ID.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    /**
     * The user behind the digital service.
     */
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;
    /**
     * Auto creation date.
     */
    private LocalDateTime creationDate;

    /**
     * The shared digital service's uid.
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "digital_service_uid", referencedColumnName = "uid")
    private DigitalService digitalService;

    /**
     * Linked Digital Service Version UID
     */
    @Column(name = "digital_service_version_uid")
    private String digitalServiceVersionUid;

    /**
     * Expiry date.
     */
    private LocalDateTime expiryDate;

    /**
     * Specifies whether link is active or has expired
     */
    private boolean isActive;

}
