/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.apiindicator.mapper.DigitalServiceIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceIndicatorRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigitalServiceIndicatorViewServiceTest {

    @Mock
    private DigitalServiceIndicatorRepository digitalServiceIndicatorRepository;

    @Mock
    private DigitalServiceIndicatorMapper digitalServiceIndicatorMapper;

    @InjectMocks
    private DigitalServiceIndicatorViewService digitalServiceIndicatorViewService;

    @Test
    void shouldReturnEmptyListWhenNoIndicatorsFound() {
        final String uid = "nonexistent-uid";

        when(digitalServiceIndicatorRepository.findDigitalServiceIndicators(uid)).thenReturn(List.of());

        Assertions.assertThat(digitalServiceIndicatorViewService.getDigitalServiceIndicators(uid)).isEmpty();
    }


    @Test
    void shouldHandleMultipleIndicators() {
        final String uid = "uid";

        when(digitalServiceIndicatorViewService.getDigitalServiceIndicators(uid))
                .thenReturn(List.of(DigitalServiceIndicatorBO.builder().build(), DigitalServiceIndicatorBO.builder().build()));

        Assertions.assertThat(digitalServiceIndicatorViewService.getDigitalServiceIndicators(uid)).hasSize(2);
        
    }
}
