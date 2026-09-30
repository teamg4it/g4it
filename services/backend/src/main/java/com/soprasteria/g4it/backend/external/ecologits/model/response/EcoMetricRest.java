
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
 * A single environmental metric returned by EcoLogits (energy, gwp, adpe, pe, wcf...).
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EcoMetricRest {

    private String type;

    private String name;

    private EcoValueRangeRest value;

    private String unit;

}