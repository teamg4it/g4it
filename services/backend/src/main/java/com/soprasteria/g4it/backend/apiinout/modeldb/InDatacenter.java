package com.soprasteria.g4it.backend.apiinout.modeldb;/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "in_datacenter")
public class InDatacenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    /**
     * Linked Inventory
     */
    private Long inventoryId;

    /**
     * Deprecated: Linked Digital Service. Temporary field
     */
    private String digitalServiceUid;

    /**
     * Linked Digital Service Version UID
     */
    private String digitalServiceVersionUid;

    private String fullName;

    @NotNull
    private String location;

    @NotNull
    @Min(0)
    private Double pue;

    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;
}
