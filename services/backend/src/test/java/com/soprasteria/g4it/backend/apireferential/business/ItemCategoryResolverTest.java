// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCategoryResolverTest {

    @Mock
    private ItemTypeRepository repository;
    @InjectMocks
    private ItemCategoryResolver resolver;

    @Test
    void referenceWinsOverTypeAndWorkspaceWinsOverOrganizationAndGlobal() {
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                item("laptop", "INFRASTRUCTURE", null, null, null),
                item("laptop", "organization category", "synthetic-org", null, null),
                item("laptop", "workspace category", null, 7L, null),
                item("global reference", "global category", null, null, "model"),
                item("organization reference", "organization category", "synthetic-org", null, "model"),
                item("workspace reference", "DEVICES", null, 7L, "model"),
                item("unrelated organization", "wrong category", "other-org", 7L, "model")));

        var lookup = resolver.prepare("synthetic-org", 7L);

        var reference = lookup.resolve("model", "laptop");
        assertEquals("workspace reference", reference.itemType());
        assertTrue(reference.isDevice());
        assertEquals("workspace category", lookup.resolve("unknown-model", "laptop").itemCategory());
        verify(repository, times(1)).findByWorkspaceIdOrWorkspaceIdIsNull(7L);
    }

    @Test
    void referencePrecedesEvenAWorkspaceTypeOverride() {
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                item("laptop", "workspace category", null, 7L, null),
                item("reference type", "DEVICES", null, null, "model")));

        assertTrue(resolver.prepare("synthetic-org", 7L).resolve("model", "laptop").isDevice());
    }

    @Test
    void organizationOverridesGlobalAndMissingCategoryDoesNotOverride() {
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                item("laptop", "INFRASTRUCTURE", null, null, null),
                item("laptop", "custom category", "synthetic-org", null, null),
                item("laptop", null, null, 7L, null)));

        assertEquals("custom category",
                resolver.prepare("synthetic-org", 7L).resolve(null, "laptop").itemCategory());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Devices", "DEVICES ", "custom category"})
    void onlyExactDevicesIsEligibleAndOtherCategoriesRemainUnchanged(String category) {
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(7L))
                .thenReturn(List.of(item("laptop", category, null, null, null)));

        var resolved = resolver.prepare("synthetic-org", 7L).resolve(null, "laptop");

        assertEquals(category, resolved.itemCategory());
        assertFalse(resolved.isDevice());
    }

    @Test
    void unmatchedEquipmentUsesInfrastructureAndKeepsItsType() {
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of());

        var resolved = resolver.prepare("synthetic-org", 7L).resolve("unknown-reference", "unknown-type");

        assertEquals("unknown-type", resolved.itemType());
        assertEquals("INFRASTRUCTURE", resolved.itemCategory());
        assertFalse(resolved.isDevice());
    }

    private ItemType item(String type, String category, String organization, Long workspace, String reference) {
        return ItemType.builder().type(type).itemCategory(category).organization(organization)
                .workspaceId(workspace).refDefaultItem(reference).build();
    }

    @Test
    void organizationWinsWithinTheSameWorkspaceRegardlessOfRepositoryOrder() {
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                item("laptop", "INFRASTRUCTURE", null, 7L, "model"),
                item("laptop", "DEVICES", "synthetic-org", 7L, "model")));

        var lookup = resolver.prepare("synthetic-org", 7L);

        assertTrue(lookup.resolve("model", "other-type").isDevice());
        assertTrue(lookup.resolve(null, "laptop").isDevice());
    }

    @Test
    void absentWorkspaceStillUsesOrganizationBeforeGlobalForBothLookupKeys() {
        when(repository.findByWorkspaceIdOrWorkspaceIdIsNull(null)).thenReturn(List.of(
                item("laptop", "INFRASTRUCTURE", null, null, "model"),
                item("laptop", "DEVICES", "synthetic-org", null, "model"),
                item("laptop", "other category", "other-org", null, "model")));

        var lookup = resolver.prepare("synthetic-org", null);

        assertTrue(lookup.resolve("model", "other-type").isDevice());
        assertTrue(lookup.resolve(null, "laptop").isDevice());
        verify(repository).findByWorkspaceIdOrWorkspaceIdIsNull(null);
    }
}
