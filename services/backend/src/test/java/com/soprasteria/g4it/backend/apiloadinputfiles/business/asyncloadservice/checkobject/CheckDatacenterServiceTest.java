/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckDatacenterServiceTest {

    @InjectMocks
    private CheckDatacenterService service;

    @Mock
    private GenericRuleService genericRuleService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private Context context;

    @Mock
    private InDatacenterRest datacenter;

    @Test
    void shouldReturnNoErrorWhenAllRulesPass() {
        final Locale locale = Locale.FRANCE;
        final String organization = "ORG";
        final String filename = "datacenter.csv";
        final int line = 4;

        when(context.getLocale()).thenReturn(locale);
        when(context.getOrganization()).thenReturn(organization);
        when(datacenter.getLocation()).thenReturn("France");
        when(datacenter.getPue()).thenReturn(1.2d);
        when(genericRuleService.checkViolations(datacenter, filename, line)).thenReturn(Optional.empty());
        when(genericRuleService.checkLocation(locale, organization, filename, line, "France")).thenReturn(Optional.empty());

        final List<LineError> result = service.checkRules(context, datacenter, filename, line);

        assertTrue(result.isEmpty());
        verify(genericRuleService).checkViolations(datacenter, filename, line);
        verify(genericRuleService).checkLocation(locale, organization, filename, line, "France");
    }

    @Test
    void shouldAddPueErrorWhenPueIsNullOrLessOrEqualToOne() {
        final Locale locale = Locale.ENGLISH;
        final String filename = "datacenter.csv";
        final int line = 12;

        when(context.getLocale()).thenReturn(locale);
        when(context.getOrganization()).thenReturn("ORG");
        when(datacenter.getLocation()).thenReturn("France");
        when(datacenter.getPue()).thenReturn(1.0d);
        when(genericRuleService.checkViolations(datacenter, filename, line)).thenReturn(Optional.empty());
        when(genericRuleService.checkLocation(any(), any(), eq(filename), eq(line), any())).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("pue.should.greater.than.one"), any(String[].class), eq(locale)))
                .thenReturn("pue must be greater than one");

        final List<LineError> result = service.checkRules(context, datacenter, filename, line);

        assertEquals(1, result.size());
        assertEquals(new LineError(filename, line, "pue must be greater than one"), result.getFirst());
    }

    @Test
    void shouldCollectAllErrorsWhenAllChecksFail() {
        final Locale locale = Locale.CANADA_FRENCH;
        final String organization = "ORG";
        final String filename = "datacenter.csv";
        final int line = 27;

        final LineError violationError = new LineError(filename, line, "constraint error");
        final LineError locationError = new LineError(filename, line, "unknown location");
        final LineError pueError = new LineError(filename, line, "pue must be greater than one");

        when(context.getLocale()).thenReturn(locale);
        when(context.getOrganization()).thenReturn(organization);
        when(datacenter.getLocation()).thenReturn("Unknown");
        when(datacenter.getPue()).thenReturn(null);
        when(genericRuleService.checkViolations(datacenter, filename, line)).thenReturn(Optional.of(violationError));
        when(genericRuleService.checkLocation(locale, organization, filename, line, "Unknown"))
                .thenReturn(Optional.of(locationError));
        when(messageSource.getMessage(eq("pue.should.greater.than.one"), any(String[].class), eq(locale)))
                .thenReturn("pue must be greater than one");

        final List<LineError> result = service.checkRules(context, datacenter, filename, line);

        assertEquals(List.of(violationError, locationError, pueError), result);
    }
}

