/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.modeldb;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * G4IT role.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "g4it_role")
public class Role implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The role's name.
     */
    private String name;

    @ToString.Exclude
    @OneToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<UserRoleWorkspace> userRoleWorkspace;
}
