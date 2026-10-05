/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.apireferential.repository.MatchingItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferentialGetServiceIsWorkspaceSpecificTest {

    @Mock
    ItemImpactRepository itemImpactRepository;
    @Mock
    ItemTypeRepository itemTypeRepository;
    @Mock
    MatchingItemRepository matchingItemRepository;

    @InjectMocks
    ReferentialGetService service;

    @Test
    void testIsWorkspaceSpecific_noData_returnsFalse() {
        when(itemImpactRepository.countByWorkspaceId(1L)).thenReturn(0L);
        when(itemTypeRepository.countByWorkspaceId(1L)).thenReturn(0L);
        when(matchingItemRepository.countByWorkspaceId(1L)).thenReturn(0L);

        assertFalse(service.isWorkspaceSpecific(1L));
    }

    @Test
    void testIsWorkspaceSpecific_hasItemImpact_returnsTrue() {
        when(itemImpactRepository.countByWorkspaceId(1L)).thenReturn(1L);

        assertTrue(service.isWorkspaceSpecific(1L));
    }

    @Test
    void testIsWorkspaceSpecific_hasItemType_returnsTrue() {
        when(itemImpactRepository.countByWorkspaceId(1L)).thenReturn(0L);
        when(itemTypeRepository.countByWorkspaceId(1L)).thenReturn(1L);

        assertTrue(service.isWorkspaceSpecific(1L));
    }

    @Test
    void testIsWorkspaceSpecific_hasMatchingItem_returnsTrue() {
        when(itemImpactRepository.countByWorkspaceId(1L)).thenReturn(0L);
        when(itemTypeRepository.countByWorkspaceId(1L)).thenReturn(0L);
        when(matchingItemRepository.countByWorkspaceId(1L)).thenReturn(1L);

        assertTrue(service.isWorkspaceSpecific(1L));
    }
}
