/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiuser.business;


import com.soprasteria.g4it.backend.apiuser.model.SubscriberDetailsBO;
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
class SubscriberServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private SubscriberService subscriberService;

    @Test
    void getSubscriptionByIdReturnsSubscriberWhenIdExists() {
        Long subscriptionId = 1L;
        Subscriber mockSubscriber = new Subscriber();
        mockSubscriber.setId(subscriptionId);
        mockSubscriber.setName("Test Subscriber");

        when(subscriberRepository.findById(subscriptionId)).thenReturn(Optional.of(mockSubscriber));

        Subscriber result = subscriberService.getSubscriptionById(subscriptionId);

        assertNotNull(result);
        assertEquals(subscriptionId, result.getId());
        assertEquals("Test Subscriber", result.getName());
    }

    @Test
    void getSubscriptionByIdThrowsExceptionWhenIdDoesNotExist() {
        Long subscriptionId = 1L;

        when(subscriberRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(
                G4itRestException.class,
                () -> subscriberService.getSubscriptionById(subscriptionId)
        );

        assertEquals("404", exception.getCode());
        assertEquals("subscription with id '1' not found", exception.getMessage());
    }

    @Test
    void searchSubscribersByDomainNameReturnsSubscribersForValidDomain() {
        String userEmail = "user@example.com";
        String domainName = "example.com";
        List<Subscriber> mockSubscribers = List.of(
                Subscriber.builder().id(1L).name("Subscriber1").authorizedDomains(domainName).build(),
                Subscriber.builder().id(2L).name("Subscriber2").authorizedDomains(domainName).build()
        );

        when(subscriberRepository.findByAuthorizedDomainsContaining(domainName)).thenReturn(mockSubscribers);

        List<SubscriberDetailsBO> result = subscriberService.searchSubscribersByDomainName(userEmail);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Subscriber1", result.get(0).getName());
        assertEquals("Subscriber2", result.get(1).getName());
    }

    @Test
    void searchSubscribersByDomainNameReturnsEmptyListForUnknownDomain() {
        String userEmail = "user@unknown.com";
        String domainName = "unknown.com";

        when(subscriberRepository.findByAuthorizedDomainsContaining(domainName)).thenReturn(List.of());

        List<SubscriberDetailsBO> result = subscriberService.searchSubscribersByDomainName(userEmail);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
