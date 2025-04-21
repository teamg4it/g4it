/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.ItemTypeRest;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenericRuleServiceTest {
    @Mock
    private MessageSource messageSource;
    @Mock
    private Validator validator;

    @Mock
    private ReferentialGetService referentialGetService;

    @InjectMocks
    private GenericRuleService genericRuleService;

    private final Locale locale = Locale.getDefault();
    private final String filename = "filename";
    private final String subscriber = "subscriber";
    private final int line = 1;

    @Test
    void testValidLocation_Ok(){
        when(referentialGetService.getCountries(subscriber)).thenReturn(List.of("FR"));
        Assertions.assertTrue(genericRuleService.checkLocation(locale, subscriber, filename, line, "FR").isEmpty());
    }

    @Test
    void testLocation_WhenInGlobalCountries_Ok() {
        when(referentialGetService.getCountries(subscriber)).thenReturn(List.of());
        when(referentialGetService.getCountries(null)).thenReturn(List.of("FR"));
        Assertions.assertTrue(genericRuleService.checkLocation(locale, subscriber, filename, line, "FR").isEmpty());
    }

    @Test
    void testInValidLocation_Error() {
        when(referentialGetService.getCountries(any())).thenReturn(List.of());
        when(messageSource.getMessage(any(), any(), eq(locale)))
                .thenReturn("Invalid location");

        var actual = genericRuleService.checkLocation(locale, subscriber, filename, line, "testLocation");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "Invalid location"), actual.get());

    }

    @Test
    void testValidType_WithSubscriberAndGlobalTypes_Ok() {
        ItemTypeRest subscriberType = ItemTypeRest.builder().type("Laptop").build();
        ItemTypeRest globalType = ItemTypeRest.builder().type("Monitor").build();

        // Mock subscriber-specific type
        when(referentialGetService.getItemTypes("Laptop", subscriber))
                .thenReturn(List.of(subscriberType));

        // Mock global-only type
        when(referentialGetService.getItemTypes("Monitor", subscriber))
                .thenReturn(List.of());
        when(referentialGetService.getItemTypes("Monitor", null))
                .thenReturn(List.of(globalType));

        // Test subscriber-specific type
       var actual = genericRuleService.checkType(
                locale, subscriber, filename, line, "Laptop");
        assertTrue(actual.isEmpty());

        // Test global type
        var actualGlobal = genericRuleService.checkType(
                locale, "subscriber", filename, line, "Monitor");
        assertTrue(actualGlobal.isEmpty());

        // Verify interactions
        verify(referentialGetService).getItemTypes("Laptop", "subscriber");
        verify(referentialGetService).getItemTypes("Monitor", "subscriber");
        verify(referentialGetService).getItemTypes("Monitor", null);

    }

    @Test
    void testViolations_Ok() {
        when(validator.validate(any())).thenReturn(Set.of());
        assertTrue(genericRuleService.checkViolations(new Object(), filename, line).isEmpty());
    }

}
