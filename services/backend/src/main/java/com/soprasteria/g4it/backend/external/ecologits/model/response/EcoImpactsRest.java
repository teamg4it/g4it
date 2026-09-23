/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.ecologits.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Impacts breakdown of an EcoLogits estimation: total impacts plus usage (electricity use) and
 * embodied (manufacturing) contributions.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EcoImpactsRest {

    private EcoMetricRest energy;

    private EcoMetricRest gwp;

    private EcoMetricRest adpe;

    private EcoMetricRest pe;

    private EcoMetricRest wcf;

}