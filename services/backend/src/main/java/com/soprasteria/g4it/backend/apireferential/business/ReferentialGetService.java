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
import com.soprasteria.g4it.backend.apireferential.repository.*;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public List<ItemTypeRest> getItemTypes(String type, String organization) {

        // get all itemTypes
        if (type == null) {
            return refRestMapper.toItemTypeRest(itemTypeRepository.findByOrganization(organization));
        }

        Optional<ItemType> itemType = itemTypeRepository.findByTypeAndOrganization(type, organization);

        return refRestMapper.toItemTypeRest(itemType.map(List::of).orElseGet(List::of));
    }

    /**
     * Get the referential MatchingItem by model
     * and/or organization(mapped to subscriber column in table)
     *
     * @return matchingItem
     */
    @Cacheable("ref_getMatchingItem")
    public MatchingItemRest getMatchingItem(String model, String organization) {
        return matchingItemRepository.findByItemSourceAndOrganization(model, organization)
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
    public List<ItemImpact> getElectricityMix() {
        return itemImpactRepository.findByCategory("electricity-mix");
    }

}
