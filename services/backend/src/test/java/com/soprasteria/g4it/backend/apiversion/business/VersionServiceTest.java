/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiversion.business;


import com.soprasteria.g4it.backend.server.gen.api.dto.VersionRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @InjectMocks
    private VersionService versionService;

    @Test
    void getVersionReturnsCorrectVersionDetails() {
        ReflectionTestUtils.setField(versionService, "version", "1.0.0");
        ReflectionTestUtils.setField(versionService, "boaviztaVersion", "2.0.0");
        VersionRest result = versionService.getVersion();

        assertNotNull(result);
        assertEquals("1.0.0", result.getG4it());
        assertEquals("2.0.0", result.getBoaviztapi());
    }

}