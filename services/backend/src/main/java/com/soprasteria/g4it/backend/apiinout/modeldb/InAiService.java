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

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "in_aiservices")
public class InAiService {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "in_aiservices_id_seq")
    @SequenceGenerator(name = "in_aiservices_id_seq", sequenceName = "in_aiservices_id_seq", allocationSize = 100)
    private Long id;

    @NotNull
    @Column(name = "service_name")
    private String serviceName;

    /**
     * Linked Inventory
     */
    @NotNull
    @Column(name = "inventory_id")
    private Long inventoryId;

    @NotNull
    @Column(name = "provider")
    private String provider;

    @NotNull
    @Column(name = "model")
    private String model;

    @NotNull
    @Column(name = "output_tokens")
    private Long outputTokens;

    @Column(name = "location")
    private String location;

}
