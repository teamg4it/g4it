/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckPhysicalEquipmentService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LoadPhysicalEquipmentService {

    @Autowired
    CheckPhysicalEquipmentService checkPhysicalEquipmentService;

    @Autowired
    InPhysicalEquipmentMapper inPhysicalEquipmentMapper;

    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<LineError> execute(final Context context, final FileToLoad fileToLoad, final int pageNumber, List<InPhysicalEquipmentRest> physicalEquipments) {
        if (physicalEquipments.isEmpty()) return List.of();

        log.info("Load physical equipments for {}, size = {}", context.log(), physicalEquipments.size());

        List<LineError> errors = new ArrayList<>();

        List<InPhysicalEquipment> physicalEquipmentsToSave = new ArrayList<>();

        for (int i = 0; i < physicalEquipments.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            List<LineError> coherenceErrorInLine =  fileToLoad.getCoherenceErrorByLineNumer().getOrDefault(line, List.of());

            final List<LineError> checkErrors = checkPhysicalEquipmentService.checkRules(context, physicalEquipments.get(i),fileToLoad.getFilename(),  line);
            if (checkErrors.isEmpty() && coherenceErrorInLine.isEmpty()) {
                physicalEquipmentsToSave.add(inPhysicalEquipmentMapper.toEntity(physicalEquipments.get(i)));
            } else {
                errors.addAll(checkErrors);
                errors.addAll(coherenceErrorInLine);
            }
        }

        // Delete existing physical equipments and its sub objects
        final Set<String> names = physicalEquipments.stream().map(InPhysicalEquipmentRest::getName).collect(Collectors.toSet());
        inPhysicalEquipmentRepository.deleteByInventoryIdAndNameIn(context.getInventoryId(), names);

        // Load data into database
        inPhysicalEquipmentRepository.saveAll(physicalEquipmentsToSave);
        entityManager.flush();
        entityManager.clear();

        physicalEquipmentsToSave.clear();
        return errors;
    }

    public Long getPhysicalEquipmentCount(Long inventoryId) {
        return inPhysicalEquipmentRepository.sumQuantityByInventoryId(inventoryId);
    }

}
