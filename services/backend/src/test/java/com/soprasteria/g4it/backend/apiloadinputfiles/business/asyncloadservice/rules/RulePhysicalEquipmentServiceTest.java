/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.common.model.LineError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RulePhysicalEquipmentServiceTest {

    @InjectMocks
    RulePhysicalEquipmentService ruleService;
    @Mock
    DigitalServiceReferentialService digitalServiceRefService;

    @Mock
    MessageSource messageSource;
    private final Locale locale = Locale.getDefault();
    private final String filename = "filename";
    private final int line = 1;

    @Test
    void testCheckElectricityConsumption_NullValue_ReturnsError() {

        Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("Field 'consoElecAn' is mandatory.");
        var actual = ruleService.checkElectricityConsumption(locale, filename, line, null);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Field 'consoElecAn' is mandatory."), actual.get());

    }

    @Test
    void testCheckElectricityConsumption_ValidValue_ReturnsEmpty() {
        var actual = ruleService.checkElectricityConsumption(locale, "file.csv", line, 100.0);
        assertTrue(actual.isEmpty());
    }

    @Test
    void testCheckDurationHour_Null_ReturnsBlankError() {
        Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("Field 'dureeUtilisation' is mandatory.");
        var actual = ruleService.checkDurationHour(locale, filename, line, null);
        assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Field 'dureeUtilisation' is mandatory."), actual.get());
    }

    @Test
    void testCheckDurationHour_Negative_ReturnsBlankError() {
        Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("Field 'dureeUtilisation' is mandatory.");
        var actual = ruleService.checkDurationHour(locale, filename, line, -5.0);
        assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Field 'dureeUtilisation' is mandatory."), actual.get());
    }

    @Test
    void testCheckDurationHour_TooHigh_ReturnsInvalidError() {
        Mockito.when(messageSource.getMessage(eq("durationHour.invalid"), any(), any())).thenReturn("dureeUtilisation should not be more than 8760");
        var actual = ruleService.checkDurationHour(locale, filename, line, 9999.0);
        assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "dureeUtilisation should not be more than 8760"), actual.get());
    }

    @Test
    void testCheckDurationHour_Valid_ReturnsEmpty() {
        assertTrue(ruleService.checkDurationHour(locale, filename, line, 100.0).isEmpty());
    }
    
}
