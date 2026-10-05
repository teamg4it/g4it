/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalFileDeletionSchedulerTest {

    @Mock
    private LocalFileDeletionService localFileDeletionService;

    @InjectMocks
    private LocalFileDeletionScheduler scheduler;

    @Test
    void executeAutoDeletionShouldCallService() {
        scheduler.executeAutoDeletion();

        verify(localFileDeletionService).executeDeletion();
    }
}
