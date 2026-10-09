/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.ecologits.model.response;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for {@link EcoValueRangeRest} that supports both the regular
 * object representation ({"min": x, "max": y}) and a scalar number representation,
 * which EcoLogits returns when min and max values are equal.
 */
public class EcoValueRangeRestDeserializer extends ValueDeserializer<EcoValueRangeRest> {

    @Override
    public EcoValueRangeRest deserialize(final JsonParser parser, final DeserializationContext context) {
        final JsonNode node = context.readTree(parser);

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isNumber()) {
            final Double value = node.doubleValue();
            return new EcoValueRangeRest(value, value);
        }

        final Double min = node.hasNonNull("min") ? node.get("min").doubleValue() : null;
        final Double max = node.hasNonNull("max") ? node.get("max").doubleValue() : null;
        return new EcoValueRangeRest(min, max);
    }
}
