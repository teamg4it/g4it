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
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.common.utils.InfrastructureType.CLOUD_SERVICES;

@Service
@Slf4j
public class EvaluateService {

    private static final int INITIAL_MAP_CAPACITY = 5_000;
    private static final int MAXIMUM_MAP_CAPACITY = 500_000;
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
    private Map<String, String> codeToCountryMapCache;
    private List<String> lifecycleStepsCache;
    private Map<Pair<String, String>, Integer> electricityMixQuartilesCache;

    @PostConstruct
    public void init() {
        codeToCountryMapCache =
                boaviztapiService.getCountryMap()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey
                        ));
        lifecycleStepsCache = referentialService.getLifecycleSteps();
        electricityMixQuartilesCache = referentialService.getElectricityMixQuartiles();
    }

    /**
     * Evaluate the inventory
     *
     * @param context         the context
     * @param task            the task
     * @param exportDirectory the export directory
     */
    public void doEvaluate(final Context context, final Task task, Path exportDirectory) {

        // retrieving the VM list for this DS
        Map<String, List<InVirtualEquipment>> vmsByPhysical =
                inVirtualEquipmentRepository
                        .findByDigitalServiceVersionUid(context.getDigitalServiceVersionUid())
                        .stream()
                        // ONLY VMs attached to a physical equipment
                        .filter(vm -> vm.getPhysicalEquipmentName() != null)
                        .collect(Collectors.groupingBy(InVirtualEquipment::getPhysicalEquipmentName));

        final Inventory inventory = task.getInventory();
        final String inventoryName = inventory == null ? context.getDigitalServiceName() : inventory.getName();
        final long start = System.currentTimeMillis();
        final String organization = context.getOrganization();
        final Long taskId = task.getId();

        // Get datacenters by name (name, InDatacenter)
        final Map<String, InDatacenter> datacenterByNameMap = context.getInventoryId() == null ?
                inDatacenterRepository.findByDigitalServiceVersionUid(context.getDigitalServiceVersionUid()).stream()
                        .collect(Collectors.toMap(InDatacenter::getName, Function.identity())) :
                inDatacenterRepository.findByInventoryId(context.getInventoryId()).stream()
                        .collect(Collectors.toMap(InDatacenter::getName, Function.identity()));

        // Match referential if needed, with cache
//        final List<String> lifecycleSteps = referentialService.getLifecycleSteps();
        final List<String> lifecycleSteps = lifecycleStepsCache;

        List<CriterionRest> activeCriteria = referentialService.getActiveCriteria(task.getCriteria().stream()
                .map(StringUtils::kebabToSnakeCase).toList());

        if (activeCriteria == null) return;

        List<String> criteriaCodes = activeCriteria.stream().map(CriterionRest::getCode).toList();

        // get (criterion, unit) map
        Map<String, String> criteriaUnitMap = activeCriteria.stream().collect(Collectors.toMap(
                CriterionRest::getCode,
                CriterionRest::getUnit
        ));

//        RefShortcutBO refShortcutBO = new RefShortcutBO(
//                criteriaUnitMap,
//                getShortcutMap(criteriaCodes),
//                getShortcutMap(lifecycleSteps),
//                referentialService.getElectricityMixQuartiles()
//        );
        RefShortcutBO refShortcutBO = new RefShortcutBO(
                criteriaUnitMap,
                getShortcutMap(criteriaCodes),
                getShortcutMap(lifecycleSteps),
                electricityMixQuartilesCache
        );

        final List<HypothesisRest> hypothesisRestList = referentialService.getHypotheses(organization);

        log.info("Start evaluating impacts for {}/{}", context.log(), taskId);

        Map<String, Double> refSip = referentialService.getSipValueMap(criteriaCodes);
//        Map<String, String> codeToCountryMap =
//                boaviztapiService.getCountryMap()
//                        .entrySet()
//                        .stream()
//                        .collect(Collectors.toMap(
//                                Map.Entry::getValue,
//                                Map.Entry::getKey
//                        ));
        Map<String, String> codeToCountryMap = codeToCountryMapCache;

        Map<List<String>, AggValuesBO> aggregationPhysicalEquipments = new HashMap<>(INITIAL_MAP_CAPACITY);
        Map<List<String>, AggValuesBO> aggregationVirtualEquipments = new HashMap<>(context.isHasVirtualEquipments() ? INITIAL_MAP_CAPACITY : 0);
        Map<List<String>, AggValuesBO> aggregationApplications = new HashMap<>(context.isHasApplications() ? INITIAL_MAP_CAPACITY : 0);


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
                        inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(context.getDigitalServiceVersionUid()) :
                        inPhysicalEquipmentRepository.countByInventoryId(context.getInventoryId());

        long totalCloudVirtualEquipments = context.getInventoryId() == null ?
                inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(context.getDigitalServiceVersionUid(), CLOUD_SERVICES.name()) :
                inVirtualEquipmentRepository.countByInventoryIdAndInfrastructureType(context.getInventoryId(), CLOUD_SERVICES.name());

        long totalEquipments = totalPhysicalEquipments + totalCloudVirtualEquipments;
        FileType physicalEquipmentIndicator = context.getDigitalServiceVersionUid() == null ? FileType.PHYSICAL_EQUIPMENT_INDICATOR :
                FileType.PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE;
        FileType virtualEquipmentIndicator = context.getDigitalServiceVersionUid() == null ? FileType.VIRTUAL_EQUIPMENT_INDICATOR :
                FileType.VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE;
        int outPhysicalEquipmentSize = 0;
        int outVirtualEquipmentSize = 0;
        int outApplicationSize = 0;
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
            SaveResult saveResult = evaluateVirtualsEquipments(context, evaluateReportBO, null, null,
                    aggregationVirtualEquipments, aggregationApplications,
                    csvInVirtualEquipment, csvVirtualEquipment, csvInApplication, csvApplication, refSip, refShortcutBO,
                    criteriaCodes, lifecycleSteps, codeToCountryMap/*, outVirtualEquipmentSize*/,
                    null, null);
            outVirtualEquipmentSize += saveResult.savedVirtualCount();
            outApplicationSize += saveResult.savedApplicationCount();

            int pageNumber = 0;
            final Sort sortByName = Sort.by("name");
            while (true) {
                Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, sortByName);
                final List<InPhysicalEquipment> physicalEquipments =
                        context.getInventoryId() == null ?
                                inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(context.getDigitalServiceVersionUid(), page) :
                                inPhysicalEquipmentRepository.findByInventoryId(context.getInventoryId(), page);

                if (physicalEquipments.isEmpty()) {
                    break;
                }

                log.info("Evaluating {} physical equipments, page {}/{}", physicalEquipments.size(), pageNumber + 1, (int) Math.ceil((double) totalPhysicalEquipments / Constants.BATCH_SIZE));
                int physicalSaveCounter = 0;
                for (InPhysicalEquipment physicalEquipment : physicalEquipments) {

                    if (aggregationPhysicalEquipments.size() > MAXIMUM_MAP_CAPACITY) {
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

                    Double equipmentPue = datacenter != null ? datacenter.getPue() : null;
                    String equipmentLocation = datacenter != null ? datacenter.getLocation() : null;

                    // Call external tools - lib calculs
                    List<ImpactEquipementPhysique> impactEquipementPhysiqueList = evaluateNumEcoEvalService.calculatePhysicalEquipment(
                            physicalEquipment, datacenter,
                            organization, activeCriteria, lifecycleSteps, hypothesisRestList);

                    // Identify NON-CLOUD VMs for this physical equipment
                    List<InVirtualEquipment> allVMs = vmsByPhysical.getOrDefault(physicalEquipment.getName(), List.of());

                    List<InVirtualEquipment> nonCloudVMs = allVMs.stream()
                            .filter(vm -> !CLOUD_SERVICES.name().equals(vm.getInfrastructureType()))
                            .toList();

                    boolean hasNonCloudVM = !nonCloudVMs.isEmpty();

                    if (evaluateReportBO.isExport()) {
                        csvInPhysicalEquipment.printRecord(inputToCsvRecord.toCsv(physicalEquipment, datacenter));
                    }

                    // Aggregate physical equipment indicators in memory
                    for (ImpactEquipementPhysique impact : impactEquipementPhysiqueList) {

                        Double sipValue = refSip.get(impact.getCritere());
                        AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                                impact.getQuantite(), impact.getConsoElecMoyenne(),
                                impact.getImpactUnitaire(),
                                sipValue,
                                impact.getDureeDeVie(), null, null, false);

                        aggregationPhysicalEquipments
                                .computeIfAbsent(aggregationToOutput.keyPhysicalEquipment(physicalEquipment, datacenter, impact, refShortcutBO, evaluateReportBO.isDigitalService()),
                                        k -> new AggValuesBO())
                                .add(values);

                        if (evaluateReportBO.isExport()) {
                            csvPhysicalEquipment.printRecord(impactToCsvRecord.toCsv(
                                    context, taskId, inventoryName, physicalEquipment, impact, sipValue, evaluateReportBO.isVerbose())
                            );
                        }

                        evaluateReportBO.setNbPhysicalEquipmentLines(evaluateReportBO.getNbPhysicalEquipmentLines() + 1);
                    }

                    /**
                     * ------------------------------------------------------------------
                     * VM RULE:
                     * A physical equipment must run VM calculations ONLY IF:
                     *    - It has ≥ 1 NON-CLOUD VM
                     * Cloud VMs do NOT count for this condition.
                     * ------------------------------------------------------------------
                     */
                    if (!hasNonCloudVM) {
                        log.info("Skipping VM calculation for physical equipment {} — contains no NON-cloud VMs", physicalEquipment.getName());
                        continue;
                    }

                    SaveResult saveResult2 = evaluateVirtualsEquipments(context, evaluateReportBO, physicalEquipment, impactEquipementPhysiqueList,
                            aggregationVirtualEquipments, aggregationApplications,
                            csvInVirtualEquipment, csvVirtualEquipment, csvInApplication, csvApplication,
                            refSip, refShortcutBO, criteriaCodes, lifecycleSteps, codeToCountryMap/*, outVirtualEquipmentSize*/,
                            equipmentPue, equipmentLocation);
                    outVirtualEquipmentSize += saveResult2.savedVirtualCount();
                    outApplicationSize += saveResult2.savedApplicationCount();

                    physicalSaveCounter++;
                    if (physicalSaveCounter >= 10) {
                        outPhysicalEquipmentSize += saveService.saveOutPhysicalEquipments(
                                aggregationPhysicalEquipments, taskId, refShortcutBO);
                        aggregationPhysicalEquipments = new HashMap<>(INITIAL_MAP_CAPACITY);
                        physicalSaveCounter = 0;
                    }
                }

                csvPhysicalEquipment.flush();
                csvVirtualEquipment.flush();
                csvApplication.flush();

                final long currentTotal = (long) Constants.BATCH_SIZE * pageNumber + physicalEquipments.size();

                // set progress percentage, 0% to 90% is for this process, 90% to 100% is for compressing exports
                double processFactor = evaluateReportBO.isExport() ? 0.8 : 0.9;
                taskRepository.updateProgress(
                        taskId,
                        (int) Math.ceil(currentTotal * 100L * processFactor / totalEquipments) + "%",
                        LocalDateTime.now()
                );

                pageNumber++;
                physicalEquipments.clear();
            }

        } catch (IOException e) {
            log.error("Cannot write csv output files", e);
            throw new AsyncTaskException("An error occurred on writing csv files", e);
        }

        log.info("Saving aggregated indicators");
        // Store aggregated indicators
        if (!aggregationPhysicalEquipments.isEmpty()) {
            outPhysicalEquipmentSize += saveService.saveOutPhysicalEquipments(
                    aggregationPhysicalEquipments, taskId, refShortcutBO);
            aggregationPhysicalEquipments.clear();
        }
        if (!aggregationVirtualEquipments.isEmpty()) {
            outVirtualEquipmentSize += saveService.saveOutVirtualEquipments(aggregationVirtualEquipments, taskId, refShortcutBO);
            aggregationVirtualEquipments.clear();
        }
        if (!aggregationApplications.isEmpty()) {
            outApplicationSize += saveService.saveOutApplications(aggregationApplications, taskId, refShortcutBO);
            aggregationApplications.clear();
        }

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

    // Returns number of virtual equipment records saved in this call (delta, not total)
    private SaveResult evaluateVirtualsEquipments(Context context, EvaluateReportBO evaluateReportBO,
                                                  InPhysicalEquipment physicalEquipment,
                                                  List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                                  Map<List<String>, AggValuesBO> aggregationVirtualEquipments,
                                                  Map<List<String>, AggValuesBO> aggregationApplications,
                                                  CSVPrinter csvInVirtualEquipment,
                                                  CSVPrinter csvVirtualEquipment,
                                                  CSVPrinter csvInApplication,
                                                  CSVPrinter csvApplication,
                                                  Map<String, Double> refSip, RefShortcutBO refShortcutBO,
                                                  final List<String> criteria, final List<String> lifecycleSteps,
                                                  Map<String, String> codeToCountryMap,
                                                  Double equipmentPue,
                                                  String equipmentLocation) throws IOException {

        if (!context.isHasVirtualEquipments()) return new SaveResult(0, 0);

        String physicalEquipmentName = physicalEquipment == null ? null : physicalEquipment.getName();

        int pageNumber = 0;
        int virtualSaveCounter = 0;

        int savedVirtualCount = 0;
        int savedApplicationCount = 0;
        final Sort sortByName = Sort.by("name");
        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, sortByName);
            List<InVirtualEquipment> virtualEquipments;
            if (context.getInventoryId() == null && physicalEquipmentName == null) {
                virtualEquipments = inVirtualEquipmentRepository
                        .findByDigitalServiceVersionUidAndPhysicalEquipmentNameIsNull(
                                context.getDigitalServiceVersionUid(), page);
            } else {
                virtualEquipments = context.getInventoryId() == null ?
                        inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndPhysicalEquipmentName(
                                context.getDigitalServiceVersionUid(), physicalEquipmentName, page) :
                        inVirtualEquipmentRepository.findByInventoryIdAndPhysicalEquipmentName(
                                context.getInventoryId(), physicalEquipmentName, page);
            }
            if (virtualEquipments.isEmpty()) {
                break;
            }
            Double totalVcpuCoreNumber =
                    evaluateNumEcoEvalService.getTotalVcpuCoreNumber(virtualEquipments);
            Double totalStorage =
                    evaluateNumEcoEvalService.getTotalDiskSize(virtualEquipments);
            int virtualSize = virtualEquipments.size();
            for (InVirtualEquipment virtualEquipment : virtualEquipments) {
                List<ImpactEquipementVirtuel> impactEquipementVirtuelList;
                Double cloudElectricityKwh = null;
                boolean isCloudService = CLOUD_SERVICES.name().equals(virtualEquipment.getInfrastructureType());
                if (isCloudService) {
                    List<ImpactBO> impactBOList = evaluateBoaviztapiService.evaluate(virtualEquipment, criteria, lifecycleSteps);
                    impactEquipementVirtuelList = internalToNumEcoEvalImpact.map(impactBOList);
                    cloudElectricityKwh =
                            boaviztapiService.getAnnualCloudElectricityKwh(virtualEquipment);
                } else {
                    impactEquipementVirtuelList = evaluateNumEcoEvalService.calculateVirtualEquipment(
                            virtualEquipment, impactEquipementPhysiqueList, virtualSize, totalVcpuCoreNumber, totalStorage,
                            equipmentPue, equipmentLocation
                    );
                }
                String location = isCloudService ? codeToCountryMap.get(virtualEquipment.getLocation()) : virtualEquipment.getLocation();
                if (evaluateReportBO.isExport()) {
                    csvInVirtualEquipment.printRecord(inputToCsvRecord.toCsv(virtualEquipment, location));
                }

                // Aggregate virtual equipment indicators in memory
                for (ImpactEquipementVirtuel impact : impactEquipementVirtuelList) {

                    Double sipValue = refSip.get(impact.getCritere());
                    AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                            virtualEquipment.getQuantity(),
                            isCloudService ? cloudElectricityKwh : impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                            sipValue,
                            null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload(), isCloudService);

                    aggregationVirtualEquipments
                            .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(physicalEquipment, virtualEquipment, impact, refShortcutBO, evaluateReportBO), k -> new AggValuesBO())
                            .add(values);

                    if (evaluateReportBO.isExport()) {
                        csvVirtualEquipment.printRecord(impactToCsvRecord.toCsv(
                                context, evaluateReportBO, virtualEquipment, impact, sipValue)
                        );
                    }

                    evaluateReportBO.setNbVirtualEquipmentLines(evaluateReportBO.getNbVirtualEquipmentLines() + 1);
                }

                if (aggregationVirtualEquipments.size() > MAXIMUM_MAP_CAPACITY) {
                    log.error("Exceeding aggregation size for virtual equipments");
                    throw new AsyncTaskException("Exceeding aggregation size for virtual equipments, please reduce criteria number");
                }

                virtualSaveCounter++;

                if (virtualSaveCounter >= 10) {
                    savedVirtualCount += saveService.saveOutVirtualEquipments(
                            aggregationVirtualEquipments, evaluateReportBO.getTaskId(), refShortcutBO);
                    aggregationVirtualEquipments.clear();
                    virtualSaveCounter = 0;
                }

                savedApplicationCount += this.evaluateApplications(context, evaluateReportBO, physicalEquipment, virtualEquipment, impactEquipementVirtuelList,
                        aggregationApplications, csvInApplication, csvApplication, refSip, refShortcutBO);
            }
            pageNumber++;
            if (pageNumber > 0 && pageNumber % 5 == 0) {
                csvVirtualEquipment.flush();
                csvApplication.flush();
            }
            virtualEquipments.clear();
        }

        if (!aggregationVirtualEquipments.isEmpty()) {
            savedVirtualCount += saveService.saveOutVirtualEquipments(
                    aggregationVirtualEquipments, evaluateReportBO.getTaskId(), refShortcutBO);
            aggregationVirtualEquipments.clear();
        }
        return new SaveResult(savedVirtualCount, savedApplicationCount);
    }

    private int evaluateApplications(Context context, EvaluateReportBO evaluateReportBO,
                                     InPhysicalEquipment physicalEquipment,
                                     InVirtualEquipment virtualEquipment,
                                     List<ImpactEquipementVirtuel> impactEquipementVirtuelList,
                                     Map<List<String>, AggValuesBO> aggregationApplications,
                                     CSVPrinter csvInApplication,
                                     CSVPrinter csvApplication,
                                     Map<String, Double> refSip, RefShortcutBO refShortcutBO) throws IOException {

        if (!context.isHasApplications()) return 0;
        int savedApplicationCount = 0;
        String physicalEquipmentName = physicalEquipment == null ? null : physicalEquipment.getName();

        List<InApplication> applicationList = inApplicationRepository.findByInventoryIdAndPhysicalEquipmentNameAndVirtualEquipmentName(context.getInventoryId(), physicalEquipmentName, virtualEquipment.getName());
        int applicationSaveCounter = 0;
        for (InApplication application : applicationList) {

            if (evaluateReportBO.isExport()) {
                csvInApplication.printRecord(inputToCsvRecord.toCsv(application));
            }

            List<ImpactApplication> impactApplicationList = evaluateNumEcoEvalService.calculateApplication(application, impactEquipementVirtuelList, applicationList.size());
            // Aggregate virtual equipment indicators in memory
            for (ImpactApplication impact : impactApplicationList) {

                Double sipValue = refSip.get(impact.getCritere());
                AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                        null, impact.getConsoElecMoyenne(), impact.getImpactUnitaire(),
                        sipValue,
                        null, null, null, false);

                aggregationApplications
                        .computeIfAbsent(aggregationToOutput.keyApplication(physicalEquipment, virtualEquipment, application, impact, refShortcutBO), k -> new AggValuesBO())
                        .add(values);

                if (evaluateReportBO.isExport()) {
                    csvApplication.printRecord(impactToCsvRecord.toCsv(
                            context, evaluateReportBO, application, impact, sipValue)
                    );
                }

                evaluateReportBO.setNbApplicationLines(evaluateReportBO.getNbApplicationLines() + 1);
            }

            if (aggregationApplications.size() > MAXIMUM_MAP_CAPACITY) {
                log.error("Exceeding aggregation size for applications");
                throw new AsyncTaskException("Exceeding aggregation size for applications, please reduce criteria number");
            }

            applicationSaveCounter++;

            if (applicationSaveCounter >= 10) {
                savedApplicationCount += saveService.saveOutApplications(
                        aggregationApplications,
                        evaluateReportBO.getTaskId(),
                        refShortcutBO
                );
                aggregationApplications.clear();
                applicationSaveCounter = 0;
            }
        }

        if (!aggregationApplications.isEmpty()) {
            savedApplicationCount += saveService.saveOutApplications(
                    aggregationApplications,
                    evaluateReportBO.getTaskId(),
                    refShortcutBO
            );
            aggregationApplications.clear();
        }
        return savedApplicationCount;
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
                .errors(error == null ? Collections.emptySet() : Collections.singleton(error))
                .build();
    }

    private BiMap<String, String> getShortcutMap(List<String> strings) {
        final int size = strings.size();
        final BiMap<String, String> result = HashBiMap.create(size);
        for (int i = 0; i < size; i++) {
            result.put(strings.get(i), String.valueOf(i));
        }
        return result;
    }

    record SaveResult(int savedVirtualCount, int savedApplicationCount) {
    }

}
