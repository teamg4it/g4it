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
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalImpact;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.EvaluateReportBO;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.*;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.common.utils.InfrastructureType.CLOUD_SERVICES;

@Service
@Slf4j
public class EvaluateService {

    private static final int INITIAL_MAP_CAPICITY = 50_000;
    private static final int MAXIMUM_MAP_CAPICITY = 500_000;
    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    InApplicationRepository inApplicationRepository;
    @Autowired
    AggregationToOutput aggregationToOutput;
    @Autowired
    ImpactToCsvRecord impactToCsvRecord;
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
    OutApplicationRepository outApplicationRepository;
    @Autowired
    CsvFileService csvFileService;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    InputToCsvRecord inputToCsvRecord;
    @Autowired
    EvaluateBoaviztapiService evaluateBoaviztapiService;
    @Autowired
    InternalToNumEcoEvalImpact internalToNumEcoEvalImpact;
    @Autowired
    BoaviztapiService boaviztapiService;
    @Value("${local.working.folder}")
    private String localWorkingFolder;

    /**
     * Evaluate the inventory
     *
     * @param context         the context
     * @param task            the task
     * @param exportDirectory the export directory
     */
    public void doEvaluate(final Context context, final Task task, Path exportDirectory) {

        final Inventory inventory = task.getInventory();
        final String inventoryName = inventory == null ? context.getDigitalServiceName() : inventory.getName();
        final long start = System.currentTimeMillis();
        final String subscriber = context.getSubscriber();
        final Long taskId = task.getId();

        // Get datacenters by name (name, InDatacenter)
        final Map<String, InDatacenter> datacenterByNameMap = context.getInventoryId() == null ?
                inDatacenterRepository.findByDigitalServiceUid(context.getDigitalServiceUid()).stream()
                        .collect(Collectors.toMap(InDatacenter::getName, Function.identity())) :
                inDatacenterRepository.findByInventoryId(context.getInventoryId()).stream()
                        .collect(Collectors.toMap(InDatacenter::getName, Function.identity()));

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

        log.info("Start evaluating impacts for {}/{}", context.log(), taskId);

        Map<String, Double> refSip = referentialService.getSipValueMap(criteriaCodes);

        Map<List<String>, AggValuesBO> aggregationPhysicalEquipments = new HashMap<>(INITIAL_MAP_CAPICITY);
        Map<List<String>, AggValuesBO> aggregationVirtualEquipments = new HashMap<>(context.isHasVirtualEquipments() ? INITIAL_MAP_CAPICITY : 0);
        Map<List<String>, AggValuesBO> aggregationApplications = new HashMap<>(context.isHasApplications() ? INITIAL_MAP_CAPICITY : 0);


        if (inventory != null && null == inventory.getDoExportVerbose()) {
            inventory.setDoExportVerbose(true);
        }
        EvaluateReportBO evaluateReportBO = EvaluateReportBO.builder()
                .export(true)
                .verbose(inventory == null || inventory.getDoExportVerbose())
                .isDigitalService(inventory == null)
                .nbPhysicalEquipmentLines(0)
                .nbVirtualEquipmentLines(0)
                .nbApplicationLines(0)
                .taskId(taskId)
                .name(inventoryName)
                .build();

        long totalPhysicalEquipments =
                context.getInventoryId() == null ?
                        inPhysicalEquipmentRepository.countByDigitalServiceUid(context.getDigitalServiceUid()) :
                        inPhysicalEquipmentRepository.countByInventoryId(context.getInventoryId());

        long totalCloudVirtualEquipments = context.getInventoryId() == null ?
                inVirtualEquipmentRepository.countByDigitalServiceUidAndInfrastructureType(context.getDigitalServiceUid(), CLOUD_SERVICES.name()) :
                inVirtualEquipmentRepository.countByInventoryIdAndInfrastructureType(context.getInventoryId(), CLOUD_SERVICES.name());

        long totalEquipments = totalPhysicalEquipments + totalCloudVirtualEquipments;
        FileType physicalEquipmentIndicator = context.getDigitalServiceUid() == null ? FileType.PHYSICAL_EQUIPMENT_INDICATOR :
                FileType.PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE;
        FileType virtualEquipmentIndicator = context.getDigitalServiceUid() == null ? FileType.VIRTUAL_EQUIPMENT_INDICATOR :
                FileType.VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE;
        try (CSVPrinter csvPhysicalEquipment = csvFileService.getPrinter(physicalEquipmentIndicator, exportDirectory);
             CSVPrinter csvVirtualEquipment = csvFileService.getPrinter(virtualEquipmentIndicator, exportDirectory);
             CSVPrinter csvApplication = csvFileService.getPrinter(FileType.APPLICATION_INDICATOR, exportDirectory);
             CSVPrinter csvInDatacenter = csvFileService.getPrinter(FileType.DATACENTER, exportDirectory);
             CSVPrinter csvInPhysicalEquipment = csvFileService.getPrinter(FileType.EQUIPEMENT_PHYSIQUE, exportDirectory);
             CSVPrinter csvInVirtualEquipment = csvFileService.getPrinter(FileType.VIRTUAL_EQUIPMENT, exportDirectory);
             CSVPrinter csvInApplication = csvFileService.getPrinter(FileType.APPLICATION, exportDirectory);
        ) {

            if (evaluateReportBO.isExport()) {
                for (InDatacenter inDatacenter : datacenterByNameMap.values()) {
                    csvInDatacenter.printRecord(inputToCsvRecord.toCsv(inDatacenter));
                }
            }

            // manage virtual equipments without physical equipments (cloud)
            evaluateVirtualsEquipments(context, evaluateReportBO, null, null,
                    aggregationVirtualEquipments, aggregationApplications,
                    csvInVirtualEquipment, csvVirtualEquipment, csvInApplication, csvApplication, refSip, refShortcutBO,
                    criteriaCodes, lifecycleSteps);

            int pageNumber = 0;
            while (true) {
                Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, Sort.by("name"));
                final List<InPhysicalEquipment> physicalEquipments =
                        context.getInventoryId() == null ?
                                inPhysicalEquipmentRepository.findByDigitalServiceUid(context.getDigitalServiceUid(), page) :
                                inPhysicalEquipmentRepository.findByInventoryId(context.getInventoryId(), page);

                if (physicalEquipments.isEmpty()) {
                    break;
                }

                log.info("Evaluating {} physical equipments, page {}/{}", physicalEquipments.size(), pageNumber + 1, (int) Math.ceil((double) totalPhysicalEquipments / Constants.BATCH_SIZE));

                for (InPhysicalEquipment physicalEquipment : physicalEquipments) {

                    if (aggregationPhysicalEquipments.size() > MAXIMUM_MAP_CAPICITY) {
                        log.error("Exceeding aggregation size for physical equipments");
                        throw new AsyncTaskException("Exceeding aggregation size for physical equipments, please reduce criteria number");
                    }

                    final InDatacenter datacenter = physicalEquipment.getDatacenterName() == null ?
                            null :
                            datacenterByNameMap.get(physicalEquipment.getDatacenterName());

                    if (datacenter != null) {
                        // force location into physicalEquipment
                        physicalEquipment.setLocation(datacenter.getLocation());
                    }

                    // Call external tools - lib calculs
                    List<ImpactEquipementPhysique> impactEquipementPhysiqueList = evaluateNumEcoEvalService.calculatePhysicalEquipment(
                            physicalEquipment, datacenter,
                            subscriber, activeCriteria, lifecycleSteps, hypothesisRestList);

                    if (evaluateReportBO.isExport()) {
                        csvInPhysicalEquipment.printRecord(inputToCsvRecord.toCsv(physicalEquipment, datacenter));
                    }

                    // Aggregate physical equipment indicators in memory
                    for (ImpactEquipementPhysique impact : impactEquipementPhysiqueList) {

                        AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                                impact.getQuantite(), impact.getConsoElecMoyenne(),
                                impact.getImpactUnitaire(),
                                refSip.get(impact.getCritere()),
                                impact.getDureeDeVie(), null, null, false);

                        aggregationPhysicalEquipments
                                .computeIfAbsent(aggregationToOutput.keyPhysicalEquipment(physicalEquipment, datacenter, impact, refShortcutBO, evaluateReportBO.isDigitalService()),
                                        k -> new AggValuesBO())
                                .add(values);

                        if (evaluateReportBO.isExport()) {
                            csvPhysicalEquipment.printRecord(impactToCsvRecord.toCsv(
                                    context, taskId, inventoryName, physicalEquipment, impact, refSip.get(impact.getCritere()), evaluateReportBO.isVerbose())
                            );
                        }

                        evaluateReportBO.setNbPhysicalEquipmentLines(evaluateReportBO.getNbVirtualEquipmentLines() + 1);
                    }

                    evaluateVirtualsEquipments(context, evaluateReportBO, physicalEquipment, impactEquipementPhysiqueList,
                            aggregationVirtualEquipments, aggregationApplications,
                            csvInVirtualEquipment, csvVirtualEquipment, csvInApplication, csvApplication,
                            refSip, refShortcutBO, criteriaCodes, lifecycleSteps);
                }

                csvPhysicalEquipment.flush();
                csvVirtualEquipment.flush();
                csvApplication.flush();

                final long currentTotal = (long) Constants.BATCH_SIZE * pageNumber + physicalEquipments.size();

                // set progress percentage, 0% to 90% is for this process, 90% to 100% is for compressing exports
                double processFactor = evaluateReportBO.isExport() ? 0.8 : 0.9;
                task.setProgressPercentage((int) Math.ceil(currentTotal * 100L * processFactor / totalEquipments) + "%");
                task.setLastUpdateDate(LocalDateTime.now());
                taskRepository.save(task);

                pageNumber++;
                physicalEquipments.clear();
            }

        } catch (IOException e) {
            log.error("Cannot write csv output files", e);
            throw new AsyncTaskException("An error occurred on writing csv files", e);
        }

        log.info("Saving aggregated indicators");
        // Store aggregated indicators
        int outPhysicalEquipmentSize = saveService.saveOutPhysicalEquipments(aggregationPhysicalEquipments, taskId, refShortcutBO);
        int outVirtualEquipmentSize = saveService.saveOutVirtualEquipments(aggregationVirtualEquipments, taskId, refShortcutBO);
        int outApplicationSize = saveService.saveOutApplications(aggregationApplications, taskId, refShortcutBO);

        log.info("End evaluating impacts for {}/{} in {}s and sizes: {}/{}/{}", context.log(), taskId,
                (System.currentTimeMillis() - start) / 1000,
                outPhysicalEquipmentSize, outVirtualEquipmentSize, outApplicationSize);

        // clean files if empty
        try {
            if (!evaluateReportBO.isExport()) {
                Files.deleteIfExists(exportDirectory.resolve(FileType.DATACENTER.getFileName() + Constants.CSV));
            }
            if (evaluateReportBO.getNbPhysicalEquipmentLines() == 0 || !evaluateReportBO.isExport()) {
                Files.deleteIfExists(exportDirectory.resolve(FileType.PHYSICAL_EQUIPMENT_INDICATOR.getFileName() + Constants.CSV));
                Files.deleteIfExists(exportDirectory.resolve(FileType.EQUIPEMENT_PHYSIQUE.getFileName() + Constants.CSV));
            }
            if (evaluateReportBO.getNbVirtualEquipmentLines() == 0 || !evaluateReportBO.isExport()) {
                Files.deleteIfExists(exportDirectory.resolve(FileType.VIRTUAL_EQUIPMENT_INDICATOR.getFileName() + Constants.CSV));
                Files.deleteIfExists(exportDirectory.resolve(FileType.EQUIPEMENT_VIRTUEL.getFileName() + Constants.CSV));
            }
            if (evaluateReportBO.getNbApplicationLines() == 0 || !evaluateReportBO.isExport()) {
                Files.deleteIfExists(exportDirectory.resolve(FileType.APPLICATION_INDICATOR.getFileName() + Constants.CSV));
                Files.deleteIfExists(exportDirectory.resolve(FileType.APPLICATION.getFileName() + Constants.CSV));
            }
        } catch (IOException e) {
            log.error("Cannot delete export local files", e);
            throw new AsyncTaskException("An error occurred on deleting empty csv files", e);
        }
    }

    private void evaluateVirtualsEquipments(Context context, EvaluateReportBO evaluateReportBO,
                                            InPhysicalEquipment physicalEquipment,
                                            List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                            Map<List<String>, AggValuesBO> aggregationVirtualEquipments,
                                            Map<List<String>, AggValuesBO> aggregationApplications,
                                            CSVPrinter csvInVirtualEquipment,
                                            CSVPrinter csvVirtualEquipment,
                                            CSVPrinter csvInApplication,
                                            CSVPrinter csvApplication,
                                            Map<String, Double> refSip, RefShortcutBO refShortcutBO,
                                            final List<String> criteria, final List<String> lifecycleSteps) throws IOException {

        if (!context.isHasVirtualEquipments()) return;

        String physicalEquipmentName = physicalEquipment == null ? null : physicalEquipment.getName();

        int pageNumber = 0;
        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, Sort.by("name"));
            List<InVirtualEquipment> virtualEquipments = context.getInventoryId() == null ?
                    inVirtualEquipmentRepository.findByDigitalServiceUidAndPhysicalEquipmentName(context.getDigitalServiceUid(), physicalEquipmentName, page) :
                    inVirtualEquipmentRepository.findByInventoryIdAndPhysicalEquipmentName(context.getInventoryId(), physicalEquipmentName, page);

            if (virtualEquipments.isEmpty()) {
                break;
            }
            Map<String, String> countryMap = boaviztapiService.getCountryMap();

            // Reverse the map to create a code-to-countryLabel mapping
            Map<String, String> codeToCountryMap = countryMap.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

            for (InVirtualEquipment virtualEquipment : virtualEquipments) {
                List<ImpactEquipementVirtuel> impactEquipementVirtuelList;
                boolean isCloudService = CLOUD_SERVICES.name().equals(virtualEquipment.getInfrastructureType());
                if (isCloudService) {
                    List<ImpactBO> impactBOList = evaluateBoaviztapiService.evaluate(virtualEquipment, criteria, lifecycleSteps);
                    impactEquipementVirtuelList = internalToNumEcoEvalImpact.map(impactBOList);

                } else {
                    Double totalVcpuCoreNumber = evaluateNumEcoEvalService.getTotalVcpuCoreNumber(virtualEquipments);
                    Integer totalVpcuCore = totalVcpuCoreNumber == null ? null : totalVcpuCoreNumber.intValue();
                    Double totalStorage = evaluateNumEcoEvalService.getTotalDiskSize(virtualEquipments);

                    impactEquipementVirtuelList = evaluateNumEcoEvalService.calculateVirtualEquipment(
                            virtualEquipment, impactEquipementPhysiqueList,
                            virtualEquipments.size(), totalVpcuCore, totalStorage
                    );
                }
                String location = isCloudService ? codeToCountryMap.get(virtualEquipment.getLocation()) : virtualEquipment.getLocation();
                if (evaluateReportBO.isExport()) {
                    csvInVirtualEquipment.printRecord(inputToCsvRecord.toCsv(virtualEquipment, location));
                }

                // Aggregate virtual equipment indicators in memory
                for (ImpactEquipementVirtuel impact : impactEquipementVirtuelList) {

                    AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                            virtualEquipment.getQuantity(), impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                            refSip.get(impact.getCritere()),
                            null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload(), isCloudService);

                    aggregationVirtualEquipments
                            .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(physicalEquipment, virtualEquipment, impact, refShortcutBO, evaluateReportBO), k -> new AggValuesBO())
                            .add(values);

                    if (evaluateReportBO.isExport()) {
                        csvVirtualEquipment.printRecord(impactToCsvRecord.toCsv(
                                context, evaluateReportBO, virtualEquipment, impact, refSip.get(impact.getCritere()))
                        );
                    }

                    evaluateReportBO.setNbVirtualEquipmentLines(evaluateReportBO.getNbVirtualEquipmentLines() + 1);
                }

                if (aggregationVirtualEquipments.size() > MAXIMUM_MAP_CAPICITY) {
                    log.error("Exceeding aggregation size for virtual equipments");
                    throw new AsyncTaskException("Exceeding aggregation size for virtual equipments, please reduce criteria number");
                }

                this.evaluateApplications(context, evaluateReportBO, physicalEquipment, virtualEquipment, impactEquipementVirtuelList,
                        aggregationApplications, csvInApplication, csvApplication, refSip, refShortcutBO);
            }
            csvVirtualEquipment.flush();
            csvApplication.flush();
            pageNumber++;
            virtualEquipments.clear();
        }
    }

    private void evaluateApplications(Context context, EvaluateReportBO evaluateReportBO,
                                      InPhysicalEquipment physicalEquipment,
                                      InVirtualEquipment virtualEquipment,
                                      List<ImpactEquipementVirtuel> impactEquipementVirtuelList,
                                      Map<List<String>, AggValuesBO> aggregationApplications,
                                      CSVPrinter csvInApplication,
                                      CSVPrinter csvApplication,
                                      Map<String, Double> refSip, RefShortcutBO refShortcutBO) throws IOException {

        if (!context.isHasApplications()) return;
        String physicalEquipmentName = physicalEquipment == null ? null : physicalEquipment.getName();

        List<InApplication> applicationList = inApplicationRepository.findByInventoryIdAndPhysicalEquipmentNameAndVirtualEquipmentName(context.getInventoryId(), physicalEquipmentName, virtualEquipment.getName());

        for (InApplication application : applicationList) {

            if (evaluateReportBO.isExport()) {
                csvInApplication.printRecord(inputToCsvRecord.toCsv(application));
            }

            List<ImpactApplication> impactApplicationList = evaluateNumEcoEvalService.calculateApplication(application, impactEquipementVirtuelList, applicationList.size());
            // Aggregate virtual equipment indicators in memory
            for (ImpactApplication impact : impactApplicationList) {

                AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                        null, impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                        refSip.get(impact.getCritere()),
                        null, null, null, false);

                aggregationApplications
                        .computeIfAbsent(aggregationToOutput.keyApplication(physicalEquipment, virtualEquipment, application, impact, refShortcutBO), k -> new AggValuesBO())
                        .add(values);

                if (evaluateReportBO.isExport()) {
                    csvApplication.printRecord(impactToCsvRecord.toCsv(
                            context, evaluateReportBO, application, impact, refSip.get(impact.getCritere()))
                    );
                }

                evaluateReportBO.setNbApplicationLines(evaluateReportBO.getNbApplicationLines() + 1);
            }

            if (aggregationApplications.size() > MAXIMUM_MAP_CAPICITY) {
                log.error("Exceeding aggregation size for applications");
                throw new AsyncTaskException("Exceeding aggregation size for applications, please reduce criteria number");
            }
        }
    }

    /**
     * Create AggValuesBO from params with default values
     *
     * @param indicatorStatus the indicator status
     * @param trace           the trace
     * @param quantity        the quantity
     * @param elecConsumption the electricity consumption
     * @param unitImpact      the unit impact
     * @param sipValue        the sip value
     * @param lifespan        the lifespan
     * @return the agg value
     */
    private AggValuesBO createAggValuesBO(String indicatorStatus,
                                          String trace,
                                          Double quantity,
                                          Double elecConsumption,
                                          Double unitImpact,
                                          Double sipValue,
                                          Double lifespan,
                                          Double usageDuration,
                                          Double workload, Boolean isCloudService) {

        boolean isOk = "OK".equals(indicatorStatus);

        String error = isOk ? null : trace;

        Double localQuantity = quantity == null ? 1d : quantity;
        Double impact;

        if (isCloudService) {
            impact = unitImpact == null ? 0d : unitImpact * localQuantity;
        } else {
            impact = unitImpact == null ? 0d : unitImpact;
        }

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
