/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.apiinout.mapper.InApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckApplication;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoadApplicationMetadataService implements IMetadataLoaderService {

    @Autowired
    private CheckApplicationRepository checkApplicationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void execute(Context context, FileToLoad fileToLoad, int pageNumber, List<Object> objects) {

        List<CheckApplication> checkApplications = new ArrayList<>();

        LocalDateTime beginBulkCreation = LocalDateTime.now();

        for (int i = 0; i < objects.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            InApplicationRest inApplicationRest = (InApplicationRest) objects.get(i);
            CheckApplication checkApplication = new CheckApplication();
            checkApplication.setApplicationName(inApplicationRest.getName());
            checkApplication.setEnvironmentType(inApplicationRest.getEnvironment());
            checkApplication.setVirtualEquipmentName(inApplicationRest.getVirtualEquipmentName());
            checkApplication.setCreationDate(beginBulkCreation);
            checkApplication.setFileName(fileToLoad.getFilename());
            checkApplication.setTaskId(context.getTaskId());
            checkApplication.setLineNumber(line);
            checkApplications.add(checkApplication);
        }

        checkApplicationRepository.saveAll(checkApplications);
        entityManager.flush();
        entityManager.clear();

    }

}
