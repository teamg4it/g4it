// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.config;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

@Configuration
public class JsonNullableConfig {

    @Bean
    public SimpleModule jsonNullableModule() {
        return new SimpleModule().addSerializer(new JsonNullableSerializer());
    }

    // The OpenAPI nullable library supplies a Jackson 2 module, not a Jackson 3 serializer.
    private static class JsonNullableSerializer extends StdSerializer<JsonNullable<?>> {
        JsonNullableSerializer() {
            super(JsonNullable.class, false);
        }

        @Override
        public void serialize(JsonNullable<?> value, JsonGenerator generator, SerializationContext context) {
            context.writeValue(generator, value.orElse(null));
        }
    }
}
