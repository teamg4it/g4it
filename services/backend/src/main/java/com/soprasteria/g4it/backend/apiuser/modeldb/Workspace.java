/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.modeldb;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * G4IT Organization.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "g4it_organization")
public class Workspace extends AbstractBaseEntity implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The organization's name.
     */
    @NotNull
    private String name;

    /**
     * The storage retention day for export folder
     */
    private Integer storageRetentionDayExport;

    /**
     * The storage retention day for output folder
     */
    private Integer storageRetentionDayOutput;

    /**
     * The data retention day
     */
    private Integer dataRetentionDay;

    /**
     * The deletion Date
     */
    private LocalDateTime deletionDate;

    /**
     * The status
     */
    private String status;

    /**
     * The Criteria key for inventory.
     */
    private List<String> criteriaIs;

    /**
     * Criteria key for digital service.
     */
    private List<String> criteriaDs;

    /**
     * The last user who updated the note.
     */
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "last_updated_by", referencedColumnName = "id")
    private User lastUpdatedBy;

    /**
     * The  user who created the organization.
     */
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    /**
     * The organization's subscriber.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;

    /**
     * Identifier of the inventory related to the export request
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "workspace")
    private List<Inventory> inventory;

    /**
     * Boolean isMigrated: true if organization name has been migrated to organization id
     */
    private Boolean isMigrated = true;

}
