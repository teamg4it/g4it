/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InDatacenterMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckDatacenterService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
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
public class LoadDatacenterService {

    @Autowired
    CheckDatacenterService checkDatacenterService;

    @Autowired
    InDatacenterMapper inDatacenterMapper;

    @Autowired
    InDatacenterRepository inDatacenterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<LineError> execute(final Context context, final FileToLoad fileToLoad, final int pageNumber, List<InDatacenterRest> datacenters) {
        if (datacenters.isEmpty()) return List.of();

        log.info("Load datacenters for {}, size = {}", context.log(), datacenters.size());

        List<LineError> errors = new ArrayList<>();

        List<InDatacenter> datacentersToSave = new ArrayList<>();

        for (int i = 0; i < datacenters.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            List<LineError> coherenceErrorInLine =  fileToLoad.getCoherenceErrorByLineNumer().getOrDefault(line, List.of());

            final List<LineError> checkErrors = checkDatacenterService.checkRules(context, datacenters.get(i),fileToLoad.getFilename(), line);
            if (checkErrors.isEmpty() && coherenceErrorInLine.isEmpty()) {
                //No check line error and no line coherence and duplication error
                datacentersToSave.add(inDatacenterMapper.toEntity(datacenters.get(i)));
            } else {
                errors.addAll(checkErrors);
                errors.addAll(coherenceErrorInLine);
            }
        }

        // Delete existing datacenters present in csv
        Set<String> datacenterNames = datacenters.stream().map(InDatacenterRest::getName).collect(Collectors.toSet());
        inDatacenterRepository.deleteByInventoryIdAndNameIn(context.getInventoryId(), datacenterNames);

        // Load data into database
        inDatacenterRepository.saveAll(datacentersToSave);
        entityManager.flush();
        entityManager.clear();

        return errors;
    }

    public Long getDatacenterCount(Long inventoryId) {
        return inDatacenterRepository.countDistinctNameByInventoryId(inventoryId);
    }

}
