/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * The G4IT client organization (formerly subscriber).
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "g4it_organization")
public class Organization extends AbstractBaseEntity implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The Organization's workspaces.
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "organization")
    private List<Workspace> workspaces;

    /**
     * The authorized domains of organizations
     */
    private String authorizedDomains;

    /**
     * The Organization name.
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
     * The Criteria key
     */
    private List<String> criteria;

    /**
     * Is EcomindAi enabled or not for this Organization
     */
    private boolean ecomindai;
}
