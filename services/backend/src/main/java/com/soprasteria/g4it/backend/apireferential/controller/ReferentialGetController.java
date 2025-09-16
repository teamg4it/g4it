/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.controller;

import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.ReferentialGetApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Referential endpoints
 */
@Service
@NoArgsConstructor
public class ReferentialGetController implements ReferentialGetApiDelegate {

    @Autowired
    private ReferentialGetService referentialGetService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<LifecycleStepRest>> getAllLifecycleSteps() {
        List<LifecycleStepRest> allLifecycleSteps = referentialGetService.getAllLifecycleSteps();
        return new ResponseEntity<>(allLifecycleSteps, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<CriterionRest>> getAllCriteria() {
        List<CriterionRest> allCriteria = referentialGetService.getAllCriteria();
        return new ResponseEntity<>(allCriteria, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<HypothesisRest>> getHypotheses(String organization) {
        List<HypothesisRest> hypotheses = referentialGetService.getHypotheses(organization);
        return new ResponseEntity<>(hypotheses, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ItemTypeRest>> getItemTypes(String type, String organization) {
        List<ItemTypeRest> itemTypes = referentialGetService.getItemTypes(type, organization);
        return new ResponseEntity<>(itemTypes, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ItemImpactRest>> getItemImpacts(String criterion, String lifecycleStep,
                                                               String name, String location, String category,
                                                               String organization) {
        List<ItemImpactRest> itemImpacts = referentialGetService.getItemImpacts(criterion, lifecycleStep, name, location, category, organization);
        return new ResponseEntity<>(itemImpacts, HttpStatus.OK);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MatchingItemRest> getMatchingItem(String model, String organization) {
        MatchingItemRest matchingItem = referentialGetService.getMatchingItem(model, organization);
        if (matchingItem == null) throw new G4itRestException("404", "Matching item not found");

        return new ResponseEntity<>(matchingItem, HttpStatus.OK);
    }

}