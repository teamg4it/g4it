/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apiindicator.modeldb.RefSustainableIndividualPackage;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferentialServiceTest {
    @Mock
    ReferentialGetService referentialGetService;
    @Mock
    RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository;
    @InjectMocks
    ReferentialService referentialService;

    @Test
    void testGetLifecycleSteps() {
        LifecycleStepRest step1 = mock(LifecycleStepRest.class);
        when(step1.getCode()).thenReturn("step1");
        LifecycleStepRest step2 = mock(LifecycleStepRest.class);
        when(step2.getCode()).thenReturn("step2");
        when(referentialGetService.getAllLifecycleSteps()).thenReturn(List.of(step1, step2));
        List<String> result = referentialService.getLifecycleSteps();
        assertEquals(List.of("step1", "step2"), result);
    }

    @Test
    void testGetActiveCriteria_All() {
        CriterionRest crit1 = mock(CriterionRest.class);
        CriterionRest crit2 = mock(CriterionRest.class);
        when(referentialGetService.getAllCriteria()).thenReturn(List.of(crit1, crit2));
        List<CriterionRest> result = referentialService.getActiveCriteria(null);
        assertEquals(2, result.size());
    }

    @Test
    void testGetActiveCriteria_Subset() {
        CriterionRest crit1 = mock(CriterionRest.class);
        when(crit1.getCode()).thenReturn("c1");
        CriterionRest crit2 = mock(CriterionRest.class);
        when(crit2.getCode()).thenReturn("c2");
        when(referentialGetService.getAllCriteria()).thenReturn(List.of(crit1, crit2));
        List<CriterionRest> result = referentialService.getActiveCriteria(List.of("c1"));
        assertEquals(1, result.size());
        assertEquals("c1", result.get(0).getCode());
    }

    @Test
    void testGetActiveCriteria_Invalid() {
        CriterionRest crit1 = mock(CriterionRest.class);
        when(crit1.getCode()).thenReturn("c1");
        when(referentialGetService.getAllCriteria()).thenReturn(List.of(crit1));
        assertNull(referentialService.getActiveCriteria(List.of("c2")));
    }

    @Test
    void testGetHypotheses_MergesOrganizationAndGlobal() {
        HypothesisRest orgHyp = mock(HypothesisRest.class);
        when(orgHyp.getCode()).thenReturn("h1");
        HypothesisRest globalHyp = mock(HypothesisRest.class);
        when(globalHyp.getCode()).thenReturn("h2");
        when(referentialGetService.getHypotheses("org")).thenReturn(List.of(orgHyp));
        when(referentialGetService.getHypotheses(null)).thenReturn(List.of(globalHyp));
        List<HypothesisRest> result = referentialService.getHypotheses("org");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(h -> h.getCode().equals("h1")));
        assertTrue(result.stream().anyMatch(h -> h.getCode().equals("h2")));
    }

    @Test
    void testGetMatchingItem_OrgThenGlobal() {
        MatchingItemRest globalItem = mock(MatchingItemRest.class);
        when(referentialGetService.getMatchingItem("model", "org")).thenReturn(null);
        when(referentialGetService.getMatchingItem("model", null)).thenReturn(globalItem);
        assertEquals(globalItem, referentialService.getMatchingItem("model", "org"));
    }

    @Test
    void testGetItemType_OrgThenGlobal() {
        ItemTypeRest globalType = mock(ItemTypeRest.class);
        when(referentialGetService.getItemTypes("type", "org")).thenReturn(List.of());
        when(referentialGetService.getItemTypes("type", null)).thenReturn(List.of(globalType));
        assertEquals(globalType, referentialService.getItemType("type", "org"));
    }

    @Test
    void testGetItemImpacts_OrgThenGlobalAndElectricityMix() {
        ItemImpactRest orgImpact = mock(ItemImpactRest.class);
        ItemImpactRest elecMixImpact = mock(ItemImpactRest.class);
        when(referentialGetService.getItemImpacts("crit", "step", "name", null, null, "org"))
                .thenReturn(List.of(orgImpact));
        when(referentialGetService.getItemImpacts("crit", null, null, "loc", "electricity-mix", "org"))
                .thenReturn(List.of(elecMixImpact));
        List<ItemImpactRest> result = referentialService.getItemImpacts("crit", "step", "name", "loc", "org");
        assertTrue(result.contains(orgImpact));
        assertTrue(result.contains(elecMixImpact));
    }

    @Test
    void testGetSipValueMap() {
        // Use a criteria label that matches the transformation logic
        RefSustainableIndividualPackage sip = mock(RefSustainableIndividualPackage.class);
        when(sip.getCriteria()).thenReturn("Climate change"); // label, not key
        when(sip.getIndividualSustainablePackage()).thenReturn(42.0);
        when(refSustainableIndividualPackageRepository.findAll()).thenReturn(List.of(sip));
        List<String> activeCriteria = List.of("climate-change"); // key
        Map<String, Double> result = referentialService.getSipValueMap(activeCriteria);
        assertEquals(1, result.size());
        assertEquals(42.0, result.get(StringUtils.kebabToSnakeCase("climate-change")));
    }

    @Test
    void testGetElectricityMixQuartiles() {
        ItemImpact mix1 = mock(ItemImpact.class);
        when(mix1.getCriterion()).thenReturn("crit");
        when(mix1.getValue()).thenReturn(10.0);
        when(mix1.getLocation()).thenReturn("loc1");
        ItemImpact mix2 = mock(ItemImpact.class);
        when(mix2.getCriterion()).thenReturn("crit");
        when(mix2.getValue()).thenReturn(20.0);
        when(mix2.getLocation()).thenReturn("loc2");
        when(referentialGetService.getElectricityMix()).thenReturn(List.of(mix1, mix2));
        Map<Pair<String, String>, Integer> result = referentialService.getElectricityMixQuartiles();
        assertNotNull(result);
        assertTrue(result.containsKey(Pair.of("loc1", "crit")));
        assertTrue(result.containsKey(Pair.of("loc2", "crit")));
    }
}
