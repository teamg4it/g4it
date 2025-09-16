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
 * Referential Matching Item
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "ref_matching_item")
public class MatchingItem implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Equipment model source
     */
    private String itemSource;

    /**
     * Ref equipment target
     */
    private String refItemTarget;

    /**
     * Subscriber
     */
    @Column(name = "subscriber")
    private String organization;

    public static String[] getCsvHeaders() {
        return new String[]{"itemSource", "refItemTarget", "subscriber"};
    }

    public Object[] toCsvRecord() {
        return new Object[]{itemSource, refItemTarget, organization};
    }

}