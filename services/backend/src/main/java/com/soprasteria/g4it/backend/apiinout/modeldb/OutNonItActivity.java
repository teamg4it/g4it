/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.modeldb;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "out_nonitactivities")
public class OutNonItActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "out_nonitactivities_id_seq")
    @SequenceGenerator(name = "out_nonitactivities_id_seq", sequenceName = "out_nonitactivities_id_seq", allocationSize = 100)
    private Long id;

    private String name;

    private Long taskId;

    private String criterion;

    private String category;

    private String subcategory;

    private String reference;

    private String statusIndicator;

    @Min(0)
    private Long quantity;

    @Min(0)
    private Double unitImpact;

    @Min(0)
    private Double peopleEqImpact;

    private String unit;

    private List<String> commonFilters;

    private Set<String> errors;

    private String source;

    @EqualsAndHashCode.Exclude
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @EqualsAndHashCode.Exclude
    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}
