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
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "in_application")
public class InApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "in_application_id_seq")
    @SequenceGenerator(name = "in_application_id_seq", sequenceName = "in_application_id_seq", allocationSize = 100)
    private Long id;

    @NotNull
    private String name;

    /**
     * Linked Inventory
     */
    private Long inventoryId;

    /**
     * Linked Digital Service. Temporary field
     */
    private String digitalServiceUid;


    /**
     * Linked Digital Service Version UID
     */
    @Column(name = "digital_service_version_uid")
    private String digitalServiceVersionUid;

    private String physicalEquipmentName;
    private String virtualEquipmentName;

    private String environment;

    private List<String> commonFilters;

    private List<String> filters;

    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;


}
