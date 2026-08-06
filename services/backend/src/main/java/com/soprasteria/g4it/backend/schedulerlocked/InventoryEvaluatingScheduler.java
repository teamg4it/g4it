/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.schedulerlocked;

import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@Slf4j
public class InventoryEvaluatingScheduler {

    @Autowired
    private EvaluatingService evaluatingService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 5_000)
    @SchedulerLock(name = "restartLostEvaluating", lockAtMostFor = "2m", lockAtLeastFor = "9s")
    public void restartLostEvaluating() {
        evaluatingService.restartEvaluating();
    }

}
