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
import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.EvaluateReportBO;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.InAiParameterRepository;
import com.soprasteria.g4it.backend.apirecomandation.mapper.RecommendationJsonMapper;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIServiceEstimationBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.HypothesisRest;
import lombok.extern.slf4j.Slf4j;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    TaskRepository taskRepository;

    @Autowired
    AiService aiService;

    @Autowired
    AiConfigurationMapper aiConfigurationMapper;

    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    OutAiRecoRepository outAiRecoRepository;

    @Autowired
    InAiParameterRepository inAIParameterRepository;

    @Autowired
    AggregationToOutput aggregationToOutput;

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

        final String subscriber = context.getSubscriber();
        final long start = System.currentTimeMillis();
        final Long taskId = task.getId();

        // get the data in database
        // Get the service digital
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(context.getDigitalServiceUid());
        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", context.getDigitalServiceUid()));
        }
        // Get the AI parameters
        InAiParameter inAiParameters = inAIParameterRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        if (inAiParameters == null) {
            throw new G4itRestException("404", String.format("the ai parameter doesn't exist for digital service : %s", context.getDigitalServiceUid()));
        }
        // Get the data center
        List<InDatacenter> datacenters = inDatacenterRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        if (datacenters.isEmpty()) {
            throw new G4itRestException("404", String.format("the data center doesn't exist for digital service : %s", context.getDigitalServiceUid()));
        }
        // Get the physical equipment
        List<InPhysicalEquipment> physicalEquipments = inPhysicalEquipmentRepository.findByDigitalServiceUid(context.getDigitalServiceUid());
        if (physicalEquipments.isEmpty()) {
            throw new G4itRestException("404", String.format("the physical equipements doesn't exist for digital service : %s", context.getDigitalServiceUid()));
        }
        // Get the virtual equipment
        List<InVirtualEquipment> virtualEquipments = inVirtualEquipmentRepository.findByDigitalServiceUid(context.getDigitalServiceUid());
        if (virtualEquipments.isEmpty()) {
            throw new G4itRestException("404", String.format("the virtual equipements doesn't exist for digital service : %s", context.getDigitalServiceUid()));
        }
        log.info("Retrieved digital service and AI parameters");
        log.info("Retrieved {} datacenters, {} physical equipments, {} virtual equipments",
                datacenters.size(), physicalEquipments.size(), virtualEquipments.size());

        // call Ecomind with the data
        List<AIServiceEstimationBO> estimationBOList = evaluateEcomind(inAiParameters);
        AIServiceEstimationBO estimationBO = estimationBOList.getFirst();

        // save the result of the call in db
        InPhysicalEquipment inPhysicalEquipment = physicalEquipments.getFirst();
        inPhysicalEquipment.setElectricityConsumption(estimationBO.getElectricityConsumption().doubleValue());
        inPhysicalEquipmentRepository.save(inPhysicalEquipment);

        InVirtualEquipment inVirtualEquipment = virtualEquipments.getFirst();
        inVirtualEquipment.setElectricityConsumption(estimationBO.getElectricityConsumption().doubleValue());
        inVirtualEquipmentRepository.save(inVirtualEquipment);

        final LocalDateTime now = LocalDateTime.now();
        OutAiReco outAiReco = OutAiReco.builder().build();
        outAiReco.setElectricityConsumption(estimationBO.getElectricityConsumption().doubleValue());
        outAiReco.setRuntime(estimationBO.getRuntime().longValue());

        String recommendationsJson = RecommendationJsonMapper.toJson(estimationBO.getRecommendations());
        outAiReco.setRecommendations(recommendationsJson);
        outAiReco.setDigitalServiceUid(context.getDigitalServiceUid());
        outAiReco.setCreationDate(now);
        outAiReco.setLastUpdateDate(now);
        outAiReco.setTaskId(taskId);
        outAiRecoRepository.save(outAiReco);

        // Call numecoeval

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

        log.info("manage physical equipments");
        // manage physical equipments
        List<ImpactEquipementPhysique> impactEquipementPhysiqueList = evaluateNumEcoEvalService.calculatePhysicalEquipment(
                physicalEquipments.getFirst(), datacenters.getFirst(),
                subscriber, activeCriteria, lifecycleSteps, hypothesisRestList);


        // Aggregate physical equipment indicators in memory
        for (ImpactEquipementPhysique impact : impactEquipementPhysiqueList) {

            AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                    impact.getQuantite(), impact.getConsoElecMoyenne(),
                    impact.getImpactUnitaire(),
                    refSip.get(impact.getCritere()),
                    impact.getDureeDeVie(), null, null);

            aggregationPhysicalEquipments
                    .computeIfAbsent(aggregationToOutput.keyPhysicalEquipment(physicalEquipments.getFirst(), datacenters.getFirst(), impact, refShortcutBO, evaluateReportBO.isDigitalService()),
                            k -> new AggValuesBO())
                    .add(values);

            log.info("manage virtual equipments");
            // manage virtual equipments
            evaluateVirtualsEquipments(context, evaluateReportBO, physicalEquipments.getFirst(), virtualEquipments, aggregationVirtualEquipments, impactEquipementPhysiqueList,
                    refSip, refShortcutBO, criteriaCodes, lifecycleSteps);

        }

        task.setProgressPercentage("100%");
        task.setLastUpdateDate(LocalDateTime.now());
        taskRepository.save(task);

        physicalEquipments.clear();

        // Save the result in db
        log.info("Saving aggregated indicators");
        // Store aggregated indicators
        int outPhysicalEquipmentSize = saveService.saveOutPhysicalEquipments(aggregationPhysicalEquipments, taskId, refShortcutBO);
        int outVirtualEquipmentSize = saveService.saveOutVirtualEquipments(aggregationVirtualEquipments, taskId, refShortcutBO);

        log.info("End evaluating impacts for {}/{} in {}s and sizes: {}/{}", context.log(), taskId,
                (System.currentTimeMillis() - start) / 1000,
                outPhysicalEquipmentSize, outVirtualEquipmentSize);

    }

    private List<AIServiceEstimationBO> evaluateEcomind(InAiParameter inAiParameter) throws IOException {
        AIConfigurationBO aiConfigurationBO = AIConfigurationBO.builder().build();
        aiConfigurationBO.setFramework(inAiParameter.getFramework());
        aiConfigurationBO.setModelName(inAiParameter.getModelName());
        aiConfigurationBO.setQuantization(inAiParameter.getQuantization());
        aiConfigurationBO.setNbParameters(inAiParameter.getNbParameters());
        aiConfigurationBO.setTotalGeneratedTokens(inAiParameter.getTotalGeneratedTokens().longValue());
        List<AIConfigurationRest> aiConfigurationRest = aiConfigurationMapper.toAIModelConfigRest(List.of(aiConfigurationBO));
        String stage = inAiParameter.getIsInference() ? "INFERENCE" : "TRAINING";
        String type = inAiParameter.getType();
        return aiService.runEstimation(type, stage, aiConfigurationRest);
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

        // Aggregate virtual equipment indicators in memory
        for (ImpactEquipementVirtuel impact : impactEquipementVirtuelList) {
            AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                    virtualEquipment.getQuantity(), impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                    refSip.get(impact.getCritere()),
                    null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload());

            aggregationVirtualEquipments
                    .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(physicalEquipment, virtualEquipment, impact, refShortcutBO, evaluateReportBO), k -> new AggValuesBO())
                    .add(values);
        }

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