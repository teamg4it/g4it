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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferentialPersistenceServiceTest {
    @Mock
    private LifecycleStepRepository lifecycleStepRepository;
    @Mock
    private CriterionRepository criteriaRepository;
    @Mock
    private ItemTypeRepository itemTypeRepository;
    @Mock
    private HypothesisRepository hypothesisRepository;
    @Mock
    private MatchingItemRepository matchingItemRepository;
    @Mock
    private ItemImpactRepository itemImpactRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private ReferentialPersistenceService service;

    @Test
    void testSaveCriteria() {
        Criterion c = new Criterion();
        List<Criterion> list = Collections.singletonList(c);
        when(criteriaRepository.saveAll(list)).thenReturn(list);
        int result = service.saveCriteria(list);
        verify(criteriaRepository).deleteAll();
        verify(criteriaRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testSaveLifecycleSteps() {
        LifecycleStep l = new LifecycleStep();
        List<LifecycleStep> list = Collections.singletonList(l);
        when(lifecycleStepRepository.saveAll(list)).thenReturn(list);
        int result = service.saveLifecycleSteps(list);
        verify(lifecycleStepRepository).deleteAll();
        verify(lifecycleStepRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testSaveItemTypes() {
        ItemType i = new ItemType();
        List<ItemType> list = Collections.singletonList(i);
        when(itemTypeRepository.saveAll(list)).thenReturn(list);
        int result = service.saveItemTypes(list);
        verify(itemTypeRepository).deleteAll();
        verify(itemTypeRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testSaveItemTypesWithOrganization() {
        ItemType i = new ItemType();
        List<ItemType> list = Collections.singletonList(i);
        String org = "org";
        when(itemTypeRepository.saveAll(list)).thenReturn(list);
        int result = service.saveItemTypes(list, org);
        verify(itemTypeRepository).deleteByOrganization(org);
        verify(itemTypeRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testSaveHypotheses() {
        Hypothesis h = new Hypothesis();
        List<Hypothesis> list = Collections.singletonList(h);
        when(hypothesisRepository.saveAll(list)).thenReturn(list);
        int result = service.saveHypotheses(list);
        verify(hypothesisRepository).deleteAll();
        verify(hypothesisRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testSaveHypothesesWithOrganization() {
        Hypothesis h = new Hypothesis();
        List<Hypothesis> list = Collections.singletonList(h);
        String org = "org";
        when(hypothesisRepository.saveAll(list)).thenReturn(list);
        int result = service.saveHypotheses(list, org);
        verify(hypothesisRepository).deleteByOrganization(org);
        verify(hypothesisRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testSaveItemMatchings() {
        MatchingItem m = new MatchingItem();
        List<MatchingItem> list = Collections.singletonList(m);
        when(matchingItemRepository.saveAll(list)).thenReturn(list);
        int result = service.saveItemMatchings(list);
        verify(matchingItemRepository).deleteAll();
        verify(matchingItemRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testSaveItemMatchingsWithOrganization() {
        MatchingItem m = new MatchingItem();
        List<MatchingItem> list = Collections.singletonList(m);
        String org = "org";
        when(matchingItemRepository.saveAll(list)).thenReturn(list);
        int result = service.saveItemMatchings(list, org);
        verify(matchingItemRepository).deleteByOrganization(org);
        verify(matchingItemRepository).saveAll(list);
        assertEquals(1, result);
    }

    @Test
    void testTruncateItemImpacts() {
        service.truncateItemImpacts();
        verify(itemImpactRepository).truncateTable();
    }

    @Test
    void testDeleteItemImpactsByOrganization() {
        String org = "org";
        service.deleteItemImpactsByOrganization(org);
        verify(itemImpactRepository).deleteByOrganization(org);
    }

    @Test
    void testSaveItemImpacts() {
        ItemImpact i = new ItemImpact();
        List<ItemImpact> list = Collections.singletonList(i);
        service.saveItemImpacts(list);
        verify(itemImpactRepository).saveAll(list);
        verify(entityManager).flush();
        verify(entityManager).clear();
    }
}
