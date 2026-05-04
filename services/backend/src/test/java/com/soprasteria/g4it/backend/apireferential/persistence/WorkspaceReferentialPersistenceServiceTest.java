/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.persistence;

import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceReferentialPersistenceServiceTest {

    @InjectMocks
    private WorkspaceReferentialPersistenceService service;

    @Mock private ItemTypeRepository itemTypeRepository;
    @Mock private ItemImpactRepository itemImpactRepository;
    @Mock private MatchingItemRepository matchingItemRepository;
    @Mock private HypothesisRepository hypothesisRepository;

    // =========================
    // ITEM TYPE
    // =========================

    @Test
    void syncItemTypes_update_create_delete() {
        Long workspaceId = 1L;

        ItemType existing = new ItemType();
        existing.setType("A");

        ItemType updated = new ItemType();
        updated.setType("A");
        updated.setCategory("updated");

        ItemType newItem = new ItemType();
        newItem.setType("B");

        when(itemTypeRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of(existing));

        service.syncItemTypes(workspaceId, List.of(updated, newItem));

        verify(itemTypeRepository).saveAll(any());
        verify(itemTypeRepository).deleteAll(any());
    }

    @Test
    void syncItemTypes_emptyInput_deletesAll() {
        service.syncItemTypes(1L, List.of());

        verify(itemTypeRepository).deleteByWorkspaceId(1L);
    }

    @Test
    void syncItemTypes_setsWorkspaceId_forNewItems() {
        Long workspaceId = 1L;

        ItemType newItem = new ItemType();
        newItem.setType("A");

        when(itemTypeRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of());

        service.syncItemTypes(workspaceId, List.of(newItem));

        assertEquals(workspaceId, newItem.getWorkspaceId());
    }

    @Test
    void syncMatchingItems_duplicateKeys_shouldNotCrash() {
        Long workspaceId = 1L;

        MatchingItem m1 = new MatchingItem();
        m1.setItemSource("A");

        MatchingItem m2 = new MatchingItem();
        m2.setItemSource("A");

        when(matchingItemRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                service.syncMatchingItems(workspaceId, List.of(m1, m2))
        );

        verify(matchingItemRepository).saveAll(any());
    }

    @Test
    void syncItemTypes_nullKey_shouldNotCrash() {
        Long workspaceId = 1L;

        ItemType item = new ItemType();
        item.setType(null); // null key

        when(itemTypeRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                service.syncItemTypes(workspaceId, List.of(item))
        );

        verify(itemTypeRepository).saveAll(any());
    }

    // =========================
    // ITEM IMPACT
    // =========================

    @Test
    void syncItemImpacts_success() {
        Long workspaceId = 1L;

        ItemImpact existing = new ItemImpact();
        existing.setName("A");
        existing.setLifecycleStep("L");
        existing.setCriterion("C");

        ItemImpact updated = new ItemImpact();
        updated.setName("A");
        updated.setLifecycleStep("L");
        updated.setCriterion("C");

        when(itemImpactRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of(existing));

        service.syncItemImpacts(workspaceId, List.of(updated));

        verify(itemImpactRepository).saveAll(any());
    }

    @Test
    void syncItemImpacts_emptyInput_deletesAll() {
        service.syncItemImpacts(1L, List.of());

        verify(itemImpactRepository).deleteByWorkspaceId(1L);
    }

    // =========================
    // MATCHING ITEM
    // =========================

    @Test
    void syncMatchingItems_success() {
        Long workspaceId = 1L;

        MatchingItem existing = new MatchingItem();
        existing.setItemSource("A");

        MatchingItem updated = new MatchingItem();
        updated.setItemSource("A");
        updated.setRefItemTarget("new");

        when(matchingItemRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of(existing));

        service.syncMatchingItems(workspaceId, List.of(updated));

        verify(matchingItemRepository).saveAll(any());
    }

    @Test
    void syncMatchingItems_emptyInput_deletesAll() {
        service.syncMatchingItems(1L, List.of());

        verify(matchingItemRepository).deleteByWorkspaceId(1L);
    }

    @Test
    void syncItemTypes_duplicateKeys_shouldNotCrash() {
        Long workspaceId = 1L;

        ItemType item1 = new ItemType();
        item1.setType("A");

        ItemType item2 = new ItemType();
        item2.setType("A"); // duplicate

        when(itemTypeRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                service.syncItemTypes(workspaceId, List.of(item1, item2))
        );

        verify(itemTypeRepository).saveAll(any());
    }
}