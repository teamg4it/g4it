/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.criteria;

import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriteriaServiceTest {

    private static final String SUBSCRIBER = "SUBSCRIBER";
    private static final Long ORGANIZATION_ID = 1L;
    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private CriteriaService criteriaService;

    @Test
    void getSelectedCriteriaReturnsCriteriaByTypeWhenSubscriberExists() {
        List<String> mockCriteria = List.of("criteria1", "criteria2");
        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setCriteria(mockCriteria);

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(mockSubscriber));

        CriteriaByType result = criteriaService.getSelectedCriteria(SUBSCRIBER);
        assertNotNull(result);
    }

    @Test
    void getSelectedCriteriaThrowsExceptionWhenSubscriberNotFound() {
        String subscriber = "nonExistentSubscriber";

        when(subscriberRepository.findByName(subscriber)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () -> {
            criteriaService.getSelectedCriteria(subscriber);
        });

        assertEquals("404", exception.getCode());
        assertEquals("Subscriber nonExistentSubscriber not found", exception.getMessage());
    }

    @Test
    void getSelectedCriteriaForInventoryReturnsInventoryCriteriaWhenInventoryCriteriaProvided() {
        List<String> inventoryCriteria = List.of("inventory1", "inventory2");
        List<String> subscriberCriteria = List.of("criteria1", "criteria2");
        Organization mockOrganization = new Organization();
        mockOrganization.setCriteriaIs(List.of("orgCriteria1"));

        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setCriteria(subscriberCriteria);

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(mockSubscriber));
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(mockOrganization);

        CriteriaByType result = criteriaService.getSelectedCriteriaForInventory(SUBSCRIBER, ORGANIZATION_ID, inventoryCriteria);

        assertNotNull(result);
    }
    @Test
    void getSelectedCriteriaForInventoryReturnsOrganizationCriteriaWhenNoInventoryCriteriaProvided() {
        List<String> orgCriteria = List.of("orgCriteria1", "orgCriteria2");
        List<String> subscriberCriteria = List.of("criteria1", "criteria2");

        Organization mockOrg = new Organization();
        mockOrg.setCriteriaIs(orgCriteria);

        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setCriteria(subscriberCriteria);
        
        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(mockSubscriber));
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(mockOrg);

        CriteriaByType result = criteriaService.getSelectedCriteriaForInventory(SUBSCRIBER, ORGANIZATION_ID, null);

        assertNotNull(result);
    }   
    
    @Test
    void getSelectedCriteriaForInventoryReturnsSubscriberCriteriaWhenNoInventoryOrOrganizationCriteriaProvided() {
        List<String> subscriberCriteria = List.of("criteria1", "criteria2");
        Organization mockOrganization = new Organization();

        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setCriteria(subscriberCriteria);

        when(mockOrganization.getCriteriaIs()).thenReturn(null);
        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(mockSubscriber));
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(mockOrganization);

        CriteriaByType result = criteriaService.getSelectedCriteriaForInventory(SUBSCRIBER, ORGANIZATION_ID, null);

        assertNotNull(result);
    }

    @Test
    void getSelectedCriteriaForDigitalServiceReturnsDigitalServiceCriteriaWhenDigitalServiceCriteriaProvided() {
        List<String> digitalServiceCriteria = List.of("digital1", "digital2");
        List<String> subscriberCriteria = List.of("criteria1", "criteria2");
        Organization mockOrganization = new Organization();
        mockOrganization.setCriteriaDs(List.of("orgCriteriaDs1"));

        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setCriteria(subscriberCriteria);

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(mockSubscriber));
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(mockOrganization);

        CriteriaByType result = criteriaService.getSelectedCriteriaForDigitalService(SUBSCRIBER, ORGANIZATION_ID, digitalServiceCriteria);

        assertNotNull(result);
    }
    @Test
    void getSelectedCriteriaForDigitalServiceReturnsOrganizationCriteriaWhenNoDigitalServiceCriteriaProvided() {
        List<String> orgCriteria = List.of("orgCriteria1", "orgCriteria2");
        List<String> subscriberCriteria = List.of("criteria1", "criteria2");

        Organization mockOrg = new Organization();
        mockOrg.setCriteriaIs(orgCriteria);

        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setCriteria(subscriberCriteria);

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(mockSubscriber));
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(mockOrg);

        CriteriaByType result = criteriaService.getSelectedCriteriaForDigitalService(SUBSCRIBER, ORGANIZATION_ID, null);

        assertNotNull(result);
    }

    @Test
    void getSelectedCriteriaForDigitalServiceReturnsSubscriberCriteriaWhenNoDigitalServiceOrOrganizationCriteriaProvided() {
        List<String> subscriberCriteria = List.of("criteria1", "criteria2");
        Organization mockOrganization = new Organization();

        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setCriteria(subscriberCriteria);

        when(mockOrganization.getCriteriaIs()).thenReturn(null);
        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(mockSubscriber));
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(mockOrganization);

        CriteriaByType result = criteriaService.getSelectedCriteriaForDigitalService(SUBSCRIBER, ORGANIZATION_ID, null);

        assertNotNull(result);
    }

}