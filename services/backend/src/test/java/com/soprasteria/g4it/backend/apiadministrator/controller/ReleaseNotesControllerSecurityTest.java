/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.controller;

import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorRoleService;
import com.soprasteria.g4it.backend.apiadministrator.business.ReleaseNotesService;
import com.soprasteria.g4it.backend.apiadministrator.modeldb.ReleaseNote;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import com.soprasteria.g4it.backend.server.gen.api.dto.ReleaseNotesPublishRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseNotesControllerSecurityTest {

    @Mock
    private AdministratorRoleService administratorRoleService;
    @Mock
    private AuthService authService;
    @Mock
    private ReleaseNotesService releaseNotesService;

    @InjectMocks
    private AdministratorRestController administratorController;

    private ReleaseNotesRestController releaseNotesController;

    @BeforeEach
    void setUp() {
        releaseNotesController = new ReleaseNotesRestController(authService, releaseNotesService);
    }

    @Test
    void nonSuperAdminCannotGetOrPublishReleaseNotes() {
        UserBO admin = UserBO.builder().id(1L).email("admin@example.com").build();
        when(authService.getUser()).thenReturn(admin);
        doThrow(new AuthorizationException(403, "Not a super admin"))
                .when(administratorRoleService).hasSuperAdminRights(admin);

        assertEquals(403, assertThrows(AuthorizationException.class,
                () -> administratorController.getLatestReleaseNote()).getStatusCode());
        assertEquals(403, assertThrows(AuthorizationException.class,
                () -> administratorController.saveReleaseNotes(
                        ReleaseNotesPublishRest.builder().content("Release notes").build())).getStatusCode());
    }

    @Test
    void superAdminCanGetAndPublishReleaseNotes() {
        UserBO superAdmin = UserBO.builder().id(1L).email(Constants.SUPER_ADMIN_EMAIL).build();
        when(authService.getUser()).thenReturn(superAdmin);
        when(releaseNotesService.getLatestReleaseNote()).thenReturn(Optional.empty());

        ResponseEntity<?> getResponse = administratorController.getLatestReleaseNote();
        ResponseEntity<Void> publishResponse = administratorController.saveReleaseNotes(
                ReleaseNotesPublishRest.builder().content("Release notes").build());

        assertEquals(HttpStatus.NO_CONTENT, getResponse.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, publishResponse.getStatusCode());
        verify(administratorRoleService, times(2)).hasSuperAdminRights(superAdmin);
        verify(releaseNotesService).publish("Release notes");
    }

    @Test
    void authenticatedRegularUserCanReadAndAcknowledgeReleaseNotes() {
        UserBO user = UserBO.builder().id(42L).email("user@example.com").build();
        when(authService.getUser()).thenReturn(user);
        when(releaseNotesService.getUnseenReleaseNote(42L))
                .thenReturn(Optional.of(ReleaseNote.builder().id(1L).content("Release notes").build()));

        assertEquals(HttpStatus.OK, releaseNotesController.getReleaseNotes().getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, releaseNotesController.markReleaseNotesSeen().getStatusCode());

        verify(releaseNotesService).markLatestReleaseNoteAsSeen(42L);
    }

    @Test
    void releaseNotesRequireAnAuthenticatedUser() {
        when(authService.getUser()).thenThrow(new AuthorizationException(401, "Not authenticated"));

        assertEquals(401, assertThrows(AuthorizationException.class,
                () -> releaseNotesController.getReleaseNotes()).getStatusCode());
        assertEquals(401, assertThrows(AuthorizationException.class,
                () -> releaseNotesController.markReleaseNotesSeen()).getStatusCode());
    }
}
