/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One node of the graph tree at the currently requested {@link GraphLevel}.
 * <p>
 * At {@code GLOBAL} level exactly one instance exists (with {@code label == null}
 * and no hierarchy count/attribute fields populated). From {@code DOMAIN} onward,
 * one instance exists per distinct value of the level, each carrying only the
 * level-specific extra fields it needs (see solution.md §4.3 table):
 * <ul>
 *     <li>{@code DOMAIN}: subDomainCount + applicationCount</li>
 *     <li>{@code SUB_DOMAIN}: applicationCount</li>
 *     <li>{@code APPLICATION}: none</li>
 *     <li>{@code VIRTUAL_EQUIPMENT}: cluster + equipmentType + environment</li>
 * </ul>
 * {@code consistentCount}/{@code inconsistentCount} are always populated at every level.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class ApplicationNodeBO {

    private String label;

    private Double impact;

    private Double sip;

    private Long countValue;

    private Long consistentCount;

    private Long inconsistentCount;

    /**
     * Populated only when graphLevel = DOMAIN.
     */
    private Long subDomainCount;

    /**
     * Populated only when graphLevel = DOMAIN or SUB_DOMAIN.
     */
    private Long applicationCount;

    /**
     * Populated only when graphLevel = VIRTUAL_EQUIPMENT.
     */
    private String cluster;

    /**
     * Populated only when graphLevel = VIRTUAL_EQUIPMENT.
     */
    private String equipmentType;

    /**
     * Populated only when graphLevel = VIRTUAL_EQUIPMENT.
     */
    private String environment;

    /**
     * Breakdown of this node by the requested repartition axis.
     */
    private List<ApplicationRepartitionImpactBO> repartitions;
}

