/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalImpact;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.EvaluateReportBO;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.*;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIServiceEstimationBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class EvaluateAiService {

    private static final int INITIAL_MAP_CAPICITY = 50_000;
    private static final int MAXIMUM_MAP_CAPICITY = 500_000;
    @Autowired
    InDatacenterRepository inDatacenterRepository;
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
    @Autowired
    AiService aiService;

    @Autowired
    AiConfigurationMapper aiConfigurationMapper;

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

        //TODO : call Ecomind with the data

        //TODO : save the result of the call in db
       /* List<AIServiceEstimationBO> estimationBOList =  evaluateEcomind();
        AIServiceEstimationBO estimationBO = estimationBOList.get(0);
*/

        //TODO : Call numecoeval

        //TODO : Save the result in db
    }

    private List<AIServiceEstimationBO> evaluateEcomind(AiParameter aiParameter) throws IOException {
            AIConfigurationBO aiConfigurationBO = AIConfigurationBO.builder().build();
            aiConfigurationBO.setFramework(aiParameter.getFramework());
            aiConfigurationBO.setModelName(aiParameter.getModelName());
            aiConfigurationBO.setQuantization(aiParameter.getQuantization());
            aiConfigurationBO.setNbParameters(aiParameter.getNbParameters());
            aiConfigurationBO.setTotalGeneratedTokens(aiParameter.getTotalGeneratedTokens().longValue());
            List<AIConfigurationRest> aiConfigurationRest = aiConfigurationMapper.toAIModelConfigRest(List.of(aiConfigurationBO));
            String stage = aiParameter.getIsInference() ? "INFERENCE" : "TRAINING";
            String type = aiParameter.getType();
            return aiService.runEstimation(type, stage,aiConfigurationRest);
    }
}
