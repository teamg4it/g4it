package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.mapper.CsvToInMapper;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PhysicalEquipmentUsageValidatorTest {
    private final ItemTypeRepository repository = mock(ItemTypeRepository.class);
    private final PhysicalEquipmentReferenceResolver references = mock(PhysicalEquipmentReferenceResolver.class);
    private final ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
    private PhysicalEquipmentUsageValidator validator;
    private Context context;

    @BeforeEach
    void setUp() {
        messages.setBasename("messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        context = Context.builder().inventoryId(1L).organization("org").workspaceId(2L).locale(Locale.ENGLISH).build();
        validator = new PhysicalEquipmentUsageValidator(new ItemCategoryResolver(repository), references, messages);
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(2L))
                .thenReturn(List.of(item("Laptop", "DEVICES", "ref")));
        when(references.resolve(any(), any(), any(), any(), anyLong()))
                .thenReturn(new PhysicalEquipmentReferenceResolver.Reference(null, null, "ref", true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"BYOD", "COPE"})
    void exactContextsAreAccepted(String value) {
        assertTrue(validator.check(context, equipment(value, "0.5"), "input.csv", 2).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"byod", "cope", "UNKNOWN", " BYOD", "COPE ", " "})
    void contextIsCaseSensitiveAndNotTrimmed(String value) {
        var errors = validator.check(context, equipment(value, "0.5"), "input.csv", 2);
        assertEquals(1, errors.size());
        assertEquals("Invalid value for column \"contexteUsage\". Allowed values are: BYOD, COPE, or empty.",
                errors.getFirst().error());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0", "1", "0.57", "1e-1", " 0.5 ", "0.000000000000000000000001"})
    void emptyAndInclusiveFractionsAreAccepted(String value) {
        assertTrue(validator.check(context, equipment("BYOD", value), "input.csv", 2).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.1", "2.0", "abc", "NaN", "Infinity", "-Infinity", "0,5", " ",
            "1.000000000000000000000001", "-0.000000000000000000000001", "1e999"})
    void invalidAndOutOfBoundsFractionsAreRejected(String value) {
        var errors = validator.check(context, equipment("COPE", value), "input.csv", 2);
        assertEquals(1, errors.size());
        assertEquals("Invalid value for column \"fractionUsage\". The value must be a number between 0 and 1.",
                errors.getFirst().error());
    }

    @Test
    void collectsBothLocalizedErrorsWithoutChangingSourceValues() {
        var equipment = equipment("byod", "abc");
        var errors = validator.check(context, equipment, "input.csv", 7);
        assertEquals(2, errors.size());
        assertTrue(errors.stream().allMatch(e -> e.line() == 7 && e.filename().equals("input.csv")));
        assertEquals(JsonNullable.of("byod"), equipment.getUsageContext());
        assertEquals(JsonNullable.of("abc"), equipment.getUsageFraction());
        Context french = Context.builder().inventoryId(1L).organization("org").workspaceId(2L).locale(Locale.FRENCH).build();
        var localized = validator.check(french, equipment, "input.csv", 7);
        assertEquals("Valeur invalide pour la colonne \"contexteUsage\". Les valeurs autorisées sont : BYOD, COPE, ou vide.",
                localized.get(0).error());
        assertEquals("Valeur invalide pour la colonne \"fractionUsage\". La valeur doit être un nombre compris entre 0 et 1.",
                localized.get(1).error());
    }

    @Test
    void referencePrecedenceAndNonDeviceExemptionsPreserveRawValues() {
        for (String category : List.of("INFRASTRUCTURE", "Devices", "CUSTOM")) {
            when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(2L))
                    .thenReturn(List.of(item("Laptop", "DEVICES", "device"), item("Server", category, "ref")));
            var equipment = equipment(" anything ", "not a number");
            assertTrue(validator.check(context, equipment, "input.csv", 2).isEmpty());
            assertEquals(JsonNullable.of("not a number"), equipment.getUsageFraction());
        }
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(2L)).thenReturn(List.of());
        assertTrue(validator.check(context, equipment("invalid", "invalid"), "input.csv", 2).isEmpty());
    }

    @Test
    void digitalServiceAndEmptyLegacyInputDoNotResolveCategories() {
        clearInvocations(repository, references);
        var digital = Context.builder().digitalServiceVersionUid("synthetic").build();
        assertTrue(validator.check(digital, equipment("invalid", "abc"), "input.csv", 2).isEmpty());
        assertTrue(validator.check(context, new InPhysicalEquipmentRest(), "input.csv", 2).isEmpty());
        verifyNoInteractions(repository, references);
    }

    @Test
    void parserRetainsExemptRawValuesAndSupportsLegacyFiles() throws Exception {
        CsvToInMapper mapper = Mappers.getMapper(CsvToInMapper.class);
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(2L)).thenReturn(List.of(item("Server", "INFRASTRUCTURE", "ref")));
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader(
                " type ; contexteUsage ; fractionUsage \nServer; custom ;abc\n"))) {
            var equipment = mapper.csvInPhysicalEquipmentToRest(parser.iterator().next(), 1L, null);
            assertEquals(JsonNullable.of(" custom "), equipment.getUsageContext());
            assertEquals(JsonNullable.of("abc"), equipment.getUsageFraction());
            assertTrue(validator.check(context, equipment, "input.csv", 2).isEmpty());
        }
        try (var parser = CsvUtils.parseWithNormalizedHeaders(new StringReader("type\nLaptop\n"))) {
            var equipment = mapper.csvInPhysicalEquipmentToRest(parser.iterator().next(), 1L, null);
            assertNull(equipment.getUsageContext().orElse(null));
            assertNull(equipment.getUsageFraction().orElse(null));
            assertTrue(validator.check(context, equipment, "input.csv", 2).isEmpty());
        }
    }

    private static InPhysicalEquipmentRest equipment(String context, String fraction) {
        return InPhysicalEquipmentRest.builder().type("Laptop").usageContext(JsonNullable.of(context))
                .usageFraction(JsonNullable.of(fraction)).build();
    }

    @Test
    void digitalServiceWithInventoryIdentityStillIgnoresUsageValidation() {
        var digital = Context.builder().inventoryId(1L).digitalServiceVersionUid("synthetic").build();
        clearInvocations(repository, references);

        assertTrue(validator.check(digital, equipment("invalid", "abc"), "input.csv", 2).isEmpty());

        verifyNoInteractions(repository, references);
    }

    @Test
    void organizationOnlyDeviceStillCollectsBothErrorsWithoutWorkspaceLookup() {
        var organizationOnly = Context.builder().inventoryId(1L).organization("org")
                .locale(Locale.ENGLISH).build();
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(null))
                .thenReturn(List.of(item("Laptop", "DEVICES", "ref")));

        var errors = validator.check(organizationOnly, equipment("invalid", "abc"), "input.csv", 2);

        assertEquals(2, errors.size());
        verify(references).resolve(null, "Laptop", "org", null, 0L);
        verify(repository).findByWorkspaceIdOrWorkspaceIdIsNull(null);
    }

    @Test
    void nullOptionalWrappersRemainLegacyInputWithoutReferenceLookups() {
        var legacy = InPhysicalEquipmentRest.builder().type("Laptop")
                .usageContext(null).usageFraction(null).build();
        clearInvocations(repository, references);

        assertTrue(validator.check(context, legacy, "input.csv", 2).isEmpty());

        assertNull(legacy.getUsageContext());
        assertNull(legacy.getUsageFraction());
        verifyNoInteractions(repository, references);
    }

    private static ItemType item(String type, String category, String reference) {
        return ItemType.builder().type(type).itemCategory(category).refDefaultItem(reference).build();
    }
}
