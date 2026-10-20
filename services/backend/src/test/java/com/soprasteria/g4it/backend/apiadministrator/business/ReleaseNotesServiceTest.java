/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiadministrator.modeldb.ReleaseNote;
import com.soprasteria.g4it.backend.apiadministrator.repository.ReleaseNotesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReleaseNotesServiceTest {

    @Mock
    private ReleaseNotesRepository repository;

    @InjectMocks
    private ReleaseNotesService service;

    @Test
    void publishSavesReleaseNoteContent() {
        String content = "<h2>New feature</h2>";

        service.publish(content);

        ArgumentCaptor<ReleaseNote> releaseNoteCaptor = ArgumentCaptor.forClass(ReleaseNote.class);
        verify(repository).save(releaseNoteCaptor.capture());
        assertThat(releaseNoteCaptor.getValue().getContent()).isEqualTo(content);
    }
}
