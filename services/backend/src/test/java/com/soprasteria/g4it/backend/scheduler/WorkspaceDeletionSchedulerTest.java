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

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceDeletionSchedulerTest {

    @Mock
    private WorkspaceDeletionService workspaceDeletionService;

    @InjectMocks
    private WorkspaceDeletionScheduler scheduler;

    @Test
    void initShouldExecuteDeletionWhenOnInitTrue() throws Exception {
        setOnInit(true);

        scheduler.init();

        verify(workspaceDeletionService).executeDeletion();
    }

    @Test
    void initShouldNotExecuteDeletionWhenOnInitFalse() throws Exception {
        setOnInit(false);

        scheduler.init();

        verifyNoInteractions(workspaceDeletionService);
    }

    @Test
    void executeAutoDeletionShouldExecuteDeletion() {
        scheduler.executeAutoDeletion();

        verify(workspaceDeletionService).executeDeletion();
    }

    private void setOnInit(boolean value) throws Exception {
        Field field = WorkspaceDeletionScheduler.class.getDeclaredField("onInit");
        field.setAccessible(true);
        field.set(scheduler, value);
    }
}