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
 * Referential Item Impacts
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity

@SuperBuilder
@Table(name = "ref_item_impact")
public class ItemImpact implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_item_impact_id_seq")
    @SequenceGenerator(name = "ref_item_impact_id_seq", sequenceName = "ref_item_impact_id_seq", allocationSize = 100)
    private Long id;

    /**
     * Criterion code
     */
    private String criterion;

    /**
     * Lifecycle step code
     */
    private String lifecycleStep;

    /**
     * Name
     */
    private String name;

    /**
     * Category
     */
    private String category;

    /**
     * Average electricity consumption
     */
    private Double avgElectricityConsumption;

    /**
     * Description
     */
    private String description;

    /**
     * Location
     */
    private String location;

    /**
     * Level
     */
    private String level;

    /**
     * Source
     */
    private String source;

    /**
     * Tiers
     */
    private String tier;

    /**
     * Unit
     */
    private String unit;

    /**
     * Value
     */
    private Double value;

    /**
     * Subscriber
     */
    @Column(name = "subscriber")
    private String organization;

    /**
     * Version
     */
    private String version;

    public static String[] getCsvHeaders() {
        return new String[]{"criterion", "lifecycleStep", "name", "category", "avgElectricityConsumption", "description",
                "location", "level", "source", "tier", "unit", "value", "subscriber", "version"};
    }

    public Object[] toCsvRecord() {
        return new Object[]{criterion, lifecycleStep, name, category, avgElectricityConsumption == null ? null : avgElectricityConsumption.toString(), description
                , location, level, source, tier, unit, value == null ? null : value.toString(), organization, version};
    }
}


