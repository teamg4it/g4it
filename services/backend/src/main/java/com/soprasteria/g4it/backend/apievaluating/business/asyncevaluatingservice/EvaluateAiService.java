/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalImpact;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.*;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.AiParameterRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EvaluateAiService {

    private static final int INITIAL_MAP_CAPICITY = 50_000;
    private static final int MAXIMUM_MAP_CAPICITY = 500_000;
    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    DigitalServiceRepository digitalServiceRepository;
    @Autowired
    AiParameterRepository inAIParameterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository;
    @Autowired
    EvaluateNumEcoEvalService evaluateNumEcoEvalService;
    @Autowired
    ReferentialService referentialService;
    @Autowired
    SaveService saveService;
    @Autowired
    OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    EvaluateBoaviztapiService evaluateBoaviztapiService;
    @Autowired
    InternalToNumEcoEvalImpact internalToNumEcoEvalImpact;
    @Value("${local.working.folder}")
    private String localWorkingFolder;

    /**
     * Evaluate the digital service with ia parameter
     *
     * @param context         the context
     * @param task            the task
     * @param exportDirectory the export directory
     */
    public void doEvaluateAi(final Context context, final Task task, Path exportDirectory) {
        //TODO : get the data in database

        // Récupération du service digital
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(context.getDigitalServiceUid());

        // Récupération des AI parameters
        List<AiParameter> aiParameters = inAIParameterRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        // Récupération de data center
        List<InDatacenter> datacenters = inDatacenterRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        // Récupération de physical equipment
        List<InPhysicalEquipment> physicalEquipments = inPhysicalEquipmentRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        // Récupération de virtual equipment
        List<InVirtualEquipment> virtualEquipments = inVirtualEquipmentRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        log.info("Retrieved digital service and AI parameters");
        log.info("Retrieved {} datacenters, {} physical equipments, {} virtual equipments",
                datacenters.size(), physicalEquipments.size(), virtualEquipments.size());


        //TODO : call Ecomind with the data

        //TODO : save the result of the call in db

        //TODO : Call numecoeval

        //TODO : Save the result in db
    }

}
