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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenericRuleServiceTest {
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
    private final String organization = "organization";
    private final int line = 1;
    private final Long workspaceId = 1L;

    @Test
    void testValidLocation_Ok() {
        when(referentialGetService.getCountries(organization)).thenReturn(List.of("FR"));
        assertTrue(genericRuleService.checkLocation(locale, organization, filename, line, "FR").isEmpty());
    }

    @Test
    void testLocation_WhenInGlobalCountries_Ok() {
        when(referentialGetService.getCountries(organization)).thenReturn(List.of());
        when(referentialGetService.getCountries(null)).thenReturn(List.of("FR"));
        assertTrue(genericRuleService.checkLocation(locale, organization, filename, line, "FR").isEmpty());
    }

    @Test
    void testInValidLocation_Error() {
        when(referentialGetService.getCountries(any())).thenReturn(List.of());
        when(messageSource.getMessage(any(), any(), eq(locale)))
                .thenReturn("Invalid location");

        var actual = genericRuleService.checkLocation(locale, organization, filename, line, "testLocation");
        assertTrue(actual.isPresent());
        assertEquals(new LineError(filename, 1, "Invalid location"), actual.get());
    }

    @Test
    void testCheckType_EmptyType_Error() {
        when(messageSource.getMessage(eq("physical.equipment.must.have.type"), any(), eq(locale)))
                .thenReturn("Type must be provided");
        var actual = genericRuleService.checkType(locale, organization, filename, line, "", false,workspaceId);
        assertTrue(actual.isPresent());
        assertEquals(new LineError(filename, line, "Type must be provided"), actual.get());
    }

    @Test
    void testCheckType_DigitalService_ValidSharedServer_Ok() {
        var actual = genericRuleService.checkType(locale, organization, filename, line, "Shared Server", true,workspaceId);
        assertTrue(actual.isEmpty());
    }

    @Test
    void testCheckType_DigitalService_ValidDedicatedServer_Ok() {
        var actual = genericRuleService.checkType(locale, organization, filename, line, "Dedicated Server", true,workspaceId);
        assertTrue(actual.isEmpty());
    }
    @Test
    void testCheckType_NonDigitalService_TypeExistsForOrganization_Ok() {
        ItemTypeRest typeItem = ItemTypeRest.builder().type("Printer").build();
        when(referentialGetService.getItemTypes("Printer", organization)).thenReturn(List.of(typeItem));
        when(referentialGetService.getItemTypes("Printer", null)).thenReturn(List.of());
        when(referentialGetService.getItemTypesForWorkspace("Printer", workspaceId, organization)).thenReturn(List.of(typeItem));
        when(referentialGetService.getItemTypesForWorkspace(any(), any(), any())).thenAnswer(invocation -> {
            System.out.println("getItemTypesForWorkspace called with: " + invocation.getArgument(0) + ", " + invocation.getArgument(1) + ", " + invocation.getArgument(2));
            return List.of(typeItem);
        });
        var actual = genericRuleService.checkType(locale, organization, filename, line, "Printer", false,workspaceId);
        assertTrue(actual.isEmpty());
    }

    @Test
    void testCheckType_NonDigitalService_TypeExistsGlobally_Ok() {
        ItemTypeRest typeItem = ItemTypeRest.builder().type("Scanner").build();
        when(referentialGetService.getItemTypes("Scanner", organization))
                .thenReturn(List.of());
        when(referentialGetService.getItemTypes("Scanner", null))
                .thenReturn(List.of(typeItem));
        when(referentialGetService.getItemTypesForWorkspace("Scanner", workspaceId, organization))
                .thenReturn(List.of()); // Should be empty to test global only
        var actual = genericRuleService.checkType(locale, organization, filename, line, "Scanner", false,workspaceId);
        assertTrue(actual.isEmpty());
    }

    @Test
    void testCheckType_NonDigitalService_TypeNotExist_Error() {
        when(referentialGetService.getItemTypes("UnknownType", organization)).thenReturn(List.of());
        when(referentialGetService.getItemTypes("UnknownType", null)).thenReturn(List.of());
        when(messageSource.getMessage(eq("referential.type.not.exist"), any(), eq(locale)))
                .thenReturn("Type does not exist");
        var actual = genericRuleService.checkType(locale, organization, filename, line, "UnknownType", false,workspaceId);
        assertTrue(actual.isPresent());
        assertEquals(new LineError(filename, line, "Type does not exist"), actual.get());
    }

    @Test
    void testValidType_WithOrganizationAndGlobalTypes_Ok() {
        ItemTypeRest organizationType = ItemTypeRest.builder().type("Laptop").build();
        ItemTypeRest globalType = ItemTypeRest.builder().type("Monitor").build();

        when(referentialGetService.getItemTypes("Laptop", null)).thenReturn(List.of());
        when(referentialGetService.getItemTypes("Monitor", null)).thenReturn(List.of(globalType));
        when(referentialGetService.getItemTypesForWorkspace("Laptop", workspaceId, organization)).thenReturn(List.of(organizationType));
        when(referentialGetService.getItemTypesForWorkspace("Monitor", workspaceId, "organization")).thenReturn(List.of());

        var actual = genericRuleService.checkType(
                locale, organization, filename, line, "Laptop", false, workspaceId);
        assertTrue(actual.isEmpty());

        var actualGlobal = genericRuleService.checkType(
                locale, "organization", filename, line, "Monitor", false, workspaceId);
        assertTrue(actualGlobal.isEmpty());

        verify(referentialGetService).getItemTypesForWorkspace("Laptop", workspaceId, organization);
        verify(referentialGetService).getItemTypesForWorkspace("Monitor", workspaceId, "organization");
    }

    @Test
    void testViolations_Ok() {
        when(validator.validate(any())).thenReturn(Set.of());
        assertTrue(genericRuleService.checkViolations(new Object(), filename, line).isEmpty());
    }
}
