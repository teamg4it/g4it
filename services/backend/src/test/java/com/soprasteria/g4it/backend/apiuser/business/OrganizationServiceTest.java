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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    @Test
    void getOrgByIdShouldReturnOrganization() {
        Long organizationId = 1L;
        Organization organization = new Organization();

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        Organization result = organizationService.getOrgById(organizationId);

        assertSame(organization, result);
    }

    @Test
    void getOrgByIdShouldThrowExceptionWhenOrganizationNotFound() {
        Long organizationId = 999L;

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(
                G4itRestException.class,
                () -> organizationService.getOrgById(organizationId)
        );

        assertEquals("404", exception.getCode());
        assertEquals(
                "organization with id '999' not found",
                exception.getMessage()
        );
    }
}
