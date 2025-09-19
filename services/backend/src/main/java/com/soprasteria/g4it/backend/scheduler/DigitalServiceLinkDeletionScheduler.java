/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@Slf4j
public class DigitalServiceLinkDeletionScheduler {

    @Value("${g4it.digitalServiceLink.deletion.oninit:false}")
    private boolean onInit;

    @Autowired
    private DigitalServiceLinkRepository digitalServiceLinkRepository;

    @PostConstruct
    public void init() {
        if (onInit) {
            digitalServiceLinkRepository.deleteExpiredLinks();
        }
    }
    /**
     * Execute the deletion of expired digital services links
     */
   @Scheduled(cron = "${g4it.digitalServiceLink.deletion.cron}")
    private void executeDeletion() {
        int nbExpiredLinks = digitalServiceLinkRepository.deleteExpiredLinks();
        log.info("Deleting {} expired digital service shared links.", nbExpiredLinks);
    }
}
