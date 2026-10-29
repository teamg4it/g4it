/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.controller;

import com.soprasteria.g4it.backend.apiadministrator.business.ReleaseNotesService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.server.gen.api.ReleaseNotesApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.ReleaseNoteRest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReleaseNotesRestController implements ReleaseNotesApiDelegate {

    private final AuthService authService;
    private final ReleaseNotesService releaseNotesService;

    @Override
    public ResponseEntity<ReleaseNoteRest> getReleaseNotes() {
        return releaseNotesService.getUnseenReleaseNote(authService.getUser().getId())
                .map(releaseNote -> ResponseEntity.ok(ReleaseNoteRest.builder()
                        .id(releaseNote.getId())
                        .content(releaseNote.getContent())
                        .build()))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<Void> markReleaseNotesSeen() {
        releaseNotesService.markLatestReleaseNoteAsSeen(authService.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
