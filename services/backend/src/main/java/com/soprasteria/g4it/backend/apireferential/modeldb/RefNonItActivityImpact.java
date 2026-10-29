/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.modeldb;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Referential Non-IT Activity Impacts.
 * Holds the impact factor per unit of quantity for a given Non-IT activity reference and criterion.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "ref_nonitactivity_impact")
public class RefNonItActivityImpact implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_nonitactivity_impact_id_seq")
    @SequenceGenerator(name = "ref_nonitactivity_impact_id_seq", sequenceName = "ref_nonitactivity_impact_id_seq", allocationSize = 100)
    private Long id;

    /**
     * Non-IT activity reference code
     */
    private String reference;

    /**
     * Criterion code
     */
    private String criterion;

    /**
     * Category: Buildings, IT Pro Services, Travels
     */
    private String category;

    /**
     * Subcategory, used for Travels transport mode (Car, Train, Plane)
     */
    private String subcategory;

    /**
     * Impact factor per unit of quantity
     */
    private Double value;

    /**
     * Output unit (e.g. kg CO2e)
     */
    private String unit;

    /**
     * Source of the data
     */
    private String source;

    /**
     * Version of the referential
     */
    private String version;

    @Column(name = "workspace_id")
    private Long workspaceId;
}
