/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace.business;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.apiworkspace.model.OrganizationDetailsBO;
import com.soprasteria.g4it.backend.apiworkspace.model.WorkspaceDetailsBO;
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
    OrganizationRepository organizationRepository;

    /**
     * The Repository to access Organization data.
     */
    @Autowired
    WorkspaceRepository workspaceRepository;

    public List<OrganizationDetailsBO> searchSubscribersByDomainName(final String userEmail) {
        String domainName = userEmail.substring(userEmail.indexOf("@") + 1);
        List<Organization> organizations = organizationRepository.findByAuthorizedDomainsContaining(domainName);
        List<OrganizationDetailsBO> lstSubscriber = new ArrayList<>();
        for (Organization organization : organizations) {
            List<Workspace> workspaces = workspaceRepository.findBySubscriberId(organization.getId());
            List<WorkspaceDetailsBO> lstOrganizations = new ArrayList<>();
            for (Workspace workspace : workspaces) {
                lstOrganizations.add(WorkspaceDetailsBO.builder().id(workspace.getId()).name(workspace.getName()).status(workspace.getStatus()).build());
            }
            OrganizationDetailsBO organizationDetailsBO = OrganizationDetailsBO.builder().id(organization.getId()).name(organization.getName()).organizations(lstOrganizations).build();
            lstSubscriber.add(organizationDetailsBO);
        }
        return lstSubscriber;
    }
}
