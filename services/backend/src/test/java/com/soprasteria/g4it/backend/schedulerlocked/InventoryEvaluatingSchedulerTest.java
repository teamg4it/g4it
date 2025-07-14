/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.schedulerlocked;

import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryEvaluatingSchedulerTest {

    @Mock
    private EvaluatingService evaluatingService;

    @InjectMocks
    private InventoryEvaluatingScheduler inventoryEvaluatingScheduler;

    @Test
    void restartLostEvaluating_shouldInvokeEvaluatingService() {
        inventoryEvaluatingScheduler.restartLostEvaluating();
        verify(evaluatingService, times(1)).restartEvaluating();
    }

}