/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils;
import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.modeldb.MatchingItem;
import com.soprasteria.g4it.backend.apireferential.repository.*;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Referential get service
 */
@Service
@NoArgsConstructor
@Slf4j
public class ReferentialGetService {

    @Autowired
    private CriterionRepository criterionRepository;
    @Autowired
    private LifecycleStepRepository lifecycleStepRepository;
    @Autowired
    private ItemImpactRepository itemImpactRepository;
    @Autowired
    private MatchingItemRepository matchingItemRepository;
    @Autowired
    private HypothesisRepository hypothesisRepository;
    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private ReferentialMapper refRestMapper;

    /**
     * Get all referential lifecycle steps
     *
     * @return list of all the lifecycle steps
     */
    @Cacheable("ref_getAllLifecycleSteps")
    public List<LifecycleStepRest> getAllLifecycleSteps() {
        return refRestMapper.toLifecycleRest(lifecycleStepRepository.findAll());
    }

    /**
     * Get all referential criteria
     *
     * @return list of all the criteria
     */
    @Cacheable("ref_getAllCriteria")
    public List<CriterionRest> getAllCriteria() {
        return refRestMapper.toCriteriaRest(criterionRepository.findAll());
    }

    /**
     * Get the referential Hypothesis by
     * code and/or organization (mapped to subscriber column in table)
     *
     * @param organization the organization
     * @return list of hypotheses
     */
    @Cacheable("ref_getHypotheses")
    public List<HypothesisRest> getHypotheses(String organization) {
        return refRestMapper.toHypothesesRest(hypothesisRepository.findByOrganization(organization));
    }

    /**
     * Get the referential ItemTypes by type
     * and/or organization (mapped to subscriber column in table)
     *
     * @return the of itemTypes
     */
    @Cacheable("ref_getItemTypes")
    public List<ItemTypeRest> getItemTypes(String type, String organization,Long workspaceId) {

        // get all itemTypes
        if (type == null) {
            return refRestMapper.toItemTypeRest(itemTypeRepository.findByOrganizationAndWorkspaceId(organization,workspaceId));
        }

        Optional<ItemType> itemType = itemTypeRepository.findByTypeAndOrganizationAndWorkspaceId(type, organization,workspaceId);

        return refRestMapper.toItemTypeRest(itemType.map(List::of).orElseGet(List::of));
    }

    /**
     * Get the referential MatchingItem by model
     * and/or organization(mapped to subscriber column in table)
     *
     * @return matchingItem
     */
    @Cacheable("ref_getMatchingItem")
    public MatchingItemRest getMatchingItem(String model, String organization, Long workspaceId) {
        return matchingItemRepository.findByItemSourceAndOrganizationAndWorkspaceId(model, organization,workspaceId)
                .map(item -> refRestMapper.toMatchingItemRest(item)).orElse(null);
    }


    /**
     * Get the referential item impacts
     *
     * @param organization the organization(mapped to subscriber column in table)
     * @return list of item impacts
     */
    @Cacheable("ref_getItemImpacts")
    public List<ItemImpactRest> getItemImpacts(String criterion, String lifecycleStep,
                                               String name, String location,
                                               String category, String organization) {

        List<ItemImpact> itemImpacts = itemImpactRepository.findByCriterionAndLifecycleStepAndNameAndCategoryAndLocationAndOrganization(
                StringUtils.kebabToSnakeCase(criterion), LifecycleStepUtils.get(lifecycleStep, lifecycleStep), name, category, location, organization);
        return refRestMapper.toItemImpactRest(itemImpacts);
    }

    /**
     * Get the referential item impacts
     * @param organization the organization (mapped to subscriber column in table)
     * @return list of item impacts
     */
    @Cacheable("ref_getCountries")
    public List<String> getCountries(String organization) {
        return itemImpactRepository.findCountries(organization).stream().sorted().toList();
    }

    /**
     * Get the referential item impacts filtered by electricity mix
     *
     * @return list of item impacts
     */
    public List<ItemImpact> getElectricityMix(Long workspaceId) {
        List<ItemImpact> itemImpactList= itemImpactRepository.findByCategoryAndWorkspaceId("electricity-mix",workspaceId);
        if(itemImpactList!=null && !itemImpactList.isEmpty())
            return itemImpactList;
        return itemImpactRepository.findByCategoryAndWorkspaceId("electricity-mix",null);
    }

    /*public List<ItemTypeRest> getItemTypesForWorkspace(String type, Long workspaceId, Map<String, List<ItemTypeRest>> itemTypeMap) {

        // get all itemTypes
        if (type == null) {
            return itemTypeMap.values().stream()
                    .flatMap(Collection::stream)
                    .toList();
        }else{
            String key = buildTypeKey(type, workspaceId);
            if (itemTypeMap != null && !itemTypeMap.isEmpty() && itemTypeMap.containsKey(key)) {
                return itemTypeMap.get(key);
            }
            return List.of();
        }
    }*/
    @Cacheable(value = "ref_getItemTypes", key = "#type + '|' + #workspaceId")
    public List<ItemTypeRest> getItemTypesForWorkspace(String type, Long workspaceId) {
        if (type == null) {
            return refRestMapper.toItemTypeRest(itemTypeRepository.findByOrganizationAndWorkspaceId(null,workspaceId));
        }
        Optional<ItemType> itemType = itemTypeRepository.findByTypeAndOrganizationAndWorkspaceId(type, null,workspaceId);
        return refRestMapper.toItemTypeRest(itemType.map(List::of).orElseGet(List::of));
    }

    /*public MatchingItemRest getMatchingItemForWorkspace(String model, Long workspaceId,Map<String, MatchingItemRest> matchingItemMap) {
        String key = buildModelKey(model, workspaceId);
        if (matchingItemMap != null && !matchingItemMap.isEmpty() && matchingItemMap.containsKey(key)) {
            return matchingItemMap.get(key);
        }
        return null;
    }*/
    @Cacheable(value = "ref_getMatchingItem", key = "#model + '|' + #workspaceId")
    public MatchingItemRest getMatchingItemForWorkspace(String model, Long workspaceId) {
        return matchingItemRepository.findByItemSourceAndOrganizationAndWorkspaceId(model, null,workspaceId)
                .map(item -> refRestMapper.toMatchingItemRest(item)).orElse(null);
    }

    /*public List<ItemImpactRest> getItemImpactsForWorkspace(String criterion, String lifecycleStep,
                                               String name, Long workspaceId,Map<String, List<ItemImpactRest>> itemImpactMap) {

        String key = buildImpactKey(
                StringUtils.kebabToSnakeCase(criterion),
                LifecycleStepUtils.get(lifecycleStep, lifecycleStep),
                name,
                workspaceId
        );
        // Assuming you have a pre-fetched map of item impacts for the workspace
         if (itemImpactMap != null && !itemImpactMap.isEmpty() && itemImpactMap.containsKey(key)) {
             return itemImpactMap.get(key);
         }
        return List.of();
    }*/
    @Cacheable(value = "ref_getItemImpacts", key = "#criterion + '|' + #lifecycleStep + '|' + #name + '|' + #location + '|' + #category + '|' + #organization + '|' + #workspaceId")
    public List<ItemImpactRest> getItemImpactsForWorkspace(String criterion, String lifecycleStep,
                                                           String name, String location,
                                                           String category, String organization, Long workspaceId) {
        List<ItemImpact> itemImpacts = itemImpactRepository.findByCriterionAndLifecycleStepAndNameAndCategoryAndLocationAndOrganizationAndWorkspaceId(
                StringUtils.kebabToSnakeCase(criterion), LifecycleStepUtils.get(lifecycleStep, lifecycleStep), name, category, location, organization, workspaceId);
        return refRestMapper.toItemImpactRest(itemImpacts);
    }


    public List<ItemImpactRest> getItemImpactsELectricityMixForWorkspace(String criterion,String location,
                                                           Long workspaceId,Map<String, List<ItemImpactRest>> itemImpactElectricityMap) {

        String key = buildElectricityMixImpactKey(
                StringUtils.kebabToSnakeCase(criterion),
                "electricity-mix",
                location,
                workspaceId
        );
        // Assuming you have a pre-fetched map of item impacts for the workspace
        if (itemImpactElectricityMap != null && itemImpactElectricityMap.containsKey(key)) {
            return itemImpactElectricityMap.get(key);
        }
        return List.of();
    }

    public Map<String, MatchingItemRest> bulkGetMatchingItemsForWorkspace(Set<String> models, Long workspaceId) {
        List<MatchingItem> items = matchingItemRepository.findByItemSourceInAndWorkspaceId(models, workspaceId);
        Map<String, MatchingItemRest> map = new HashMap<>();
        for (MatchingItem item : items) {
            String key = buildModelKey(item.getItemSource(), workspaceId);
            map.put(key, refRestMapper.toMatchingItemRest(item));
        }
        return map;
    }

    public Map<String, List<ItemTypeRest>> bulkGetItemTypesForWorkspace(Set<String> types, Long workspaceId) {
        List<ItemType> items = itemTypeRepository.findByTypeInAndWorkspaceId(types, workspaceId);
        Map<String, List<ItemTypeRest>> map = new HashMap<>();
        for (ItemType item : items) {
            String key = buildTypeKey(item.getType(), workspaceId);
            map.put(key, refRestMapper.toItemTypeRest(List.of(item)));
        }
        return map;
    }

    public Map<String, List<ItemImpactRest>> bulkGetAllItemImpactsForWorkspace(
            Set<String> criteria,
            Set<String> lifecycleSteps,
            Set<String> locations,
            Long workspaceId) {
        Map<String, List<ItemImpactRest>> generalImpacts = bulkGetItemImpactsForWorkspace(criteria, lifecycleSteps, workspaceId);
        Map<String, List<ItemImpactRest>> electricityMixImpacts = bulkGetItemImpactsElectricityMixForWorkspace(criteria, locations, workspaceId);

        Map<String, List<ItemImpactRest>> result = new HashMap<>(generalImpacts);
        for (Map.Entry<String, List<ItemImpactRest>> entry : electricityMixImpacts.entrySet()) {
            result.merge(entry.getKey(), entry.getValue(), (list1, list2) -> {
                List<ItemImpactRest> merged = new ArrayList<>(list1);
                merged.addAll(list2);
                return merged;
            });
        }
        return result;
    }

    public Map<String, List<ItemImpactRest>> bulkGetItemImpactsForWorkspace(
            Set<String> criteria,
            Set<String> lifecycleSteps,
            Long workspaceId) {

        List<ItemImpact> itemImpactList =
                itemImpactRepository.findByCriterionInAndLifecycleStepInAndWorkspaceId(
                        criteria,
                        lifecycleSteps,
                        workspaceId
                );
        Map<String, List<ItemImpactRest>> map = new HashMap<>();
        for (ItemImpact impact : itemImpactList) {

            String key = buildImpactKey(
                    impact.getCriterion(),
                    impact.getLifecycleStep(),
                    impact.getName(),
                    workspaceId
            );
            map.computeIfAbsent(key, k -> new ArrayList<>())
                    .addAll(refRestMapper.toItemImpactRest(List.of(impact)));
        }
        return map;
    }
    public Map<String, List<ItemImpactRest>> bulkGetItemImpactsElectricityMixForWorkspace(
            Set<String> criteria,
            Set<String> locations,
            Long workspaceId) {
        List<ItemImpact> electricityMixImpact = itemImpactRepository.findByCriterionInAndCategoryAndLocationInAndWorkspaceId(
                criteria, "electricity-mix", locations, workspaceId);
        Map<String, List<ItemImpactRest>> map = new HashMap<>();
        for (ItemImpact impact : electricityMixImpact) {
            String key = buildElectricityMixImpactKey(
                    impact.getCriterion(),
                    impact.getCategory(),
                    impact.getLocation(),
                    workspaceId
            );
            map.computeIfAbsent(key, k -> new ArrayList<>())
                    .addAll(refRestMapper.toItemImpactRest(List.of(impact)));
        }
        return map;
    }

    public long countItemImpactsForWorkspace(Long workspaceId) {
        return itemImpactRepository.countByWorkspaceId(workspaceId);
    }



    private String buildModelKey(String model, Long workspaceId) {
        return model + "|" + workspaceId;
    }
    private String buildTypeKey(String type, Long workspaceId) {
        return type + "|" + workspaceId;
    }
    private String buildImpactKey(String criterion, String lifecycleStep, String itemImpactName, Long workspaceId) {
        return criterion + "|" + lifecycleStep + "|" + itemImpactName +"|" + workspaceId;
    }

    private String buildElectricityMixImpactKey(String criterion, String category,String location, Long workspaceId) {
        return criterion + "|" + category + "|" + location +"|" + workspaceId;
    }

}
