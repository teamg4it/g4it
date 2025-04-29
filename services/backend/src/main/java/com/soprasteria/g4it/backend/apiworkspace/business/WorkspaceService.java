/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace.business;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiworkspace.model.OrganizationDetailsBO;
import com.soprasteria.g4it.backend.apiworkspace.model.SubscriberDetailsBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Workspace service.
 */
@Service
@Slf4j
public class WorkspaceService {
    /**
     * The Repository to access Subscriber data.
     */
    @Autowired
    SubscriberRepository subscriberRepository;

    /**
     * The Repository to access Organization data.
     */
    @Autowired
    OrganizationRepository organizationRepository;

    public List<SubscriberDetailsBO> searchSubscribersByDomainName(final String userEmail) {
        String domainName = userEmail.substring(userEmail.indexOf("@") + 1);
        List<Subscriber> subscribers = subscriberRepository.findByAuthorizedDomainsContaining(domainName);
        List<SubscriberDetailsBO> lstSubscriber = new ArrayList<>();
        for (Subscriber subscriber : subscribers) {
            List<Organization> organizations = organizationRepository.findBySubscriberId(subscriber.getId());
            List<OrganizationDetailsBO> lstOrganizations = new ArrayList<>();
            for (Organization organization : organizations) {
                lstOrganizations.add(OrganizationDetailsBO.builder().id(organization.getId()).name(organization.getName()).status(organization.getStatus()).build());
            }
            SubscriberDetailsBO subscriberDetailsBO = SubscriberDetailsBO.builder().id(subscriber.getId()).name(subscriber.getName()).organizations(lstOrganizations).build();
            lstSubscriber.add(subscriberDetailsBO);
        }
        return lstSubscriber;
    }
}
