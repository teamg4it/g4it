/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Global, application-wide XSS guard applied to every {@code @RequestBody} payload,
 * regardless of the target DTO shape (plain bean, Lombok builder, record, nested
 * objects, arrays, or generic maps).
 * <p>
 * The raw JSON body is parsed into a {@link JsonNode} tree and every textual value is
 * recursively validated against the {@link XssValidator} safelist BEFORE Spring/Jackson
 * binds it onto the target model. This acts as a second, independent layer of defense
 * on top of {@link XssStringDeserializer} (registered via {@link JacksonXssConfig}),
 * which only protects {@code String}-typed bean properties resolved through the
 * standard Jackson bean deserialization path.
 */
@ControllerAdvice
public class XssRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(final MethodParameter methodParameter, final Type targetType,
                             final Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to every @RequestBody across the whole application
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(final HttpInputMessage inputMessage, final MethodParameter parameter,
                                            final Type targetType,
                                            final Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        final byte[] body = inputMessage.getBody().readAllBytes();
        final String raw = new String(body, StandardCharsets.UTF_8);

        if (!raw.isBlank()) {
            final JsonNode root = objectMapper.readTree(raw);
            validateNode(root);
        }

        final InputStream replayableBody = new ByteArrayInputStream(body);
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return replayableBody;
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    private void validateNode(final JsonNode node) {
        if (node.isTextual()) {
            XssValidator.validate(node.asText());
        } else if (node.isObject()) {
            final Iterator<JsonNode> values = node.elements();
            while (values.hasNext()) {
                validateNode(values.next());
            }
        } else if (node.isArray()) {
            for (final JsonNode child : node) {
                validateNode(child);
            }
        }
    }
}



