/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Device Type Referential.
 */
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "ref_device_type")
public class DeviceTypeRef {

    /**
     * To prevent update.
     */
    @PreUpdate
    private void preUpdate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Auto Generated ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Device's description.
     */
    private String description;

    /**
     * NumEcoEval Reference.
     */
    private String reference;

    /**
     * External description.
     */
    private String externalReferentialDescription;

    /**
     * Device's lifespan.
     */
    private Double lifespan;

    /**
     * Source.
     */
    private String source;
}
