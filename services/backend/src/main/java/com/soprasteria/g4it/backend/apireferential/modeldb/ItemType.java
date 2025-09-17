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
 * Referential ItemType
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "ref_item_type")
public class ItemType implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Item type
     */
    private String type;

    /**
     * Category
     */
    private String category;

    /**
     * Comment
     */
    private String comment;

    /**
     * Default lifespan
     */
    private Double defaultLifespan;

    /**
     * Is a server
     */
    private Boolean isServer;

    /**
     * Source
     */
    private String source;

    /**
     * Ref default item
     */
    private String refDefaultItem;

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
        return new String[]{"type", "category", "comment", "defaultLifespan", "isServer",
                "source", "refDefaultItem", "subscriber", "version"};
    }

    public Object[] toCsvRecord() {
        return new Object[]{type, category, comment, defaultLifespan, isServer,
                source, refDefaultItem, organization, version};
    }

}


