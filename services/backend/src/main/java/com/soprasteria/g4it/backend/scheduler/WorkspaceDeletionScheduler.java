/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class WorkspaceDeletionScheduler {

    @Value("${g4it.organization.deletion.oninit:false}")
    private boolean onInit;

    @Autowired
    private WorkspaceDeletionService workspaceDeletionService;

    @PostConstruct
    public void init() {
        if (onInit) {
            workspaceDeletionService.executeDeletion();
        }
    }

    /**
     * Execute the data deletion with a cron scheduler
     */
    @Scheduled(cron = "${g4it.organization.deletion.cron}")
    public void executeAutoDeletion() {
        workspaceDeletionService.executeDeletion();
    }

}
