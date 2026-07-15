/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RuleApplicationService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckApplicationServiceTest {

    @InjectMocks
    private CheckApplicationService service;

    @Mock
    private GenericRuleService genericRuleService;

    @Mock
    private RuleApplicationService ruleApplicationService;

    @Mock
    private Context context;

    @Mock
    private InApplicationRest application;

    @Test
    void shouldReturnNoErrorWhenAllRulesPass() {
        final Locale locale = Locale.FRANCE;
        final String filename = "application.csv";
        final int line = 8;
        final Set<String> physicalEquipments = Set.of("PE-1", "PE-2");

        when(context.getLocale()).thenReturn(locale);
        when(application.getVirtualEquipmentName()).thenReturn("VE-1");
        when(application.getPhysicalEquipmentName()).thenReturn("PE-1");
        when(genericRuleService.checkViolations(application, filename, line)).thenReturn(Optional.empty());
        when(ruleApplicationService.checkVirtualEquipmentLinked(locale, filename, line, "VE-1")).thenReturn(Optional.empty());
        when(ruleApplicationService.checkPhysicalEquipmentLinked(locale, filename, line, "PE-1", physicalEquipments)).thenReturn(Optional.empty());

        final List<LineError> result = service.checkRules(context, application, filename, line, physicalEquipments);

        assertTrue(result.isEmpty());
        verify(genericRuleService).checkViolations(application, filename, line);
        verify(ruleApplicationService).checkVirtualEquipmentLinked(locale, filename, line, "VE-1");
        verify(ruleApplicationService).checkPhysicalEquipmentLinked(locale, filename, line, "PE-1", physicalEquipments);
    }

    @Test
    void shouldCollectAllErrorsWhenAllRulesFail() {
        final Locale locale = Locale.ENGLISH;
        final String filename = "application.csv";
        final int line = 3;
        final Set<String> physicalEquipments = Set.of("PE-1");

        final LineError genericError = new LineError(filename, line, "invalid payload");
        final LineError virtualEquipmentError = new LineError(filename, line, "missing virtual equipment");
        final LineError physicalEquipmentError = new LineError(filename, line, "unknown physical equipment");

        when(context.getLocale()).thenReturn(locale);
        when(application.getVirtualEquipmentName()).thenReturn(null);
        when(application.getPhysicalEquipmentName()).thenReturn("PE-404");
        when(genericRuleService.checkViolations(application, filename, line)).thenReturn(Optional.of(genericError));
        when(ruleApplicationService.checkVirtualEquipmentLinked(locale, filename, line, null))
                .thenReturn(Optional.of(virtualEquipmentError));
        when(ruleApplicationService.checkPhysicalEquipmentLinked(locale, filename, line, "PE-404", physicalEquipments))
                .thenReturn(Optional.of(physicalEquipmentError));

        final List<LineError> result = service.checkRules(context, application, filename, line, physicalEquipments);

        assertEquals(List.of(genericError, virtualEquipmentError, physicalEquipmentError), result);
    }

    @Test
    void shouldCollectOnlyPresentErrors() {
        final Locale locale = Locale.CANADA;
        final String filename = "application.csv";
        final int line = 21;
        final Set<String> physicalEquipments = Set.of("PE-1", "PE-2");
        final LineError virtualEquipmentError = new LineError(filename, line, "missing virtual equipment");

        when(context.getLocale()).thenReturn(locale);
        when(application.getVirtualEquipmentName()).thenReturn(null);
        when(application.getPhysicalEquipmentName()).thenReturn("PE-1");
        when(genericRuleService.checkViolations(application, filename, line)).thenReturn(Optional.empty());
        when(ruleApplicationService.checkVirtualEquipmentLinked(locale, filename, line, null))
                .thenReturn(Optional.of(virtualEquipmentError));
        when(ruleApplicationService.checkPhysicalEquipmentLinked(locale, filename, line, "PE-1", physicalEquipments))
                .thenReturn(Optional.empty());

        final List<LineError> result = service.checkRules(context, application, filename, line, physicalEquipments);

        assertEquals(1, result.size());
        assertEquals(virtualEquipmentError, result.getFirst());
    }
}

