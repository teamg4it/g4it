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
 * Referential Hypothesis
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "ref_hypothesis")
public class Hypothesis implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hypothesis code
     */
    private String code;

    /**
     * Hypothesis value
     */
    private Double value;

    /**
     * Hypothesis description
     */
    private String description;

    /**
     * Hypothesis source
     */
    private String source;

    /**
     * Hypothesis subscriber
     */
    @Column(name = "subscriber")
    private String organization;

    /**
     * Hypothesis version
     */
    private String version;

    public static String[] getCsvHeaders() {
        return new String[]{"code", "value", "description", "source", "subscriber", "version"};
    }

    public Object[] toCsvRecord() {
        return new Object[]{code, value, description, source, organization, version};
    }
}


