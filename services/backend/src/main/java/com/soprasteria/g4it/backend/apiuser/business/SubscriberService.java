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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Organization service.
 */
@Service
@Slf4j
public class SubscriberService {

    /**
     * The Repository to access Subscriber data.
     */
    @Autowired
    SubscriberRepository subscriberRepository;


    /**
     * Find the Subscriber by id.
     *
     * @param subscriptionId the subscriber's id
     * @return the Subscriber.
     */
    public Subscriber getSubscriptionById(final Long subscriptionId) {
        return subscriberRepository.findById(subscriptionId)
                .orElseThrow(
                        () -> new G4itRestException("404", String.format("subscription with id '%d' not found", subscriptionId))
                );
    }

    public List<SubscriberDetailsBO> searchSubscribersByDomainName(final String userEmail) {
        String domainName = userEmail.substring(userEmail.indexOf("@") + 1);
        List<Subscriber> subscribers = subscriberRepository.findByAuthorizedDomainsContaining(domainName);
        List<SubscriberDetailsBO> lstSubscriber = new ArrayList<>();
        for (Subscriber subscriber : subscribers) {
            lstSubscriber.add(SubscriberDetailsBO.builder().id(subscriber.getId()).name(subscriber.getName()).build());
        }
        return lstSubscriber;
    }
}
