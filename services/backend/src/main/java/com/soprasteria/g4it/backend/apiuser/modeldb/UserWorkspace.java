/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.modeldb;

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
 * G4IT User Organization mapping.
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "g4it_user_organization")
public class UserWorkspace implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The organization.
     */
    @NotNull
    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Workspace workspace;

    /**
     * The user.
     */
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /**
     * Flag 'Default'.
     */
    private Boolean defaultFlag;

    /**
     * User role on organization.
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "g4it_user_role_organization",
            joinColumns = @JoinColumn(name = "user_organization_id",
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id",
                    referencedColumnName = "id"))
    private List<Role> roles;


    @ToString.Exclude
    @OneToMany(mappedBy = "userWorkspaces", fetch = FetchType.LAZY)
    private List<UserRoleWorkspace> userRoleWorkspace;

}
