/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace.business;

import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
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
public class NewWorkspaceService {
    /**
     * The Repository to access Subscriber data.
     */
    @Autowired
    SubscriberRepository subscriberRepository;

    /**
     * The Repository to access Organization data.
     */
    @Autowired
    WorkspaceRepository workspaceRepository;

    public List<SubscriberDetailsBO> searchSubscribersByDomainName(final String userEmail) {
        String domainName = userEmail.substring(userEmail.indexOf("@") + 1);
        List<Subscriber> subscribers = subscriberRepository.findByAuthorizedDomainsContaining(domainName);
        List<SubscriberDetailsBO> lstSubscriber = new ArrayList<>();
        for (Subscriber subscriber : subscribers) {
            List<Workspace> workspaces = workspaceRepository.findBySubscriberId(subscriber.getId());
            List<OrganizationDetailsBO> lstOrganizations = new ArrayList<>();
            for (Workspace workspace : workspaces) {
                lstOrganizations.add(OrganizationDetailsBO.builder().id(workspace.getId()).name(workspace.getName()).status(workspace.getStatus()).build());
            }
            SubscriberDetailsBO subscriberDetailsBO = SubscriberDetailsBO.builder().id(subscriber.getId()).name(subscriber.getName()).organizations(lstOrganizations).build();
            lstSubscriber.add(subscriberDetailsBO);
        }
        return lstSubscriber;
    }
}
