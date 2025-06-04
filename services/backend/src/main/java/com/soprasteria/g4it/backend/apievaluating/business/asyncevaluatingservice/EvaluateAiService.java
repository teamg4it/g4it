/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalImpact;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.EvaluateReportBO;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
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
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIServiceEstimationBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.HypothesisRest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.common.utils.InfrastructureType.CLOUD_SERVICES;

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

    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    AiParameterRepository inAIParameterRepository;
    @Autowired
    AggregationToOutput aggregationToOutput;

    @Autowired
    EvaluateService evaluateService;

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    /**
     * Evaluate the digital service with ia parameter
     *
     * @param context         the context
     * @param task            the task
     * @param exportDirectory the export directory
     */
    public void doEvaluateAi(final Context context, final Task task, Path exportDirectory) throws IOException {
        //TODO : get the data in database

        // Récupération du service digitalAdd commentMore actions
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
        List<AIServiceEstimationBO> estimationBOList = evaluateEcomind(aiParameters.getFirst());
        AIServiceEstimationBO estimationBO = estimationBOList.getFirst();

        //TODO : save the result of the call in db

        //TODO : Call numecoeval
        final String subscriber = context.getSubscriber();
        final long start = System.currentTimeMillis();
        final Long taskId = task.getId();

        // Match referential if needed, with cache
        final List<String> lifecycleSteps = referentialService.getLifecycleSteps();
        List<CriterionRest> activeCriteria = referentialService.getActiveCriteria(task.getCriteria().stream()
                .map(StringUtils::kebabToSnakeCase).toList());

        if (activeCriteria == null) return;

        List<String> criteriaCodes = activeCriteria.stream().map(CriterionRest::getCode).toList();

        // get (criterion, unit) map
        Map<String, String> criteriaUnitMap = activeCriteria.stream().collect(Collectors.toMap(
                CriterionRest::getCode,
                CriterionRest::getUnit
        ));

        RefShortcutBO refShortcutBO = new RefShortcutBO(
                criteriaUnitMap,
                getShortcutMap(criteriaCodes),
                getShortcutMap(lifecycleSteps),
                referentialService.getElectricityMixQuartiles()
        );

        long totalEquipments = inPhysicalEquipmentRepository.countByDigitalServiceUid(context.getDigitalServiceUid());

        final List<HypothesisRest> hypothesisRestList = referentialService.getHypotheses(subscriber);

        Map<String, Double> refSip = referentialService.getSipValueMap(criteriaCodes);

        log.info("Start evaluating impacts for {}/{}", context.log(), taskId);

        EvaluateReportBO evaluateReportBO = EvaluateReportBO.builder()
                .export(false)
                .isDigitalService(true)
                .nbPhysicalEquipmentLines(1)
                .nbVirtualEquipmentLines(1)
                .nbApplicationLines(0)
                .taskId(taskId)
                .build();

        Map<List<String>, AggValuesBO> aggregationPhysicalEquipments = new HashMap<>(INITIAL_MAP_CAPICITY);
        Map<List<String>, AggValuesBO> aggregationVirtualEquipments = new HashMap<>(context.isHasVirtualEquipments() ? INITIAL_MAP_CAPICITY : 0);

        if (physicalEquipments.isEmpty()) {
            break;
        }

        // manage physical equipments
        List<ImpactEquipementPhysique> impactEquipementPhysiqueList = evaluateNumEcoEvalService.calculatePhysicalEquipment(
                physicalEquipments.getFirst(), datacenters.getFirst(),
                subscriber, activeCriteria, lifecycleSteps, hypothesisRestList);


        ImpactEquipementPhysique impact  = impactEquipementPhysiqueList.getFirst();

        AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                impact.getQuantite(), impact.getConsoElecMoyenne(),
                impact.getImpactUnitaire(),
                refSip.get(impact.getCritere()),
                impact.getDureeDeVie(), null, null);

        aggregationPhysicalEquipments
                .computeIfAbsent(aggregationToOutput.keyPhysicalEquipment(physicalEquipments.getFirst(), datacenters.getFirst(), impact, refShortcutBO, evaluateReportBO.isDigitalService()),
                        k -> new AggValuesBO())
                .add(values);


        if (virtualEquipments.isEmpty()) {
            break;
        }

        // manage virtual equipments
        evaluateVirtualsEquipments(context, evaluateReportBO, physicalEquipments.getFirst(), virtualEquipments,aggregationVirtualEquipments,impactEquipementPhysiqueList,
                refSip, refShortcutBO, criteriaCodes, lifecycleSteps);


        final long currentTotal = (long) Constants.BATCH_SIZE * 1 + physicalEquipments.size();

        // set progress percentage, 0% to 90% is for this process, 90% to 100% is for compressing exports
        double processFactor = evaluateReportBO.isExport() ? 0.8 : 0.9;
        task.setProgressPercentage((int) Math.ceil(currentTotal * 100L * processFactor / totalEquipments) + "%");
        task.setLastUpdateDate(LocalDateTime.now());
        taskRepository.save(task);

        physicalEquipments.clear();

        //TODO : Save the result in db
        log.info("Saving aggregated indicators");
        // Store aggregated indicators
        int outPhysicalEquipmentSize = saveService.saveOutPhysicalEquipments(aggregationPhysicalEquipments, taskId, null);
        int outVirtualEquipmentSize = saveService.saveOutVirtualEquipments(aggregationVirtualEquipments, taskId, null);

        log.info("End evaluating impacts for {}/{} in {}s and sizes: {}/{}", context.log(), taskId,
                (System.currentTimeMillis() - start) / 1000,
                outPhysicalEquipmentSize, outVirtualEquipmentSize);
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
        return aiService.runEstimation(type, stage, aiConfigurationRest);
    }


    private void evaluatePhysicalEquipments(Context context,
                                            EvaluateReportBO evaluateReportBO,
                                            InPhysicalEquipment physicalEquipment,
                                            List<InVirtualEquipment> virtualEquipments,
                                            Map<List<String>, AggValuesBO> aggregationVirtualEquipments,
                                            List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                            Map<String, Double> refSip, RefShortcutBO refShortcutBO,
                                            final List<String> criteria, final List<String> lifecycleSteps

    ) throws IOException {

        if (!context.isHasVirtualEquipments()) return;

        Double totalVcpuCoreNumber = evaluateNumEcoEvalService.getTotalVcpuCoreNumber(virtualEquipments);
        Integer totalVpcuCore = totalVcpuCoreNumber == null ? null : totalVcpuCoreNumber.intValue();
        Double totalStorage = evaluateNumEcoEvalService.getTotalDiskSize(virtualEquipments);

        InVirtualEquipment virtualEquipment = virtualEquipments.getFirst();
        List<ImpactEquipementVirtuel> impactEquipementVirtuelList = evaluateNumEcoEvalService.calculateVirtualEquipment(
                virtualEquipment, impactEquipementPhysiqueList,
                virtualEquipments.size(), totalVpcuCore, totalStorage
        );

        String location = virtualEquipment.getLocation();

        ImpactEquipementVirtuel impact  = impactEquipementVirtuelList.getFirst();
        // Aggregate virtual equipment indicators in memory
        AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                virtualEquipment.getQuantity(), impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                refSip.get(impact.getCritere()),
                null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload());

        aggregationVirtualEquipments
                .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(physicalEquipment, virtualEquipment, impact, refShortcutBO, evaluateReportBO), k -> new AggValuesBO())
                .add(values);
    }

    private void evaluateVirtualsEquipments(Context context,
                                            EvaluateReportBO evaluateReportBO,
                                            InPhysicalEquipment physicalEquipment,
                                            List<InVirtualEquipment> virtualEquipments,
                                            Map<List<String>, AggValuesBO> aggregationVirtualEquipments,
                                            List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                            Map<String, Double> refSip, RefShortcutBO refShortcutBO,
                                            final List<String> criteria, final List<String> lifecycleSteps

    ) throws IOException {

        if (!context.isHasVirtualEquipments()) return;

        Double totalVcpuCoreNumber = evaluateNumEcoEvalService.getTotalVcpuCoreNumber(virtualEquipments);
        Integer totalVpcuCore = totalVcpuCoreNumber == null ? null : totalVcpuCoreNumber.intValue();
        Double totalStorage = evaluateNumEcoEvalService.getTotalDiskSize(virtualEquipments);

        InVirtualEquipment virtualEquipment = virtualEquipments.getFirst();
        List<ImpactEquipementVirtuel> impactEquipementVirtuelList = evaluateNumEcoEvalService.calculateVirtualEquipment(
                virtualEquipment, impactEquipementPhysiqueList,
                virtualEquipments.size(), totalVpcuCore, totalStorage
        );

        String location = virtualEquipment.getLocation();

        ImpactEquipementVirtuel impact  = impactEquipementVirtuelList.getFirst();
        // Aggregate virtual equipment indicators in memory
        AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                virtualEquipment.getQuantity(), impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                refSip.get(impact.getCritere()),
                null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload());

        aggregationVirtualEquipments
                .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(physicalEquipment, virtualEquipment, impact, refShortcutBO, evaluateReportBO), k -> new AggValuesBO())
                .add(values);
    }


    private AggValuesBO createAggValuesBO(String indicatorStatus,
                                          String trace,
                                          Double quantity,
                                          Double elecConsumption,
                                          Double unitImpact,
                                          Double sipValue,
                                          Double lifespan,
                                          Double usageDuration,
                                          Double workload) {

        boolean isOk = "OK".equals(indicatorStatus);

        String error = isOk ? null : trace;

        Double localQuantity = quantity == null ? 1d : quantity;
        Double impact;

        impact = unitImpact == null ? 0d : unitImpact;

        return AggValuesBO.builder()
                .countValue(1L)
                .unitImpact(impact)
                .peopleEqImpact(sipValue == null ? 0d : impact / sipValue)
                .electricityConsumption(elecConsumption == null ? 0d : elecConsumption)
                .quantity(localQuantity)
                .lifespan(lifespan == null ? 0d : lifespan * localQuantity)
                .usageDuration(usageDuration == null ? 0d : usageDuration)
                .workload(workload == null ? 0d : workload)
                .errors(error == null ? new HashSet<>() : new HashSet<>(List.of(error)))
                .build();
    }

    private BiMap<String, String> getShortcutMap(List<String> strings) {
        final BiMap<String, String> result = HashBiMap.create();
        for (int i = 0; i < strings.size(); i++) {
            result.put(strings.get(i), String.valueOf(i));
        }
        return result;
    }

}