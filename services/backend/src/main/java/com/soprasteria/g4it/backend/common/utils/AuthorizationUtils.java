/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class AuthorizationUtils {

    @Value("${ecomindaimodule.enabled}")
    private boolean isEcomindModuleEnabled;

    @Autowired
    private SubscriberRepository subscriberRepository;

    private static final String ECOMINDAI_DISABLED = "The EcoMindAI module is currently disabled";

    private static final String ECOMINDAI_DISABLED_FOR_SUBSCRIBER = "The EcoMindAi module is currently disabled for this subscriber";

    public void checkEcomindAuthorization() {
        if (!isEcomindModuleEnabled) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN,
                    ECOMINDAI_DISABLED);
        }
    }

    public void checkEcomindEnabledForSubscriber(String subscriber) {
        Optional<Subscriber> subsc = subscriberRepository.findByName(subscriber);
        if (subsc.isPresent() && !subsc.get().isEcomindai()) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN,
                    ECOMINDAI_DISABLED_FOR_SUBSCRIBER);
        }
    }
}
