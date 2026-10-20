package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiadministrator.modeldb.ReleaseNote;
import com.soprasteria.g4it.backend.apiadministrator.repository.ReleaseNotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReleaseNotesService {
    private final ReleaseNotesRepository repository;

    @Transactional
    public void publish(String content) {
        repository.save(ReleaseNote.builder().content(content).build());
    }
}
