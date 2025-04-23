/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckVirtualEquipment;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoadVirtualEquipmentMetadataService implements IMetadataLoaderService {

    @Autowired
    private CheckVirtualEquipmentRepository checkVirtualEquipmentRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void execute(Context context, FileToLoad fileToLoad, int pageNumber, List<Object> objects) {

        List<CheckVirtualEquipment> checkVirtualEquipments = new ArrayList<>();
        LocalDateTime beginBulkCreation = LocalDateTime.now();

        for (int i = 0; i < objects.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            InVirtualEquipmentRest inVirtualEquipmentRest = (InVirtualEquipmentRest) objects.get(i);

            CheckVirtualEquipment checkVirtualEquipment = new CheckVirtualEquipment();
            checkVirtualEquipment.setVirtualEquipmentName(inVirtualEquipmentRest.getName());
            checkVirtualEquipment.setPhysicalEquipmentName(inVirtualEquipmentRest.getPhysicalEquipmentName());
            checkVirtualEquipment.setInfrastructureType(inVirtualEquipmentRest.getInfrastructureType());
            checkVirtualEquipment.setCreationDate(beginBulkCreation);
            checkVirtualEquipment.setFileName(fileToLoad.getFilename());
            checkVirtualEquipment.setTaskId(context.getTaskId());
            checkVirtualEquipment.setLineNumber(line);
            checkVirtualEquipments.add(checkVirtualEquipment);
        }

        checkVirtualEquipmentRepository.saveAll(checkVirtualEquipments);
        entityManager.flush();
        entityManager.clear();

    }
}
