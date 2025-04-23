/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class RuleDateServiceTest {

    @InjectMocks
    RuleDateService ruleDateService;

    @Mock
    MessageSource messageSource;

    @Test
    void testRuleDateOk() {
        var actual = ruleDateService.checkDatesPurcaseRetrieval(Locale.getDefault(), "filename",1, LocalDate.now(), LocalDate.now().plusDays(1));
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testRuleDateError() {
        Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("error test");
        var actual = ruleDateService.checkDatesPurcaseRetrieval(Locale.getDefault(),"filename", 1, LocalDate.now().plusDays(1), LocalDate.now());
        Assertions.assertEquals(new LineError("filename",1, "error test"), actual.get());
    }
}
