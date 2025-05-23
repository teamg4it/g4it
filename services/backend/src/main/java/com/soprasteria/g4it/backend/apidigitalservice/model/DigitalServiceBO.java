/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.model;

import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.common.model.NoteBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Digital Service Business Object.
 */
@Data
@SuperBuilder
@AllArgsConstructor
public class DigitalServiceBO {

    /**
     * Unique identifier.
     */
    private String uid;

    /**
     * Name.
     */
    private String name;

    /**
     * Name of user  who created the digital service.
     */
    private String userName;

    /**
     * The Criterias key.
     */
    private List<String> criteria;

    /**
     * Creation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * Last update date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;

    /**
     * Last calculation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastCalculationDate;

    /**
     * Note
     */
    private NoteBO note;

}
