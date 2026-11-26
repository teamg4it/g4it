// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.server.gen.api.dto.ItemTypeRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.MatchingItemRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysicalEquipmentReferenceResolverTest {
    @Mock
    private ReferentialService referentialService;

    @InjectMocks
    private PhysicalEquipmentReferenceResolver resolver;

    @Test
    void selectsWorkspaceModelReferenceBeforeTypeDefault() {
        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setRefItemTarget("matched-reference");
        ItemTypeRest itemType = new ItemTypeRest();
        itemType.setRefDefaultItem("default-reference");
        when(referentialService.getMatchingItemForWorkspace("synthetic-model", 7L)).thenReturn(matchingItem);
        when(referentialService.getItemTypeForWorkspace("laptop", 7L)).thenReturn(itemType);

        var result = resolver.resolve("synthetic-model", "laptop", "synthetic-org", 7L, 1);

        assertSame(matchingItem, result.matchingItem());
        assertSame(itemType, result.itemType());
        assertEquals("matched-reference", result.reference());
        assertTrue(result.modelMatched());
        verify(referentialService, never()).getMatchingItem(anyString(), anyString());
        verify(referentialService, never()).getItemType(anyString(), anyString());
    }

    @Test
    void fallsBackToOrganizationTypeDefaultWhenWorkspaceAndModelAreUnmatched() {
        ItemTypeRest itemType = new ItemTypeRest();
        itemType.setRefDefaultItem("default-reference");
        when(referentialService.getItemType("laptop", "synthetic-org")).thenReturn(itemType);

        var result = resolver.resolve("unknown-model", "laptop", "synthetic-org", 7L, 1);

        assertNull(result.matchingItem());
        assertEquals("default-reference", result.reference());
        assertFalse(result.modelMatched());
        verify(referentialService).getMatchingItemForWorkspace("unknown-model", 7L);
        verify(referentialService).getMatchingItem("unknown-model", "synthetic-org");
        verify(referentialService).getItemTypeForWorkspace("laptop", 7L);
        verify(referentialService).getItemType("laptop", "synthetic-org");
    }

    @Test
    void resolvesOrganizationReferencesWithoutWorkspaceRecords() {
        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setRefItemTarget("organization-reference");
        when(referentialService.getMatchingItem("synthetic-model", "synthetic-org")).thenReturn(matchingItem);

        var result = resolver.resolve("synthetic-model", "laptop", "synthetic-org", 7L, 0);

        assertEquals("organization-reference", result.reference());
        assertTrue(result.modelMatched());
        verify(referentialService, never()).getMatchingItemForWorkspace(anyString(), anyLong());
        verify(referentialService, never()).getItemTypeForWorkspace(anyString(), anyLong());
        verify(referentialService).getItemType("laptop", "synthetic-org");
    }

    @Test
    void returnsNoReferenceWhenTypeAndModelAreUnmatched() {
        var result = resolver.resolve("unknown-model", "unknown-type", "synthetic-org", 7L, 1);

        assertNull(result.matchingItem());
        assertNull(result.itemType());
        assertNull(result.reference());
    }
}
