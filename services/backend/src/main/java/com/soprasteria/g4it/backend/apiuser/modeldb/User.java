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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * G4IT user.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "g4it_user")
public class User extends AbstractBaseEntity implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The email.
     */
    private String email;

    /**
     * The firstName of the user.
     */
    private String firstName;

    /**
     * The lastName of the user.
     */
    private String lastName;

    /**
     * The subject of the user.
     */
    private String sub;

    /**
     * The domain of the user.
     */
    private String domain;

    /**
     * User's organizations.
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserOrganization> userOrganizations;

    /**
     * User's subscribers.
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserSubscriber> userSubscribers;

}
