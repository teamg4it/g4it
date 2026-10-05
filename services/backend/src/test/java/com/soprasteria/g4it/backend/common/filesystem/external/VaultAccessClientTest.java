/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.external;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.soprasteria.g4it.backend.common.filesystem.exception.FileStorageAccessExcepton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultAccessClientTest {

    @Mock
    private SecretClient secretClient;

    @InjectMocks
    private VaultAccessClient vaultAccessClient;

    @Test
    void shouldReturnConnectionString() {
        KeyVaultSecret secret =
                new KeyVaultSecret("ORG-TEST", "connection-string");

        when(secretClient.getSecret("ORG-TEST"))
                .thenReturn(secret);

        String result =
                vaultAccessClient.getConnectionStringForOrganization("org_test");

        assertEquals("connection-string", result);

        verify(secretClient).getSecret("ORG-TEST");
    }

    @Test
    void shouldThrowExceptionWhenOrganizationIsNull() {
        assertThrows(
                FileStorageAccessExcepton.class,
                () -> vaultAccessClient.getConnectionStringForOrganization(null)
        );

        verifyNoInteractions(secretClient);
    }
}
