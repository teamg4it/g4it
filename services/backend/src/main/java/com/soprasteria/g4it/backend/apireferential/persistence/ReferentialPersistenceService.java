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
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ReferentialPersistenceService {

    @Autowired
    LifecycleStepRepository lifecycleStepRepository;

    @Autowired
    CriterionRepository criteriaRepository;

    @Autowired
    ItemTypeRepository itemTypeRepository;

    @Autowired
    HypothesisRepository hypothesisRepository;

    @Autowired
    MatchingItemRepository matchingItemRepository;

    @Autowired
    ItemImpactRepository itemImpactRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public int saveCriteria(List<Criterion> criterionList) {
        criteriaRepository.deleteAll();
        return criteriaRepository.saveAll(criterionList).size();
    }

    @Transactional
    public int saveLifecycleSteps(List<LifecycleStep> lifecycleEntities) {
        lifecycleStepRepository.deleteAll();
        return lifecycleStepRepository.saveAll(lifecycleEntities).size();
    }

    @Transactional
    public int saveItemTypes(List<ItemType> itemTypeEntities) {
        itemTypeRepository.deleteAll();
        return itemTypeRepository.saveAll(itemTypeEntities).size();
    }

    public int saveItemTypes(List<ItemType> itemTypeEntities, final String organization) {
        itemTypeRepository.deleteByOrganization(organization);
        return itemTypeRepository.saveAll(itemTypeEntities).size();
    }

    @Transactional
    public int saveHypotheses(List<Hypothesis> hypotheses) {
        hypothesisRepository.deleteAll();
        return hypothesisRepository.saveAll(hypotheses).size();
    }

    public int saveHypotheses(List<Hypothesis> hypotheses, final String organization) {
        hypothesisRepository.deleteByOrganization(organization);
        return hypothesisRepository.saveAll(hypotheses).size();
    }

    @Transactional
    public int saveItemMatchings(List<MatchingItem> matchingItems) {
        matchingItemRepository.deleteAll();
        return matchingItemRepository.saveAll(matchingItems).size();
    }

    public int saveItemMatchings(List<MatchingItem> matchingItems, final String organization) {
        matchingItemRepository.deleteByOrganization(organization);
        return matchingItemRepository.saveAll(matchingItems).size();
    }

    @Transactional
    public void truncateItemImpacts() {
        itemImpactRepository.truncateTable();
    }

    public void deleteItemImpactsByOrganization(final String organization) {
        itemImpactRepository.deleteByOrganization(organization);
    }

    @Transactional
    public void saveItemImpacts(List<ItemImpact> itemImpacts) {
        itemImpactRepository.saveAll(itemImpacts);
        entityManager.flush();
        entityManager.clear();
    }

}
