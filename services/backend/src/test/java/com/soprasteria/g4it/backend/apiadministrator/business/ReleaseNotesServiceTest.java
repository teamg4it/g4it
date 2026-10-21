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
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseNotesServiceTest {

    @Mock
    private ReleaseNotesRepository releaseNotesRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReleaseNotesService service;

    @Test
    void publishSavesReleaseNoteContent() {
        String content = "<h2>New feature</h2>";

        service.publish(content);

        ArgumentCaptor<ReleaseNote> releaseNoteCaptor = ArgumentCaptor.forClass(ReleaseNote.class);
        verify(releaseNotesRepository).save(releaseNoteCaptor.capture());
        assertThat(releaseNoteCaptor.getValue().getContent()).isEqualTo(content);
    }

    @Test
    void returnsLatestNoteWhenUserHasNotSeenIt() {
        ReleaseNote latest = ReleaseNote.builder().id(12L).content("Latest").build();
        when(releaseNotesRepository.findLatestUnseenByUserId(4L)).thenReturn(Optional.of(latest));

        assertThat(service.getUnseenReleaseNote(4L)).contains(latest);
    }

    @Test
    void returnsLatestNoteRegardlessOfSeenState() {
        ReleaseNote latest = ReleaseNote.builder().id(12L).content("Latest").build();
        when(releaseNotesRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.of(latest));

        assertThat(service.getLatestReleaseNote()).contains(latest);
    }

    @Test
    void returnsEmptyWhenNoLatestNoteExists() {
        when(releaseNotesRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());

        assertThat(service.getLatestReleaseNote()).isEmpty();
    }

    @Test
    void returnsEmptyWhenUserHasSeenLatestNote() {
        ReleaseNote latest = ReleaseNote.builder().id(12L).content("Latest").build();
        when(releaseNotesRepository.findLatestUnseenByUserId(4L)).thenReturn(Optional.empty());

        assertThat(service.getUnseenReleaseNote(4L)).isEmpty();
    }

    @Test
    void returnsEmptyWhenNoNoteHasBeenPublished() {
        when(releaseNotesRepository.findLatestUnseenByUserId(4L)).thenReturn(Optional.empty());

        assertThat(service.getUnseenReleaseNote(4L)).isEmpty();
    }

    @Test
    void marksLatestPublishedNoteAsSeenForUser() {
        service.markLatestReleaseNoteAsSeen(4L);

        verify(userRepository).markLatestReleaseNoteAsSeen(4L);
    }
}
