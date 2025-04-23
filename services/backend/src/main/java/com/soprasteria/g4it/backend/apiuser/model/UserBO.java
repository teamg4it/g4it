/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Business Object.
 */
@Data
@SuperBuilder
public class UserBO {

    @EqualsAndHashCode.Exclude
    long id;

    /**
     * The email.
     */
    private String email;

    /**
     * The firstName of the user.
     */
    @EqualsAndHashCode.Exclude
    private String firstName;

    /**
     * The lastName of the user.
     */
    @EqualsAndHashCode.Exclude
    private String lastName;

    /**
     * Is the user a super admin
     */
    private Boolean isSuperAdmin;
    /**
     * The subject of the user.
     */
    @EqualsAndHashCode.Exclude
    private String sub;

    /**
     * The domain of the user.
     */
    @EqualsAndHashCode.Exclude
    private String domain;

    /**
     * User's subscriber.
     */
    @EqualsAndHashCode.Exclude
    private List<SubscriberBO> subscribers;

    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * The admin mode to show deleted organizations
     */
    private boolean adminMode = false;

}
