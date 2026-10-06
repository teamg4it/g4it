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
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Min/max approximation interval used in every EcoLogits metric.
 * <p>
 * EcoLogits may return this value either as an object ({"min": x, "max": y})
 * or as a single scalar number when min and max are equal, hence the custom deserializer.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = EcoValueRangeRestDeserializer.class)
public class EcoValueRangeRest {

    private Double min;

    private Double max;

    public EcoValueRangeRest(final Double min, final Double max) {
        this.min = min;
        this.max = max;
    }

    public Double getMean() {
        if (min == null || max == null) {
            return null;
        }
        return (min + max) / 2d;
    }
}