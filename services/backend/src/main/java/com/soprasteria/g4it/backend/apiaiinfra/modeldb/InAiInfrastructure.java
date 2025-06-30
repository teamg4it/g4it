/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiaiinfra.modeldb;

import jakarta.persistence.*;
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
@Table(name = "in_ai_infrastructure")
public class InAiInfrastructure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Digital Service uid
     */
    private String digitalServiceUid;

    /**
     * Complementary Pue
     */
    private Double complementaryPue;

    /**
     * Infrastructure type
     */
    private String infrastructureType;

    /**
     * nbGpu
     */
    private Long nbGpu;

    /**
     * gpuMemory
     */
    private Long gpuMemory;
}
