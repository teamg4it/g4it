/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class AsyncLoadMetadataService {

    @Autowired
    private LoadMetadataService loadMetadataService;

    @Autowired
    @Qualifier("taskExecutorMetadataLoading")
    private TaskExecutor metadataTaskExecutor;

    /**
     * Load the inventory metadata
     * @param context : the inventory file loading context
     */
    public void loadInputMetadata(Context context) {
        log.info("Load input metadata {}", context.log());

        List<CompletableFuture<Void>> metadataLoadingFutures = context.getFilesToLoad().stream()
                .map(fileToLoad -> CompletableFuture.runAsync(() -> {
                    try {
                        log.info("Load input metadata for file {} {}", fileToLoad.getFilename(), context.log());
                        loadMetadataService.loadMetadataFile(fileToLoad, context);
                    } catch (AsyncTaskException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error("Error loading metadata file {}", fileToLoad.getFilename(), e);
                    }
                }, metadataTaskExecutor))
                .toList();

        CompletableFuture<Void> allMetadataLoads = CompletableFuture.allOf(
                metadataLoadingFutures.toArray(new CompletableFuture[0])
        );

        try {
            allMetadataLoads.get(60, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AsyncTaskException(String.format("%s - Metadata loading interrupted", context.log()), e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof AsyncTaskException asyncTaskException) {
                throw asyncTaskException;
            }
            throw new AsyncTaskException(String.format("%s - Error during metadata loading", context.log()), cause);
        } catch (TimeoutException e) {
            metadataLoadingFutures.forEach(future -> future.cancel(true));
            throw new AsyncTaskException(String.format("%s - Metadata loading timeout", context.log()), e);
        } finally {
            log.debug("All metadata files have been loaded");
        }

    }
}
