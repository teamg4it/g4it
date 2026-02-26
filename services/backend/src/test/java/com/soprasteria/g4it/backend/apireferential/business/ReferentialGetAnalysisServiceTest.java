/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.model.AnalysisTableBO;
import com.soprasteria.g4it.backend.apireferential.modeldb.Criterion;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.apireferential.modeldb.LifecycleStep;
import com.soprasteria.g4it.backend.apireferential.modeldb.MatchingItem;
import com.soprasteria.g4it.backend.apireferential.repository.CriterionRepository;
import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.apireferential.repository.LifecycleStepRepository;
import com.soprasteria.g4it.backend.apireferential.repository.MatchingItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferentialGetAnalysisServiceTest {
    @Mock
    ItemImpactRepository itemImpactRepo;
    @Mock
    MatchingItemRepository matchingItemRepo;
    @Mock
    CriterionRepository criterionRepo;
    @Mock
    LifecycleStepRepository lifecycleStepRepo;

    @InjectMocks
    ReferentialGetAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new ReferentialGetAnalysisService(itemImpactRepo, matchingItemRepo, criterionRepo, lifecycleStepRepo);
    }

    @Test
    void testGetAnalysis_ItemImpactErrors() {
        ItemImpact impact = mock(ItemImpact.class);
        when(itemImpactRepo.findByLevel("2-Equipement")).thenReturn(List.of(impact));
        when(impact.getName()).thenReturn("equip1");
        when(impact.getLifecycleStep()).thenReturn("step1");
        when(impact.getCriterion()).thenReturn("crit1");
        when(impact.getAvgElectricityConsumption()).thenReturn(null);
        Criterion criterion = mock(Criterion.class);
        when(criterion.getCode()).thenReturn("crit1");
        when(criterionRepo.findAll()).thenReturn(List.of(criterion));
        LifecycleStep lifecycleStep = mock(LifecycleStep.class);
        when(lifecycleStep.getCode()).thenReturn("step1");
        when(lifecycleStepRepo.findAll()).thenReturn(List.of(lifecycleStep));
        when(itemImpactRepo.findByCategory("electricity-mix")).thenReturn(Collections.emptyList());

        List<AnalysisTableBO> result = service.getAnalysis();
        assertFalse(result.isEmpty());
        assertEquals("ref_item_impact", result.get(0).getTable());
        assertFalse(result.get(0).getErrors().isEmpty());
    }

    @Test
    void testGetAnalysis_MatchingItemWarnings() {
        when(itemImpactRepo.findByLevel("2-Equipement")).thenReturn(Collections.emptyList());
        when(criterionRepo.findAll()).thenReturn(Collections.emptyList());
        when(lifecycleStepRepo.findAll()).thenReturn(Collections.emptyList());
        when(itemImpactRepo.findByCategory("electricity-mix")).thenReturn(Collections.emptyList());

        MatchingItem item = mock(MatchingItem.class);
        when(matchingItemRepo.findAll()).thenReturn(List.of(item));
        when(item.getRefItemTarget()).thenReturn("Not-Kebab-Case");
        List<AnalysisTableBO> result = service.getAnalysis();
        assertFalse(result.isEmpty());
        assertEquals("ref_matching_item", result.get(0).getTable());
        assertFalse(result.get(0).getWarnings().isEmpty());
    }

    @Test
    void testGetAnalysis_NoErrorsOrWarnings() {
        when(itemImpactRepo.findByLevel("2-Equipement")).thenReturn(Collections.emptyList());
        when(criterionRepo.findAll()).thenReturn(Collections.emptyList());
        when(lifecycleStepRepo.findAll()).thenReturn(Collections.emptyList());
        when(itemImpactRepo.findByCategory("electricity-mix")).thenReturn(Collections.emptyList());
        MatchingItem item = mock(MatchingItem.class);
        when(matchingItemRepo.findAll()).thenReturn(List.of(item));
        when(item.getRefItemTarget()).thenReturn("kebab-case-format");
        List<AnalysisTableBO> result = service.getAnalysis();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAnalysis_BothErrorsAndWarnings() {
        ItemImpact impact = mock(ItemImpact.class);
        when(itemImpactRepo.findByLevel("2-Equipement")).thenReturn(List.of(impact));
        when(impact.getName()).thenReturn("equip1");
        when(impact.getLifecycleStep()).thenReturn("step1");
        when(impact.getCriterion()).thenReturn("crit1");
        when(impact.getAvgElectricityConsumption()).thenReturn(null);
        Criterion criterion = mock(Criterion.class);
        when(criterion.getCode()).thenReturn("crit1");
        when(criterionRepo.findAll()).thenReturn(List.of(criterion));
        LifecycleStep lifecycleStep = mock(LifecycleStep.class);
        when(lifecycleStep.getCode()).thenReturn("step1");
        when(lifecycleStepRepo.findAll()).thenReturn(List.of(lifecycleStep));
        when(itemImpactRepo.findByCategory("electricity-mix")).thenReturn(Collections.emptyList());

        MatchingItem item = mock(MatchingItem.class);
        when(matchingItemRepo.findAll()).thenReturn(List.of(item));
        when(item.getRefItemTarget()).thenReturn("Not-Kebab-Case");
        List<AnalysisTableBO> result = service.getAnalysis();
        assertEquals(2, result.size());
        assertEquals("ref_item_impact", result.get(0).getTable());
        assertEquals("ref_matching_item", result.get(1).getTable());
    }
}
