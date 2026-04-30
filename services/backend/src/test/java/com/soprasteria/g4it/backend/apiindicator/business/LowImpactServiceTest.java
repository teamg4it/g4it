/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LowImpactServiceTest {

    @InjectMocks
    LowImpactService lowImpactService;

    @Mock
    ReferentialGetService referentialGetService;
    @Mock
    ReferentialService referentialService;


    @Test
    void shouldGetQuartileIndexForInventory() {

        Map<Pair<String, String>, Integer> quartileMap = Map.of(
                Pair.of("France", "CLIMATE_CHANGE"), 1,
                Pair.of("France", "ACIDIFICATION"), 1,
                Pair.of("Germany", "CLIMATE_CHANGE"), 4,
                Pair.of("Germany", "ACIDIFICATION"), 4
        );

        ReflectionTestUtils.setField(lowImpactService, "criterias", Set.of("Climate change", "Acidification"));

        Mockito.when(referentialGetService.getCountries(null)).thenReturn(List.of("France", "Germany"));
        Mockito.when(referentialService.getElectricityMixQuartiles(1L)).thenReturn(quartileMap);

        assertThat(lowImpactService.isLowImpact("France",1L)).isTrue();
        assertThat(lowImpactService.isLowImpact("Germany",1L)).isFalse();
    }
}
