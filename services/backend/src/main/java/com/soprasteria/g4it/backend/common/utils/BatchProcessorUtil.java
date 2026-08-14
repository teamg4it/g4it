/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class BatchProcessorUtil {
    public <T> void processInBatches(
            Function<Pageable, Slice<T>> fetchFunction,
            int batchSize,
            Consumer<List<T>> consumer) {

        Pageable pageable = PageRequest.of(0, batchSize);
        Slice<T> slice;

        do {
            slice = fetchFunction.apply(pageable);

            if (!slice.isEmpty()) {
                consumer.accept(slice.getContent());
            }

            pageable = slice.nextPageable();

        } while (slice.hasNext());
    }
}
