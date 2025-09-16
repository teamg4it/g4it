/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.common.filesystem.external;

import com.azure.security.keyvault.secrets.SecretClient;
import com.soprasteria.g4it.backend.common.filesystem.exception.FileStorageAccessExcepton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Client to access vault containing organization's connection string.
 */
@Profile("azure")
@Component
public class VaultAccessClient {

    /**
     * Azure secret client.
     */
    @Autowired
    private SecretClient secretClient;

    /**
     * Retrieve the organization's connection string.
     *
     * @param organization the client organization.
     * @return the organization's connection string.
     */
    public String getConnectionStringForOrganization(final String organization) {
        return Optional.ofNullable(organization)
                .map(sub -> secretClient.getSecret(sub.toUpperCase().replace("_", "-")))
                .orElseThrow(FileStorageAccessExcepton::new).getValue();
    }
}
