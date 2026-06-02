/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.modeldb.MatchingItem;
import com.soprasteria.g4it.backend.apireferential.repository.*;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferentialGetServiceTest {

    @Mock
    private CriterionRepository criterionRepository;

    @Mock
    private LifecycleStepRepository lifecycleStepRepository;

    @Mock
    private ItemImpactRepository itemImpactRepository;

    @Mock
    private MatchingItemRepository matchingItemRepository;

    @Mock
    private HypothesisRepository hypothesisRepository;

    @Mock
    private ItemTypeRepository itemTypeRepository;

    @Mock
    private ReferentialMapper refRestMapper;

    @InjectMocks
    private ReferentialGetService referentialGetService;

    @Test
    void getAllLifecycleSteps_returnsMappedLifecycleSteps() {

        List<?> entities = List.of(mock(Object.class));
        List<LifecycleStepRest> expected =
                List.of(new LifecycleStepRest());

        when(lifecycleStepRepository.findAll()).thenReturn((List) entities);
        when(refRestMapper.toLifecycleRest((List) entities))
                .thenReturn(expected);

        List<LifecycleStepRest> result =
                referentialGetService.getAllLifecycleSteps();

        assertEquals(expected, result);

        verify(lifecycleStepRepository).findAll();
        verify(refRestMapper).toLifecycleRest((List) entities);
    }

    @Test
    void getAllCriteria_returnsMappedCriteria() {

        List<?> entities = List.of(mock(Object.class));
        List<CriterionRest> expected =
                List.of(new CriterionRest());

        when(criterionRepository.findAll()).thenReturn((List) entities);
        when(refRestMapper.toCriteriaRest((List) entities))
                .thenReturn(expected);

        List<CriterionRest> result =
                referentialGetService.getAllCriteria();

        assertEquals(expected, result);
    }

    @Test
    void getHypotheses_returnsMappedHypotheses() {

        String organization = "ORG";

        List<?> entities = List.of(mock(Object.class));
        List<HypothesisRest> expected =
                List.of(new HypothesisRest());

        when(hypothesisRepository.findByOrganization(organization))
                .thenReturn((List) entities);

        when(refRestMapper.toHypothesesRest((List) entities))
                .thenReturn(expected);

        List<HypothesisRest> result =
                referentialGetService.getHypotheses(organization);

        assertEquals(expected, result);
    }

    @Test
    void getItemTypes_returnsAllItemTypes_whenTypeIsNull() {

        String organization = "ORG";
        Long workspaceId = 1L;

        List<ItemType> itemTypes =
                List.of(ItemType.builder().type("Laptop").build());

        List<ItemTypeRest> expected =
                List.of(new ItemTypeRest());

        when(itemTypeRepository.findByOrganizationAndWorkspaceId(
                organization,
                workspaceId))
                .thenReturn(itemTypes);

        when(refRestMapper.toItemTypeRest(itemTypes))
                .thenReturn(expected);

        List<ItemTypeRest> result =
                referentialGetService.getItemTypes(
                        null,
                        organization,
                        workspaceId);

        assertEquals(expected, result);
    }

    @Test
    void getItemTypes_returnsSpecificType_whenTypeExists() {

        String type = "Laptop";
        String organization = "ORG";
        Long workspaceId = 1L;

        ItemType itemType =
                ItemType.builder().type(type).build();

        List<ItemTypeRest> expected =
                List.of(new ItemTypeRest());

        when(itemTypeRepository.findByTypeAndOrganizationAndWorkspaceId(
                type,
                organization,
                workspaceId))
                .thenReturn(Optional.of(itemType));

        when(refRestMapper.toItemTypeRest(anyList()))
                .thenReturn(expected);

        List<ItemTypeRest> result =
                referentialGetService.getItemTypes(
                        type,
                        organization,
                        workspaceId);

        assertEquals(expected, result);
    }

    @Test
    void getItemTypes_returnsEmptyList_whenTypeNotFound() {

        when(itemTypeRepository.findByTypeAndOrganizationAndWorkspaceId(
                anyString(),
                anyString(),
                anyLong()))
                .thenReturn(Optional.empty());

        when(refRestMapper.toItemTypeRest(anyList()))
                .thenReturn(Collections.emptyList());

        List<ItemTypeRest> result =
                referentialGetService.getItemTypes(
                        "Laptop",
                        "ORG",
                        1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getMatchingItem_returnsMappedItem_whenFound() {

        MatchingItem item =
                MatchingItem.builder()
                        .itemSource("Dell")
                        .build();

        MatchingItemRest expected =
                new MatchingItemRest();

        when(matchingItemRepository
                .findByItemSourceAndOrganizationAndWorkspaceId(
                        "Dell",
                        "ORG",
                        1L))
                .thenReturn(Optional.of(item));

        when(refRestMapper.toMatchingItemRest(item))
                .thenReturn(expected);

        MatchingItemRest result =
                referentialGetService.getMatchingItem(
                        "Dell",
                        "ORG",
                        1L);

        assertEquals(expected, result);
    }

    @Test
    void getMatchingItem_returnsNull_whenNotFound() {

        when(matchingItemRepository
                .findByItemSourceAndOrganizationAndWorkspaceId(
                        anyString(),
                        anyString(),
                        anyLong()))
                .thenReturn(Optional.empty());

        assertNull(
                referentialGetService.getMatchingItem(
                        "Dell",
                        "ORG",
                        1L));
    }

    @Test
    void getItemImpacts_returnsMappedImpacts() {

        List<ItemImpact> impacts =
                List.of(ItemImpact.builder().build());

        List<ItemImpactRest> expected =
                List.of(new ItemImpactRest());

        when(itemImpactRepository
                .findByCriterionAndLifecycleStepAndNameAndCategoryAndLocationAndOrganization(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()))
                .thenReturn(impacts);

        when(refRestMapper.toItemImpactRest(impacts))
                .thenReturn(expected);

        List<ItemImpactRest> result =
                referentialGetService.getItemImpacts(
                        "climate-change",
                        "manufacturing",
                        "Laptop",
                        "France",
                        "IT",
                        "ORG");

        assertEquals(expected, result);
    }

    @Test
    void getCountries_returnsSortedCountries() {

        when(itemImpactRepository.findCountries("ORG"))
                .thenReturn(List.of("Germany", "France"));

        List<String> result =
                referentialGetService.getCountries("ORG");

        assertEquals(
                List.of("France", "Germany"),
                result);
    }

    @Test
    void getElectricityMix_returnsWorkspaceSpecificData() {

        List<ItemImpact> impacts =
                List.of(ItemImpact.builder().build());

        when(itemImpactRepository
                .findByCategoryAndWorkspaceId(
                        "electricity-mix",
                        1L))
                .thenReturn(impacts);

        List<ItemImpact> result =
                referentialGetService.getElectricityMix(1L);

        assertEquals(impacts, result);
    }

    @Test
    void getElectricityMix_fallsBackToGlobalData() {

        List<ItemImpact> fallback =
                List.of(ItemImpact.builder().build());

        when(itemImpactRepository
                .findByCategoryAndWorkspaceId(
                        "electricity-mix",
                        1L))
                .thenReturn(Collections.emptyList());

        when(itemImpactRepository
                .findByCategoryAndWorkspaceId(
                        "electricity-mix",
                        null))
                .thenReturn(fallback);

        List<ItemImpact> result =
                referentialGetService.getElectricityMix(1L);

        assertEquals(fallback, result);
    }

    @Test
    void countItemImpactsForWorkspace_returnsCount() {

        when(itemImpactRepository.countByWorkspaceId(1L))
                .thenReturn(123L);

        long result =
                referentialGetService.countItemImpactsForWorkspace(1L);

        assertEquals(123L, result);
    }

    @Test
    void getItemTypesForWorkspace_whenTypeNull_returnsAllTypes() {

        Long workspaceId = 1L;

        List<ItemType> itemTypes =
                List.of(ItemType.builder().type("Laptop").build());

        List<ItemTypeRest> expected =
                List.of(new ItemTypeRest());

        when(itemTypeRepository.findByOrganizationAndWorkspaceId(null, workspaceId))
                .thenReturn(itemTypes);

        when(refRestMapper.toItemTypeRest(itemTypes))
                .thenReturn(expected);

        List<ItemTypeRest> result =
                referentialGetService.getItemTypesForWorkspace(null, workspaceId);

        assertEquals(expected, result);
    }

    @Test
    void getItemTypesForWorkspace_whenTypeExists() {

        Long workspaceId = 1L;

        ItemType itemType =
                ItemType.builder()
                        .type("Laptop")
                        .build();

        List<ItemTypeRest> expected =
                List.of(new ItemTypeRest());

        when(itemTypeRepository.findByTypeAndOrganizationAndWorkspaceId(
                "Laptop",
                null,
                workspaceId))
                .thenReturn(Optional.of(itemType));

        when(refRestMapper.toItemTypeRest(anyList()))
                .thenReturn(expected);

        List<ItemTypeRest> result =
                referentialGetService.getItemTypesForWorkspace(
                        "Laptop",
                        workspaceId);

        assertEquals(expected, result);
    }

    @Test
    void getMatchingItemForWorkspace_whenFound() {

        Long workspaceId = 1L;

        MatchingItem item =
                MatchingItem.builder()
                        .itemSource("Dell")
                        .build();

        MatchingItemRest expected =
                new MatchingItemRest();

        when(matchingItemRepository.findByItemSourceAndOrganizationAndWorkspaceId(
                "Dell",
                null,
                workspaceId))
                .thenReturn(Optional.of(item));

        when(refRestMapper.toMatchingItemRest(item))
                .thenReturn(expected);

        MatchingItemRest result =
                referentialGetService.getMatchingItemForWorkspace(
                        "Dell",
                        workspaceId);

        assertEquals(expected, result);
    }

    @Test
    void getMatchingItemForWorkspace_whenNotFound() {

        when(matchingItemRepository.findByItemSourceAndOrganizationAndWorkspaceId(
                anyString(),
                isNull(),
                anyLong()))
                .thenReturn(Optional.empty());

        MatchingItemRest result =
                referentialGetService.getMatchingItemForWorkspace(
                        "Dell",
                        1L);

        assertNull(result);
    }

    @Test
    void getItemImpactsForWorkspace_returnsMappedImpacts() {

        List<ItemImpact> impacts =
                List.of(ItemImpact.builder().build());

        List<ItemImpactRest> expected =
                List.of(new ItemImpactRest());

        when(itemImpactRepository
                .findByCriterionAndLifecycleStepAndNameAndCategoryAndLocationAndOrganizationAndWorkspaceId(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyLong()))
                .thenReturn(impacts);

        when(refRestMapper.toItemImpactRest(impacts))
                .thenReturn(expected);

        List<ItemImpactRest> result =
                referentialGetService.getItemImpactsForWorkspace(
                        "climate-change",
                        "manufacturing",
                        "Laptop",
                        "France",
                        "IT",
                        "ORG",
                        1L);

        assertEquals(expected, result);
    }

    @Test
    void getItemImpactsELectricityMixForWorkspace_returnsValueFromMap() {

        Long workspaceId = 1L;

        List<ItemImpactRest> expected =
                List.of(new ItemImpactRest());

        String key =
                StringUtils.kebabToSnakeCase("climate-change")
                        + "|electricity-mix|France|"
                        + workspaceId;

        Map<String, List<ItemImpactRest>> map = new HashMap<>();
        map.put(key, expected);

        List<ItemImpactRest> result =
                referentialGetService.getItemImpactsELectricityMixForWorkspace(
                        "climate-change",
                        "France",
                        workspaceId,
                        map);

        assertSame(expected, result);
    }

    @Test
    void getItemImpactsELectricityMixForWorkspace_returnsEmptyListWhenKeyMissing() {

        List<ItemImpactRest> result =
                referentialGetService.getItemImpactsELectricityMixForWorkspace(
                        "climate-change",
                        "France",
                        1L,
                        new HashMap<>());

        assertTrue(result.isEmpty());
    }

    @Test
    void bulkGetMatchingItemsForWorkspace_returnsMap() {

        Long workspaceId = 1L;

        MatchingItem item =
                MatchingItem.builder()
                        .itemSource("Dell")
                        .build();

        MatchingItemRest rest =
                new MatchingItemRest();

        when(matchingItemRepository.findByItemSourceInAndWorkspaceId(
                anySet(),
                eq(workspaceId)))
                .thenReturn(List.of(item));

        when(refRestMapper.toMatchingItemRest(item))
                .thenReturn(rest);

        Map<String, MatchingItemRest> result =
                referentialGetService.bulkGetMatchingItemsForWorkspace(
                        Set.of("Dell"),
                        workspaceId);

        assertEquals(1, result.size());
        assertEquals(rest, result.get("Dell|1"));
    }

    @Test
    void bulkGetItemTypesForWorkspace_returnsMap() {

        Long workspaceId = 1L;

        ItemType item =
                ItemType.builder()
                        .type("Laptop")
                        .build();

        ItemTypeRest rest =
                new ItemTypeRest();

        when(itemTypeRepository.findByTypeInAndWorkspaceId(
                anySet(),
                eq(workspaceId)))
                .thenReturn(List.of(item));

        when(refRestMapper.toItemTypeRest(anyList()))
                .thenReturn(List.of(rest));

        Map<String, List<ItemTypeRest>> result =
                referentialGetService.bulkGetItemTypesForWorkspace(
                        Set.of("Laptop"),
                        workspaceId);

        assertEquals(1, result.size());
        assertEquals(List.of(rest), result.get("Laptop|1"));
    }

    @Test
    void bulkGetItemImpactsForWorkspace_returnsMap() {

        Long workspaceId = 1L;

        ItemImpact impact =
                ItemImpact.builder()
                        .criterion("criterion")
                        .lifecycleStep("step")
                        .name("Laptop")
                        .build();

        ItemImpactRest rest =
                new ItemImpactRest();

        when(itemImpactRepository
                .findByCriterionInAndLifecycleStepInAndWorkspaceId(
                        anySet(),
                        anySet(),
                        eq(workspaceId)))
                .thenReturn(List.of(impact));

        when(refRestMapper.toItemImpactRest(anyList()))
                .thenReturn(List.of(rest));

        Map<String, List<ItemImpactRest>> result =
                referentialGetService.bulkGetItemImpactsForWorkspace(
                        Set.of("criterion"),
                        Set.of("step"),
                        workspaceId);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("criterion|step|Laptop|1"));
    }

    @Test
    void bulkGetItemImpactsElectricityMixForWorkspace_returnsMap() {

        Long workspaceId = 1L;

        ItemImpact impact =
                ItemImpact.builder()
                        .criterion("criterion")
                        .category("electricity-mix")
                        .location("France")
                        .build();

        ItemImpactRest rest =
                new ItemImpactRest();

        when(itemImpactRepository
                .findByCriterionInAndCategoryAndLocationInAndWorkspaceId(
                        anySet(),
                        eq("electricity-mix"),
                        anySet(),
                        eq(workspaceId)))
                .thenReturn(List.of(impact));

        when(refRestMapper.toItemImpactRest(anyList()))
                .thenReturn(List.of(rest));

        Map<String, List<ItemImpactRest>> result =
                referentialGetService.bulkGetItemImpactsElectricityMixForWorkspace(
                        Set.of("criterion"),
                        Set.of("France"),
                        workspaceId);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(
                "criterion|electricity-mix|France|1"));
    }

    @Test
    void bulkGetAllItemImpactsForWorkspace_mergesMaps() {

        ReferentialGetService spyService =
                spy(referentialGetService);

        ItemImpactRest general =
                new ItemImpactRest();

        ItemImpactRest electricity =
                new ItemImpactRest();

        Map<String, List<ItemImpactRest>> generalMap =
                Map.of(
                        "key",
                        new ArrayList<>(List.of(general)));

        Map<String, List<ItemImpactRest>> electricityMap =
                Map.of(
                        "key",
                        new ArrayList<>(List.of(electricity)));

        doReturn(generalMap)
                .when(spyService)
                .bulkGetItemImpactsForWorkspace(
                        anySet(),
                        anySet(),
                        anyLong());

        doReturn(electricityMap)
                .when(spyService)
                .bulkGetItemImpactsElectricityMixForWorkspace(
                        anySet(),
                        anySet(),
                        anyLong());

        Map<String, List<ItemImpactRest>> result =
                spyService.bulkGetAllItemImpactsForWorkspace(
                        Set.of("c"),
                        Set.of("s"),
                        Set.of("France"),
                        1L);

        assertEquals(2, result.get("key").size());
    }


}
