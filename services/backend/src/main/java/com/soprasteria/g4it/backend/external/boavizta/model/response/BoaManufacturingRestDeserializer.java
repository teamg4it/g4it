/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.boavizta.model.response;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for {@link BoaManufacturingRest}.
 * <p>
 * BoaviztAPI sometimes returns the literal string "not implemented" (instead of
 * an object with value/min/max) for the "embedded" phase impact of a criterion
 * that is not yet supported for the requested provider/instance. This
 * deserializer treats any string value as "not available" and maps it to null
 * instead of failing the whole response deserialization.
 */
public class BoaManufacturingRestDeserializer extends ValueDeserializer<BoaManufacturingRest> {

    @Override
    public BoaManufacturingRest deserialize(final JsonParser p, final DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            // e.g. "not implemented" -> BoaviztAPI has no value for this phase/criterion
            return null;
        }
        return ctxt.readValue(p, BoaManufacturingRest.class);
    }
}

