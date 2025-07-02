/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiaiinfra.modeldb;

import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.InfrastructureType;
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

    private static final String DEVICE_REF_DESKTOP = "desktop-4";

    private static final String DEVICE_REF_LAPTOP = "laptop-3";

    private static final String DEVICE_REF_SERVER = "server-28";


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

    public InfrastructureType getInfrastructureTypeEnum() {

        if (infrastructureType == null || infrastructureType.isEmpty()) {
            return InfrastructureType.SERVER_DC;
        } else if (infrastructureType.equals(DEVICE_REF_DESKTOP)) {
            return InfrastructureType.DESKTOP;
        } else if (infrastructureType.equals(DEVICE_REF_LAPTOP)) {
            return InfrastructureType.LAPTOP;
        } else {
            return InfrastructureType.SERVER_DC;
        }

    }
}
