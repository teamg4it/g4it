/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.LoadMetadataService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AsyncLoadMetadataService {

    @Autowired
    private LoadMetadataService loadMetadataService;

    /**
     * Load the inventory metadata
     * @param context : the inventory file loading context
     */
    public void loadInventoryMetadata(Context context) {
        log.info("Load inventory metadata {}", context.log());

        try(ExecutorService executorService = Executors.newFixedThreadPool(4)){
            for (FileToLoad fileToLoad :  context.getFilesToLoad()) {
                executorService.submit(() -> {
                    try{
                        log.info("Load inventory metadata for file {} {}",fileToLoad.getFilename(), context.log());
                        loadMetadataService.loadMetadataFile(fileToLoad, context);
                    }
                    catch (Exception e){
                        log.error("Error loading metadata file {}", fileToLoad.getFilename(), e);
                    }

                });
            }

            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                log.debug("All metadata files have been loaded");
            }
        }

    }
}
