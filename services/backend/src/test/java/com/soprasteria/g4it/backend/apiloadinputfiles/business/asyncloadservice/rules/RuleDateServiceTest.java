/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
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
    private final Locale locale = Locale.getDefault();
    private final String filename = "filename";
    private final int line = 1;

    @Test
    void testRuleDateOk() {
        var actual = ruleDateService.checkDatesPurcaseRetrieval(locale, filename, line, LocalDate.now(), LocalDate.now().plusDays(1));
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testRuleDateError() {
        Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("Purchase date after retrieval date");
        var actual = ruleDateService.checkDatesPurcaseRetrieval(locale,filename, line, LocalDate.now().plusDays(1), LocalDate.now());
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,line, "Purchase date after retrieval date"), actual.get());
    }

    @Test
    void testDatePurchase_InvalidFormatError() {
        Mockito.when(messageSource.getMessage(
                any(), any(), any()))
                .thenReturn("Invalid purchase date format");

        var actual= ruleDateService.checkDatesPurcaseRetrieval(
                locale, filename, line, Constants.ERROR_DATE_FORMAT, LocalDate.now());

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,line, "Invalid purchase date format"), actual.get());

    }
    @Test
    void testDateRetrieval_InvalidFormatError() {
        Mockito.when(messageSource.getMessage(
                        any(), any(), any()))
                .thenReturn("Invalid retrieval date format");

       var actual = ruleDateService.checkDatesPurcaseRetrieval(
                locale, filename, line, LocalDate.now(), Constants.ERROR_DATE_FORMAT);

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,line, "Invalid retrieval date format"), actual.get());
    }

    @Test
    void testEmptyDatePurchaseOk() {
       var actual = ruleDateService.checkDatesPurcaseRetrieval(
                locale, filename, line, null, LocalDate.now());

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testEmptyDateRetrievalOk() {
       var actual = ruleDateService.checkDatesPurcaseRetrieval(
                locale, filename, line, LocalDate.now(), null);

        Assertions.assertTrue(actual.isEmpty());
    }
}
