// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RuleDateService;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.soprasteria.g4it.backend.common.model.LineError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckPhysicalEquipmentServiceTest {

    @Mock
    private GenericRuleService genericRuleService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private RuleDateService ruleDateService;

    @Mock
    private DigitalServiceRuleFactory digitalServiceRuleFactory;

    @Mock
    private PhysicalEquipmentUsageValidator usageValidator;

    @InjectMocks
    private CheckPhysicalEquipmentService service;

    private Context context;
    private InPhysicalEquipmentRest equipment;

    @Test
    void shouldAccumulateUsageAndOtherFieldErrors() {
        Context inventory = Context.builder().inventoryId(1L).build();
        LineError dateError = new LineError("input.csv", 2, "date");
        var usageErrors = List.of(new LineError("input.csv", 2, "context"),
                new LineError("input.csv", 2, "fraction"));
        when(ruleDateService.checkDatesPurchaseRetrieval(any(), anyString(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(Optional.of(dateError));
        when(usageValidator.check(inventory, equipment, "input.csv", 2)).thenReturn(usageErrors);
        var errors = service.checkRules(inventory, equipment, "input.csv", 2);
        assertEquals(3, errors.size());
        assertTrue(errors.containsAll(usageErrors));
        assertTrue(errors.contains(dateError));
    }

    @Test
    void shouldNotApplyInventoryUsageRulesToDigitalServiceWithInventoryIdentity() {
        var digitalService = Context.builder()
                .inventoryId(1L)
                .digitalServiceVersionUid("version")
                .build();
        equipment.setUsageContext(JsonNullable.of("BYOD"));
        equipment.setUsageFraction(JsonNullable.of("invalid"));

        assertTrue(service.checkRules(digitalService, equipment, "input.csv", 2).isEmpty());

        verifyNoInteractions(usageValidator, digitalServiceRuleFactory);
    }

    @Test
    void shouldReportUnknownTypeAsARowErrorEvenWithUsageContext() {
        var referentialGetService = mock(ReferentialGetService.class);
        var referentialService = new ReferentialService();
        ReflectionTestUtils.setField(referentialService, "referentialGetService", referentialGetService);
        var validator = new PhysicalEquipmentUsageValidator(
                new ItemCategoryResolver(mock(ItemTypeRepository.class)),
                new PhysicalEquipmentReferenceResolver(referentialService), messageSource);
        ReflectionTestUtils.setField(service, "usageValidator", validator);
        var inventory = Context.builder().inventoryId(1L).organization("synthetic-org").workspaceId(2L).build();
        equipment.setType("unknown-type");
        equipment.setUsageContext(JsonNullable.of("BYOD"));
        var typeError = new LineError("input.csv", 2, "Type does not exist in referential");
        when(genericRuleService.checkType(any(), any(), anyString(), anyInt(), any(), anyBoolean(), any()))
                .thenReturn(Optional.of(typeError));

        assertEquals(List.of(typeError), service.checkRules(inventory, equipment, "input.csv", 2));
        verify(referentialGetService).getItemTypes("unknown-type", "synthetic-org", null);
        verify(referentialGetService).getItemTypes("unknown-type", null, null);
    }

    @BeforeEach
    void setUp() {
        context = Context.builder().build();
        equipment = new InPhysicalEquipmentRest();

        when(genericRuleService.checkViolations(any(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        when(genericRuleService.checkLocation(any(), any(), anyString(), anyInt(), any()))
                .thenReturn(Optional.empty());

        when(genericRuleService.checkType(any(), any(), anyString(), anyInt(), any(), anyBoolean(), any()))
                .thenReturn(Optional.empty());

        when(ruleDateService.checkDatesPurchaseRetrieval(
                any(), anyString(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(Optional.empty());
    }

    @Test
    void shouldReturnEmptyListWhenNoErrors() {
        equipment.setType("Laptop");

        List<LineError> result =
                service.checkRules(context, equipment, "file.csv", 1);

        assertTrue(result.isEmpty());

        verify(ruleDateService)
                .checkDatesPurchaseRetrieval(
                        any(), anyString(), anyInt(),
                        any(), any(), eq(false));
    }

    @Test
    void shouldCollectGenericErrors() {
        LineError violation = mock(LineError.class);
        LineError location = mock(LineError.class);
        LineError type = mock(LineError.class);

        when(genericRuleService.checkViolations(any(), anyString(), anyInt()))
                .thenReturn(Optional.of(violation));

        when(genericRuleService.checkLocation(any(), any(), anyString(), anyInt(), any()))
                .thenReturn(Optional.of(location));

        when(genericRuleService.checkType(any(), any(), anyString(), anyInt(), any(), anyBoolean(), any()))
                .thenReturn(Optional.of(type));

        List<LineError> result =
                service.checkRules(context, equipment, "file.csv", 1);

        assertEquals(3, result.size());
        assertTrue(result.contains(violation));
        assertTrue(result.contains(location));
        assertTrue(result.contains(type));
    }

    @Test
    void shouldExecuteDateRuleForDigitalServiceNonNetwork() {

        context = Context.builder()
                .digitalServiceVersionUid("1L")
                .build();
        equipment.setType("SERVER");

        service.checkRules(context, equipment, "file.csv", 1);

        verify(ruleDateService)
                .checkDatesPurchaseRetrieval(
                        any(), anyString(), anyInt(),
                        any(), any(), eq(true));
    }

    @Test
    void shouldApplyDigitalServiceRule() {

        context = Context.builder()
                .digitalServiceVersionUid("1L")
                .build();
        equipment.setType("SERVER");

        DigitalServiceRule rule = mock(DigitalServiceRule.class);

        LineError error = mock(LineError.class);

        when(digitalServiceRuleFactory.getRule("SERVER"))
                .thenReturn(rule);

        when(rule.validate(any(), eq(equipment), eq("file.csv"), eq(1)))
                .thenReturn(List.of(error));

        List<LineError> result =
                service.checkRules(context, equipment, "file.csv", 1);

        assertEquals(1, result.size());
        assertSame(error, result.getFirst());
    }

    @Test
    void shouldAddErrorWhenDigitalRuleNotFound() {

        context = Context.builder()
                .digitalServiceVersionUid("1L")
                .build();
        equipment.setType("UNKNOWN");

        when(digitalServiceRuleFactory.getRule("UNKNOWN"))
                .thenReturn(null);

        when(messageSource.getMessage(
                eq("physical.eqp.type.invalid"),
                any(),
                any()))
                .thenReturn("invalid type");

        List<LineError> result =
                service.checkRules(context, equipment, "file.csv", 1);

        assertEquals(1, result.size());
        assertEquals("invalid type", result.getFirst().error());
    }

    @Test
    void shouldNotLookupRuleWhenTypeIsNull() {

        context = Context.builder()
                .digitalServiceVersionUid("1L")
                .build();
        equipment.setType(null);

        service.checkRules(context, equipment, "file.csv", 1);

        verifyNoInteractions(digitalServiceRuleFactory);
    }
}

