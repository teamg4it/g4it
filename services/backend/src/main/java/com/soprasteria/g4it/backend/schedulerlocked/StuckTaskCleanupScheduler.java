/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.schedulerlocked;

import com.soprasteria.g4it.backend.common.task.business.StuckTaskCleanupService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to automatically fail tasks that are stuck in IN_PROGRESS status
 * for longer than the configured timeout period.
 *
 * This prevents zombie tasks from consuming resources and provides
 * clear feedback to users when processes hang indefinitely.
 */
@Component
@Profile("!test")
@Slf4j
public class StuckTaskCleanupScheduler {

    @Autowired
    private StuckTaskCleanupService stuckTaskCleanupService;

    /**
     * Runs every 2 minutes to check for and fail stuck tasks.
     * Uses ShedLock to ensure only one instance runs in a distributed environment.
     */
    @Scheduled(fixedDelay = 120_000, initialDelay = 60_000)
    @SchedulerLock(
            name = "failStuckTasks",
            lockAtMostFor = "2m",
            lockAtLeastFor = "10s"
    )
    public void failStuckTasks() {
        log.info("Running stuck task cleanup scheduler");
        stuckTaskCleanupService.failStuckTasks();
    }

}