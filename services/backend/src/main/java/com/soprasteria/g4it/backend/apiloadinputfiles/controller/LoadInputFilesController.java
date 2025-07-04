/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiloadinputfiles.controller;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.LoadInputFilesService;
import com.soprasteria.g4it.backend.common.task.mapper.TaskMapper;
import com.soprasteria.g4it.backend.server.gen.api.LoadingFilesApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Inventory Loading Rest Service.
 */
@Service
@NoArgsConstructor
public class LoadInputFilesController implements LoadingFilesApiDelegate {

    @Autowired
    LoadInputFilesService loadInputFilesService;

    @Autowired
    TaskMapper taskMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskIdRest> launchloadInputFiles(final String subscriber,
                                                           final Long organization,
                                                           final Long inventoryId,
                                                           String acceptLanguage,
                                                           final List<MultipartFile> datacenters,
                                                           final List<MultipartFile> physicalEquipments,
                                                           final List<MultipartFile> virtualEquipments,
                                                           final List<MultipartFile> applications
    ) {
        return ResponseEntity.ok(taskMapper.mapTaskId(
                loadInputFilesService.loadFiles(
                        subscriber, organization, inventoryId,
                        datacenters, physicalEquipments, virtualEquipments, applications
                )
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskIdRest> launchLoadInputFilesDigitalService(String subscriber,
                                                                         Long organization,
                                                                         String  digitalServiceUid,
                                                                         String acceptLanguage,
                                                                         List<MultipartFile> datacenters,
                                                                         List<MultipartFile> physicalEquipments,
                                                                         List<MultipartFile> virtualEquipments) {

            return ResponseEntity.ok(taskMapper.mapTaskId(
                    loadInputFilesService.loadDigitalServiceFiles(
                            subscriber, organization, digitalServiceUid,
                            datacenters, physicalEquipments, virtualEquipments)
            ));

        }

}