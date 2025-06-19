/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiaiinfra.modeldb;

import com.soprasteria.g4it.backend.server.gen.api.dto.InAiInfrastructureRest;
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
     * Linked Digital Service. Temporary field
     */
    private String digitalServiceUid;

    private Double complementaryPue;

    @Enumerated(EnumType.STRING)
    @Column(name = "infrastructure_type_enum")
    private InAiInfrastructureRest.InfrastructureTypeEnum infrastructureTypeEnum;

    private Long nbGpu;

    private Long gpuMemory;
}
