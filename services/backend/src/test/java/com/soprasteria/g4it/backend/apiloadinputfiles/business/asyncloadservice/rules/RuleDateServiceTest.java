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
    private static final LocalDate TODAY = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);


    @Test
    void testRuleDateOk() {
        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                TODAY, TOMORROW, false);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testRuleDateError() {
        Mockito.when(messageSource.getMessage(any(), any(), any()))
                .thenReturn("Purchase date after retrieval date");

        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                TOMORROW, TODAY, false);

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Purchase date after retrieval date"), actual.get());
    }

    @Test
    void testDatePurchase_InvalidFormatError() {
        Mockito.when(messageSource.getMessage(any(), any(), any()))
                .thenReturn("Invalid purchase date format");

        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                Constants.ERROR_DATE_FORMAT, TODAY, false);

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Invalid purchase date format"), actual.get());
    }

    @Test
    void testDateRetrieval_InvalidFormatError() {
        Mockito.when(messageSource.getMessage(any(), any(), any()))
                .thenReturn("Invalid retrieval date format");

        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                TODAY, Constants.ERROR_DATE_FORMAT, false);

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Invalid retrieval date format"), actual.get());
    }

    @Test
    void testEmptyDatePurchaseOk() {
        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                null, TODAY, false);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testEmptyDateRetrievalOk() {
        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                TODAY, null, false);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testDigitalService_NullPurchaseDate_Error() {
        Mockito.when(messageSource.getMessage(any(), any(), any()))
                .thenReturn("Purchase date must not be blank");

        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                null, TODAY, true);

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Purchase date must not be blank"), actual.get());
    }

    @Test
    void testDigitalService_NullRetrievalDate_Error() {
        Mockito.when(messageSource.getMessage(any(), any(), any()))
                .thenReturn("Retrieval date must not be blank");

        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                TODAY, null, true);

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Retrieval date must not be blank"), actual.get());
    }

    @Test
    void testDigitalService_ValidDates_NoError() {
        var actual = ruleDateService.checkDatesPurchaseRetrieval(locale, filename, line,
                TODAY, TOMORROW, true);
        Assertions.assertTrue(actual.isEmpty());
    }
}
