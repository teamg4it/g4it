/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceLinkDeletionSchedulerTest {

    @Mock
    private DigitalServiceLinkRepository digitalServiceLinkRepository;

    @InjectMocks
    private DigitalServiceLinkDeletionScheduler scheduler;

    @BeforeEach
    void setup() {
        // Reset 'onInit' to default false before each test
        ReflectionTestUtils.setField(scheduler, "onInit", false);
    }

    @Test
    void init_onInitTrue_callsDeleteExpiredLinks() {
        ReflectionTestUtils.setField(scheduler, "onInit", true);
        scheduler.init();

        verify(digitalServiceLinkRepository, times(1)).deleteExpiredLinks();
    }

    @Test
    void init_onInitFalse_doesNotCallDeleteExpiredLinks() {
        ReflectionTestUtils.setField(scheduler, "onInit", false);
        scheduler.init();

        verify(digitalServiceLinkRepository, never()).deleteExpiredLinks();
    }

}
