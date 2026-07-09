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
        MatchingItemRest orgItem = mock(MatchingItemRest.class);
        when(referentialGetService.getMatchingItem("model", "org", null)).thenReturn(orgItem);
        assertEquals(orgItem, referentialService.getMatchingItem("model", "org"));
    }


    @Test
    void testGetItemType_OrgThenGlobal() {
        ItemTypeRest orgType = mock(ItemTypeRest.class);
        when(referentialGetService.getItemTypes("type", "org", null)).thenReturn(List.of(orgType));
        assertEquals(orgType, referentialService.getItemType("type", "org"));
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
        when(referentialGetService.getElectricityMix(anyLong())).thenReturn(List.of(mix1, mix2));
        Map<Pair<String, String>, Integer> result = referentialService.getElectricityMixQuartiles(anyLong());
        assertNotNull(result);
        assertTrue(result.containsKey(Pair.of("loc1", "crit")));
        assertTrue(result.containsKey(Pair.of("loc2", "crit")));
    }

    @Test
    void testGetItemImpactsForWorkspace_WithGeneralAndElectricityMix() {
        ItemImpactRest impact1 = mock(ItemImpactRest.class);
        ItemImpactRest impact2 = mock(ItemImpactRest.class);

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", "step", "name",
                null, null, null, 1L))
                .thenReturn(new ArrayList<>(List.of(impact1)));

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", null, null,
                "FR", "electricity-mix", null, 1L))
                .thenReturn(List.of(impact2));

        List<ItemImpactRest> result =
                referentialService.getItemImpactsForWorkspace(
                        "crit", "step", "name", "FR", 1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(impact1));
        assertTrue(result.contains(impact2));
    }

    @Test
    void testGetItemImpactsForWorkspace_WhenGeneralImpactsNull() {
        ItemImpactRest electricityMix = mock(ItemImpactRest.class);

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", "step", "name",
                null, null, null, 1L))
                .thenReturn(null);

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", null, null,
                "FR", "electricity-mix", null, 1L))
                .thenReturn(List.of(electricityMix));

        List<ItemImpactRest> result =
                referentialService.getItemImpactsForWorkspace(
                        "crit", "step", "name", "FR", 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(electricityMix, result.get(0));
    }

    /*@Test
    void testGetItemImpactsForWorkspace_WhenNoElectricityMix() {
        ItemImpactRest impact = mock(ItemImpactRest.class);

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", "step", "name",
                null, null, null, 1L))
                .thenReturn(List.of(impact));

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", null, null,
                "FR", "electricity-mix", null, 1L))
                .thenReturn(Collections.emptyList());

        // Fallback query used by service when workspace electricity mix is empty.
        when(referentialGetService.getItemImpacts(
                "crit", null, null,
                "FR", "electricity-mix", null))
                .thenReturn(Collections.emptyList());

        List<ItemImpactRest> result =
                referentialService.getItemImpactsForWorkspace(
                        "crit", "step", "name", "FR", 1L);

        assertEquals(1, result.size());
        assertEquals(impact, result.get(0));
    }*/

    @Test
    void testGetItemTypeForWorkspace_ReturnsFirstItemType() {
        ItemTypeRest itemType = mock(ItemTypeRest.class);

        when(referentialGetService.getItemTypesForWorkspace("Laptop", 1L))
                .thenReturn(List.of(itemType));

        ItemTypeRest result =
                referentialService.getItemTypeForWorkspace("Laptop", 1L);

        assertEquals(itemType, result);
    }

    @Test
    void testGetItemTypeForWorkspace_ReturnsNullWhenEmpty() {
        when(referentialGetService.getItemTypesForWorkspace("Laptop", 1L))
                .thenReturn(Collections.emptyList());

        ItemTypeRest result =
                referentialService.getItemTypeForWorkspace("Laptop", 1L);

        assertNull(result);
    }

    @Test
    void testGetItemTypeForWorkspace_ReturnsNullWhenListNull() {
        when(referentialGetService.getItemTypesForWorkspace("Laptop", 1L))
                .thenReturn(null);

        ItemTypeRest result =
                referentialService.getItemTypeForWorkspace("Laptop", 1L);

        assertNull(result);
    }

    @Test
    void testGetMatchingItemForWorkspace() {
        MatchingItemRest matchingItem = mock(MatchingItemRest.class);

        when(referentialGetService.getMatchingItemForWorkspace("model1", 1L))
                .thenReturn(matchingItem);

        MatchingItemRest result =
                referentialService.getMatchingItemForWorkspace("model1", 1L);

        assertEquals(matchingItem, result);
    }

    @Test
    void testGetMatchingItemForWorkspace_ReturnsNull() {
        when(referentialGetService.getMatchingItemForWorkspace("model1", 1L))
                .thenReturn(null);

        MatchingItemRest result =
                referentialService.getMatchingItemForWorkspace("model1", 1L);

        assertNull(result);
    }

    @Test
    void testGetElectricityMixQuartiles_ForWorkspace() {
        ItemImpact impact1 = mock(ItemImpact.class);
        when(impact1.getCriterion()).thenReturn("criterion");
        when(impact1.getLocation()).thenReturn("FR");
        when(impact1.getValue()).thenReturn(10.0);

        ItemImpact impact2 = mock(ItemImpact.class);
        when(impact2.getCriterion()).thenReturn("criterion");
        when(impact2.getLocation()).thenReturn("UK");
        when(impact2.getValue()).thenReturn(20.0);

        ItemImpact impact3 = mock(ItemImpact.class);
        when(impact3.getCriterion()).thenReturn("criterion");
        when(impact3.getLocation()).thenReturn("DE");
        when(impact3.getValue()).thenReturn(30.0);

        ItemImpact impact4 = mock(ItemImpact.class);
        when(impact4.getCriterion()).thenReturn("criterion");
        when(impact4.getLocation()).thenReturn("ES");
        when(impact4.getValue()).thenReturn(40.0);

        when(referentialGetService.getElectricityMix(1L))
                .thenReturn(List.of(impact1, impact2, impact3, impact4));

        Map<Pair<String, String>, Integer> result =
                referentialService.getElectricityMixQuartiles(1L);

        assertNotNull(result);
        assertEquals(4, result.size());

        assertTrue(result.containsKey(Pair.of("FR", "criterion")));
        assertTrue(result.containsKey(Pair.of("UK", "criterion")));
        assertTrue(result.containsKey(Pair.of("DE", "criterion")));
        assertTrue(result.containsKey(Pair.of("ES", "criterion")));
    }

    @Test
    void testGetElectricityMixQuartiles_FiltersNullValue() {
        ItemImpact invalid = mock(ItemImpact.class);
        when(invalid.getCriterion()).thenReturn("criterion");
        when(invalid.getValue()).thenReturn(null);
        when(invalid.getLocation()).thenReturn("FR");

        when(referentialGetService.getElectricityMix(1L))
                .thenReturn(List.of(invalid));

        Map<Pair<String, String>, Integer> result =
                referentialService.getElectricityMixQuartiles(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetItemImpactsForWorkspace_WhenGeneralImpactsEmpty_UsesGlobalFallback() {
        ItemImpactRest globalImpact = mock(ItemImpactRest.class);
        ItemImpactRest workspaceElectricityMix = mock(ItemImpactRest.class);

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", "step", "name",
                null, null, null, 1L))
                .thenReturn(Collections.emptyList());

        when(referentialGetService.getItemImpacts(
                "crit", "step", "name",
                null, null, null))
                .thenReturn(List.of(globalImpact));

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", null, null,
                "FR", "electricity-mix", null, 1L))
                .thenReturn(List.of(workspaceElectricityMix));

        List<ItemImpactRest> result =
                referentialService.getItemImpactsForWorkspace(
                        "crit", "step", "name", "FR", 1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(globalImpact));
        assertTrue(result.contains(workspaceElectricityMix));
    }

    @Test
    void testGetItemImpactsForWorkspace_WhenElectricityMixNull_UsesGlobalFallback() {
        ItemImpactRest workspaceImpact = mock(ItemImpactRest.class);
        ItemImpactRest globalElectricityMix = mock(ItemImpactRest.class);

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", "step", "name",
                null, null, null, 1L))
                .thenReturn(List.of(workspaceImpact));

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", null, null,
                "FR", "electricity-mix", null, 1L))
                .thenReturn(null);

        when(referentialGetService.getItemImpacts(
                "crit", null, null,
                "FR", "electricity-mix", null))
                .thenReturn(List.of(globalElectricityMix));

        List<ItemImpactRest> result =
                referentialService.getItemImpactsForWorkspace(
                        "crit", "step", "name", "FR", 1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(workspaceImpact));
        assertTrue(result.contains(globalElectricityMix));
    }

    @Test
    void testGetItemImpactsForWorkspace_WhenWorkspaceDataMissing_UsesBothGlobalFallbacks() {
        ItemImpactRest globalImpact = mock(ItemImpactRest.class);
        ItemImpactRest globalElectricityMix = mock(ItemImpactRest.class);

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", "step", "name",
                null, null, null, 1L))
                .thenReturn(null);

        when(referentialGetService.getItemImpacts(
                "crit", "step", "name",
                null, null, null))
                .thenReturn(List.of(globalImpact));

        when(referentialGetService.getItemImpactsForWorkspace(
                "crit", null, null,
                "FR", "electricity-mix", null, 1L))
                .thenReturn(Collections.emptyList());

        when(referentialGetService.getItemImpacts(
                "crit", null, null,
                "FR", "electricity-mix", null))
                .thenReturn(List.of(globalElectricityMix));

        List<ItemImpactRest> result =
                referentialService.getItemImpactsForWorkspace(
                        "crit", "step", "name", "FR", 1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(globalImpact));
        assertTrue(result.contains(globalElectricityMix));

        verify(referentialGetService).getItemImpacts(
                "crit", "step", "name",
                null, null, null);
        verify(referentialGetService).getItemImpacts(
                "crit", null, null,
                "FR", "electricity-mix", null);
    }
}
