/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckDatacenter;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoadDatacenterMetadataService implements IMetadataLoaderService {

    @Autowired
    private CheckDatacenterRepository checkDatacenterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void execute(Context context, FileToLoad fileToLoad, int pageNumber, List<Object> objects) {

        List<CheckDatacenter> checkDatacenters = new ArrayList<>();
        LocalDateTime beginBulkCreation = LocalDateTime.now();

        for (int i = 0; i < objects.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            InDatacenterRest inDatacenterRest = (InDatacenterRest) objects.get(i);

            CheckDatacenter checkDatacenter = new CheckDatacenter();
            checkDatacenter.setDatacenterName(inDatacenterRest.getName());
            checkDatacenter.setCreationDate(beginBulkCreation);
            checkDatacenter.setFileName(fileToLoad.getFilename());
            checkDatacenter.setTaskId(context.getTaskId());
            checkDatacenter.setLineNumber(line);
            checkDatacenters.add(checkDatacenter);
        }

        checkDatacenterRepository.saveAll(checkDatacenters);
        entityManager.flush();
        entityManager.clear();
    }
}
