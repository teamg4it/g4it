/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckPhysicalEquipment;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
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
public class LoadPhysicalEquipmentMetadataService implements IMetadataLoaderService {

    @Autowired
    private CheckPhysicalEquipmentRepository checkPhysicalEquipmentRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void execute(Context context, FileToLoad fileToLoad, int pageNumber, List<Object> objects) {

        List<CheckPhysicalEquipment> checkPhysicalEquipments = new ArrayList<>();
        LocalDateTime beginBulkCreation = LocalDateTime.now();

        for (int i = 0; i < objects.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            InPhysicalEquipmentRest inPhysicalEquipmentRest = (InPhysicalEquipmentRest) objects.get(i);

            CheckPhysicalEquipment checkPhysicalEquipment = new CheckPhysicalEquipment();
            checkPhysicalEquipment.setPhysicalEquipmentName(inPhysicalEquipmentRest.getName());
            checkPhysicalEquipment.setDatacenterName(inPhysicalEquipmentRest.getDatacenterName());
            checkPhysicalEquipment.setCreationDate(beginBulkCreation);
            checkPhysicalEquipment.setFileName(fileToLoad.getFilename());
            checkPhysicalEquipment.setTaskId(context.getTaskId());
            checkPhysicalEquipment.setLineNumber(line);
            checkPhysicalEquipments.add(checkPhysicalEquipment);
        }

        checkPhysicalEquipmentRepository.saveAll(checkPhysicalEquipments);
        entityManager.flush();
        entityManager.clear();

    }

}
