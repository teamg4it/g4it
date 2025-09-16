/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.google.common.math.Quantiles;
import com.soprasteria.g4it.backend.apiindicator.modeldb.RefSustainableIndividualPackage;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReferentialService {

    @Autowired
    ReferentialGetService referentialGetService;

    @Autowired
    RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository;

    public List<String> getLifecycleSteps() {
        return referentialGetService.getAllLifecycleSteps().stream().map(LifecycleStepRest::getCode).toList();
    }

    public List<CriterionRest> getActiveCriteria(List<String> criteria) {
        final List<CriterionRest> criterionList = referentialGetService.getAllCriteria();

        if (criteria == null) return criterionList;

        if (!criterionList.stream().map(CriterionRest::getCode).collect(Collectors.toSet()).containsAll(criteria)) {
            return null;
        }

        return criterionList.stream()
                .filter(criterionRest -> criteria.contains(criterionRest.getCode()))
                .toList();

    }

    /**
     *
     * @param organization (mapped to subscriber column in table)
     * @return List<HypothesisRest>
     */

    public List<HypothesisRest> getHypotheses(String organization) {
        final List<HypothesisRest> result = new ArrayList<>(referentialGetService.getHypotheses(organization));

        final Set<String> organizationHypotheseCodes = result.stream()
                .map(HypothesisRest::getCode)
                .collect(Collectors.toSet());

        result.addAll(referentialGetService.getHypotheses(null).stream()
                .filter(hypothesisRest -> !organizationHypotheseCodes.contains(hypothesisRest.getCode()))
                .toList());

        return result;
    }

    /**
     *
     * @param organization (mapped to subscriber column in table)
     * @return MatchingItemRest
     */
    public MatchingItemRest getMatchingItem(String model, String organization) {
        MatchingItemRest matchingItem = referentialGetService.getMatchingItem(model, organization);
        if (matchingItem == null) {
            matchingItem = referentialGetService.getMatchingItem(model, null);
        }
        return matchingItem;
    }

    /**
     * @param organization (mapped to subscriber column in table)
     * @return ItemTypeRest
     */
    public ItemTypeRest getItemType(String type, String organization) {
        List<ItemTypeRest> itemTypeRestList = referentialGetService.getItemTypes(type, organization);
        if (itemTypeRestList.isEmpty()) {
            itemTypeRestList = referentialGetService.getItemTypes(type, null);
        }

        return itemTypeRestList.getFirst();
    }

    /**
     *
     * @param organization (mapped to subscriber in table)
     * @return List<ItemImpactRest>
     */
    public List<ItemImpactRest> getItemImpacts(final String criterion, final String lifecycleStep, final String name,
                                               final String location, final String organization) {
        List<ItemImpactRest> itemImpacts = new ArrayList<>(referentialGetService.getItemImpacts(criterion, lifecycleStep,
                name, null, null, organization));

        if (itemImpacts.isEmpty()) {
            itemImpacts = new ArrayList<>(referentialGetService.getItemImpacts(criterion, lifecycleStep,
                    name, null, null, null));
        }

        List<ItemImpactRest> electricityMixImpact = new ArrayList<>(referentialGetService.getItemImpacts(criterion, null, null, location, "electricity-mix", organization));
        if (electricityMixImpact.isEmpty()) {
            electricityMixImpact = new ArrayList<>(referentialGetService.getItemImpacts(criterion, null, null, location, "electricity-mix", null));
        }

        itemImpacts.addAll(electricityMixImpact);

        return itemImpacts;
    }

    public Map<String, Double> getSipValueMap(List<String> activeCriteria) {
        Set<String> criteria = activeCriteria.stream().map(StringUtils::kebabToSnakeCase).collect(Collectors.toSet());

        return refSustainableIndividualPackageRepository.findAll()
                .stream()
                .filter(item -> criteria.contains(StringUtils.kebabToSnakeCase(CriteriaUtils.transformCriteriaNameToCriteriaKey(item.getCriteria()))))
                .collect(Collectors.toMap(
                        r -> StringUtils.kebabToSnakeCase(CriteriaUtils.transformCriteriaNameToCriteriaKey(r.getCriteria())),
                        RefSustainableIndividualPackage::getIndividualSustainablePackage)
                );

    }

    /**
     * Get electricity mix map quartiles : ([country, criteria], quartile)
     *
     * @return the map of quartiles
     */
    @Cacheable("getNewElectricityMixQuartiles")
    public Map<Pair<String, String>, Integer> getElectricityMixQuartiles() {
        var allMixElec = referentialGetService.getElectricityMix().stream()
                .filter(mix -> {
                    var hasNullValue = mix.getCriterion() == null || mix.getValue() == null;
                    if (hasNullValue) {
                        log.error("Electricity mix of country: {} has null criteria or value", mix.getLocation());
                    }
                    return !hasNullValue;
                }).toList();

        var quartileValues = allMixElec.stream()
                .collect(Collectors.groupingBy(ItemImpact::getCriterion))
                .entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Quantiles
                                .quartiles()
                                .indexes(1, 2, 3, 4)
                                .compute(entry.getValue().stream().map(ItemImpact::getValue).sorted().toList())));

        return allMixElec.stream().collect(Collectors.toMap(
                mix -> Pair.of(mix.getLocation(), mix.getCriterion()),
                mix -> {

                    int quartileIndex;
                    if (mix.getValue() == null) {
                        log.error("Electricity mix of country: {} and criteria: {} has null value", mix.getLocation(), mix.getCriterion());
                        quartileIndex = 4;
                    } else if (mix.getValue() <= quartileValues.get(mix.getCriterion()).get(1)) {
                        quartileIndex = 1;
                    } else if (mix.getValue() <= quartileValues.get(mix.getCriterion()).get(2)) {
                        quartileIndex = 2;
                    } else if (mix.getValue() <= quartileValues.get(mix.getCriterion()).get(3)) {
                        quartileIndex = 3;
                    } else {
                        quartileIndex = 4;
                    }
                    return quartileIndex;
                }));
    }
}

