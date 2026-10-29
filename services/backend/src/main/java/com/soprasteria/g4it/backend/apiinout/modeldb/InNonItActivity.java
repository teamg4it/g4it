package com.soprasteria.g4it.backend.apiinout.modeldb;/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import jakarta.persistence.*;
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
@Table(name = "in_nonitactivities")
public class InNonItActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "in_nonitactivities_id_seq")
    @SequenceGenerator(name = "in_nonitactivities_id_seq", sequenceName = "in_nonitactivities_id_seq", allocationSize = 100)
    private Long id;

    @NotNull
    @Column(name = "activity_name")
    private String activityName;

    /**
     * Linked Inventory
     */
    @NotNull
    @Column(name = "inventory_id")
    private Long inventoryId;

    @NotNull
    @Column(name = "quantity")
    private Long quantity;

    @NotNull
    @Column(name = "reference")
    private String reference;

    @Column(name = "entity")
    private String entity;

    @Column(name = "source")
    private String source;

    @EqualsAndHashCode.Exclude
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @EqualsAndHashCode.Exclude
    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

}
