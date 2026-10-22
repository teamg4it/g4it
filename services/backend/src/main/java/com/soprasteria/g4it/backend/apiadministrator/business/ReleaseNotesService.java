package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiadministrator.modeldb.ReleaseNote;
import com.soprasteria.g4it.backend.apiadministrator.repository.ReleaseNotesRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReleaseNotesService {
    private final ReleaseNotesRepository releaseNotesRepository;
    private final UserRepository userRepository;

    @Transactional
    public void publish(String content) {
        releaseNotesRepository.save(ReleaseNote.builder().content(content.replaceAll("&nbsp;", " ")).build());
    }

    @Transactional(readOnly = true)
    public Optional<ReleaseNote> getUnseenReleaseNote(long userId) {
        return releaseNotesRepository.findLatestUnseenByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<ReleaseNote> getLatestReleaseNote() {
        return releaseNotesRepository.findFirstByOrderByIdDesc();
    }

    @Transactional
    public void markLatestReleaseNoteAsSeen(long userId) {
        userRepository.markLatestReleaseNoteAsSeen(userId);
    }
}
