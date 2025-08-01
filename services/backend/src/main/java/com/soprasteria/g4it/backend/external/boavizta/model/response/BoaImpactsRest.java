/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.boavizta.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BoaImpactsRest {
    private BoaImpactRest gwp;
    private BoaImpactRest adpe;
    private BoaImpactRest ir;
    private BoaImpactRest ap;
    private BoaImpactRest pm;
    private BoaImpactRest odp;
    private BoaImpactRest pocp;
    private BoaImpactRest ept;
    private BoaImpactRest epf;
    private BoaImpactRest epm;
    private BoaImpactRest adpf;
}
