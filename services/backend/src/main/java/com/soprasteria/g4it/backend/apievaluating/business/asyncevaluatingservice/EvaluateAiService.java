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
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.EvaluateReportBO;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
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
import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIServiceEstimationBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.HypothesisRest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Autowired
    CsvFileService csvFileService;

    @Autowired
    InputToCsvRecord inputToCsvRecord;

    @Autowired
    ImpactToCsvRecord impactToCsvRecord;

    @Autowired
    InAiInfrastructureRepository inAiInfrastructureRepository;

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
        final String digitalServiceName = context.getDigitalServiceName();

        // get the data in database
        // Get the AI parameters
        InAiParameter inAiParameters = inAIParameterRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        if (inAiParameters == null) {
            throw new G4itRestException("404", String.format("the ai parameter doesn't exist for digital service : %s", context.getDigitalServiceUid()));
        }
        // Get AI infrastructure
        InAiInfrastructure inAiInfrastructure = inAiInfrastructureRepository.findByDigitalServiceUid(context.getDigitalServiceUid());

        if (inAiInfrastructure == null) {
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

        final List<HypothesisRest> hypothesisRestList = referentialService.getHypotheses(subscriber);

        Map<String, Double> refSip = referentialService.getSipValueMap(criteriaCodes);

        log.info("Start evaluating impacts for {}/{}", context.log(), taskId);

        EvaluateReportBO evaluateReportBO = EvaluateReportBO.builder()
                .export(true)
                .isDigitalService(true)
                .nbPhysicalEquipmentLines(1)
                .nbVirtualEquipmentLines(1)
                .nbApplicationLines(0)
                .taskId(taskId)
                .name(digitalServiceName)
                .build();

        Map<List<String>, AggValuesBO> aggregationPhysicalEquipments = new HashMap<>(INITIAL_MAP_CAPICITY);
        Map<List<String>, AggValuesBO> aggregationVirtualEquipments = new HashMap<>(context.isHasVirtualEquipments() ? INITIAL_MAP_CAPICITY : 0);

        try (CSVPrinter csvInDatacenter = csvFileService.getPrinter(FileType.DATACENTER, exportDirectory);
             CSVPrinter csvInPhysicalEquipment = csvFileService.getPrinter(FileType.EQUIPEMENT_PHYSIQUE, exportDirectory);
             CSVPrinter csvInVirtualEquipment = csvFileService.getPrinter(FileType.VIRTUAL_EQUIPMENT, exportDirectory);
             CSVPrinter csvInAiParameters = csvFileService.getPrinter(FileType.IN_AI_PARAMETERS, exportDirectory);
             CSVPrinter csvInAiInfrastructure = csvFileService.getPrinter(FileType.IN_AI_INFRASTRUCTURE, exportDirectory);
             CSVPrinter csvOutPhysicalEquipment = csvFileService.getPrinter(FileType.PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE, exportDirectory);
             CSVPrinter csvOutVirtualEquipment = csvFileService.getPrinter(FileType.VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE, exportDirectory);
             CSVPrinter csvOutAiReco = csvFileService.getPrinter(FileType.OUT_AI_RECO, exportDirectory);
        ) {
            if (evaluateReportBO.isExport()) {
                for (InDatacenter inDatacenter : datacenters) {
                    csvInDatacenter.printRecord(inputToCsvRecord.toCsv(inDatacenter));
                }
                // ai data
                csvInAiParameters.printRecord(inputToCsvRecord.toCsv(inAiParameters));
                csvInAiInfrastructure.printRecord(inputToCsvRecord.toCsv(inAiInfrastructure));
                csvOutAiReco.printRecord(impactToCsvRecord.toCsv(outAiReco));
            }

            while (!physicalEquipments.isEmpty()) {

                log.info("Evaluating {} physical equipments", physicalEquipments.size());

                for (InPhysicalEquipment inPhysicalEq : physicalEquipments) {
                    if (aggregationPhysicalEquipments.size() > MAXIMUM_MAP_CAPICITY) {
                        log.error("Exceeding aggregation size for physical equipments");
                        throw new AsyncTaskException("Exceeding aggregation size for physical equipments, please reduce criteria number");
                    }
                    final InDatacenter datacenter = inPhysicalEq.getDatacenterName() == null ?
                            null :
                            datacenters.stream().filter(inDatacenter -> inDatacenter.getName().equals(inPhysicalEq.getDatacenterName())).toList().getFirst();

                    if (datacenter != null) {
                        // force location into physicalEquipment
                        inPhysicalEq.setLocation(datacenter.getLocation());
                    }

                    List<ImpactEquipementPhysique> impactEquipementPhysiqueList = evaluateNumEcoEvalService.calculatePhysicalEquipment(
                            inPhysicalEq, datacenters.getFirst(),
                            subscriber, activeCriteria, lifecycleSteps, hypothesisRestList);

                    if (evaluateReportBO.isExport()) {
                        csvInPhysicalEquipment.printRecord(inputToCsvRecord.toCsv(inPhysicalEq, datacenter));
                    }

                    // Aggregate physical equipment indicators in memory
                    for (ImpactEquipementPhysique impact : impactEquipementPhysiqueList) {

                        AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                                impact.getQuantite(), impact.getConsoElecMoyenne(),
                                impact.getImpactUnitaire(),
                                refSip.get(impact.getCritere()),
                                impact.getDureeDeVie(), null, null);

                        aggregationPhysicalEquipments
                                .computeIfAbsent(aggregationToOutput.keyPhysicalEquipment(inPhysicalEq, datacenters.getFirst(), impact, refShortcutBO, evaluateReportBO.isDigitalService()),
                                        k -> new AggValuesBO())
                                .add(values);

                        if (evaluateReportBO.isExport()) {
                            csvOutPhysicalEquipment.printRecord(impactToCsvRecord.toCsv(
                                    context, taskId, digitalServiceName, inPhysicalEq, impact, refSip.get(impact.getCritere()), evaluateReportBO.isVerbose())
                            );
                        }

                        evaluateReportBO.setNbPhysicalEquipmentLines(evaluateReportBO.getNbVirtualEquipmentLines() + 1);
                    }

                    evaluateVirtualsEquipments(context, evaluateReportBO, inPhysicalEq, virtualEquipments, aggregationVirtualEquipments, impactEquipementPhysiqueList,
                            refSip, refShortcutBO, csvInVirtualEquipment, csvOutVirtualEquipment);

                }

                csvOutPhysicalEquipment.flush();
                csvOutVirtualEquipment.flush();

                task.setProgressPercentage("100%");
                task.setLastUpdateDate(LocalDateTime.now());
                taskRepository.save(task);

                physicalEquipments.clear();

            }

        } catch (IOException e) {
            log.error("Cannot write csv output files", e);
            throw new AsyncTaskException("An error occurred on writing csv files", e);
        }

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
                                            CSVPrinter csvInVirtualEquipment,
                                            CSVPrinter csvOutVirtualEquipment) throws IOException {

        if (!context.isHasVirtualEquipments()) return;

        while (!virtualEquipments.isEmpty()) {
            for (InVirtualEquipment virtualEquipment : virtualEquipments) {
                Double totalVcpuCoreNumber = evaluateNumEcoEvalService.getTotalVcpuCoreNumber(virtualEquipments);
                Integer totalVpcuCore = totalVcpuCoreNumber == null ? null : totalVcpuCoreNumber.intValue();
                Double totalStorage = evaluateNumEcoEvalService.getTotalDiskSize(virtualEquipments);

                List<ImpactEquipementVirtuel> impactEquipementVirtuelList = evaluateNumEcoEvalService.calculateVirtualEquipment(
                        virtualEquipment, impactEquipementPhysiqueList,
                        virtualEquipments.size(), totalVpcuCore, totalStorage
                );

                String location = virtualEquipment.getLocation();

                if (evaluateReportBO.isExport()) {
                    csvInVirtualEquipment.printRecord(inputToCsvRecord.toCsv(virtualEquipment, location));
                }

                // Aggregate virtual equipment indicators in memory
                for (ImpactEquipementVirtuel impact : impactEquipementVirtuelList) {
                    AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                            virtualEquipment.getQuantity(), impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                            refSip.get(impact.getCritere()),
                            null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload());

                    aggregationVirtualEquipments
                            .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(physicalEquipment, virtualEquipment, impact, refShortcutBO, evaluateReportBO), k -> new AggValuesBO())
                            .add(values);

                    if (evaluateReportBO.isExport()) {
                        csvOutVirtualEquipment.printRecord(impactToCsvRecord.toCsv(
                                context, evaluateReportBO, virtualEquipment, impact, refSip.get(impact.getCritere()))
                        );
                    }
                    evaluateReportBO.setNbVirtualEquipmentLines(evaluateReportBO.getNbVirtualEquipmentLines() + 1);
                }

                if (aggregationVirtualEquipments.size() > MAXIMUM_MAP_CAPICITY) {
                    log.error("Exceeding aggregation size for virtual equipments");
                    throw new AsyncTaskException("Exceeding aggregation size for virtual equipments, please reduce criteria number");
                }
            }
            csvOutVirtualEquipment.flush();
            virtualEquipments.clear();
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