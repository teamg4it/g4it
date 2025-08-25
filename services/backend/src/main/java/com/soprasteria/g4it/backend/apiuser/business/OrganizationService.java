/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Organization service.
 */
@Service
@Slf4j
public class OrganizationService {

    /**
     * The Repository to access Subscriber data.
     */
    @Autowired
    OrganizationRepository organizationRepository;


    /**
     * Find the Subscriber by id.
     *
     * @param subscriptionId the subscriber's id
     * @return the Subscriber.
     */
    public Organization getSubscriptionById(final Long subscriptionId) {
        return organizationRepository.findById(subscriptionId)
                .orElseThrow(
                        () -> new G4itRestException("404", String.format("subscription with id '%d' not found", subscriptionId))
                );
    }


}
