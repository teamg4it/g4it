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
import com.soprasteria.g4it.backend.apievaluating.model.*;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.*;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
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
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.common.utils.InfrastructureType.CLOUD_SERVICES;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.ecologits.EvaluateEcologitsService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AiServiceImpactToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.mapper.AiServiceToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutAiService;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvaluateService {

    private static final int INITIAL_MAP_CAPACITY = 5_000;
    private static final int MAXIMUM_MAP_CAPACITY = 500_000;
    private final InDatacenterRepository inDatacenterRepository;
    private final InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    private final InVirtualEquipmentRepository inVirtualEquipmentRepository;
    private final InApplicationRepository inApplicationRepository;
    private final AggregationToOutput aggregationToOutput;
    private final ImpactToCsvRecord impactToCsvRecord;
    private final RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository;
    private final EvaluateNumEcoEvalService evaluateNumEcoEvalService;
    private final ReferentialService referentialService;
    private final SaveService saveService;
    private final CsvFileService csvFileService;
    private final TaskRepository taskRepository;
    private final InputToCsvRecord inputToCsvRecord;
    private final EvaluateBoaviztapiService evaluateBoaviztapiService;
    private final InternalToNumEcoEvalImpact internalToNumEcoEvalImpact;
    private final BoaviztapiService boaviztapiService;
    private final InventoryRepository inventoryRepository;
    private final InAiServiceRepository inAiServiceRepository;
    private final AiServiceImpactToCsvRecord aiServiceImpactToCsvRecord;
    private final AiServiceToCsvRecord aiServiceToCsvRecord;
    private final EvaluateEcologitsService evaluateEcologitsService;
    private final Clock clock;

    @Value("${local.working.folder}")
    private String localWorkingFolder;
    @Value("${ecologits.version}")
    private String ecologitsVersion;

    private Map<String, String> codeToCountryMapCache;
    private List<String> lifecycleStepsCache;
    private Map<Pair<String, String>, Integer> electricityMixQuartilesCache;
    private Map<String, String> countryNameToCodeMapCache;
    private final ReferentialGetService referentialGetService;

    @PostConstruct
    public void init() {
        Map<String, String> countryMap = boaviztapiService.getCountryMap();
        codeToCountryMapCache =
                countryMap
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey
                        ));
        // country name (case-insensitive) -> ISO alpha-3 zone code, used to resolve AI service
        // locations (e.g. "France") into the codes expected by the EcoLogits API (e.g. "FRA").
        countryNameToCodeMapCache =
                countryMap
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().toLowerCase(Locale.ROOT),
                                Map.Entry::getValue,
                                (existing, duplicate) -> existing
                        ));
        lifecycleStepsCache = referentialService.getLifecycleSteps();
        electricityMixQuartilesCache = referentialService.getElectricityMixQuartiles(null);
    }

    /**
     * Evaluate the inventory
     *
     * @param context         the context
     * @param task            the task
     * @param exportDirectory the export directory
     */
    public void doEvaluate(final Context context, final Task task, Path exportDirectory) {

        Map<String, List<InVirtualEquipment>> vmsByPhysical = buildVmsByPhysicalMap(context);

        InventoryContext inventoryContext = resolveInventoryContext(task, context);
        Inventory inventory = inventoryContext.inventory();
        String inventoryName = inventoryContext.inventoryName();

        final long start = System.currentTimeMillis();
        final String organization = context.getOrganization();
        final Long taskId = task.getId();

        final Map<String, InDatacenter> datacenterByNameMap = buildDatacenterByNameMap(context);
        final List<String> lifecycleSteps = lifecycleStepsCache;

        CriteriaSetup criteriaSetup = buildCriteriaSetup(task, context, organization, lifecycleSteps);
        if (criteriaSetup == null) {
            return;
        }

        log.info("Start evaluating impacts for {}/{}", context.log(), taskId);

        Map<String, String> codeToCountryMap = codeToCountryMapCache;

        Map<List<String>, AggValuesBO> aggregationVirtualEquipments = HashMap.newHashMap(context.isHasVirtualEquipments() ? INITIAL_MAP_CAPACITY : 0);
        Map<List<String>, AggValuesBO> aggregationApplications = HashMap.newHashMap(context.isHasApplications() ? INITIAL_MAP_CAPACITY : 0);

        EvaluateReportBO evaluateReportBO = buildEvaluateReportBO(inventory, inventoryName, taskId);

        TotalsInfo totalsInfo = computeTotals(context);

        EvaluationState state = new EvaluationState(aggregationVirtualEquipments, aggregationApplications);

        try (CSVPrinter csvPhysicalEquipment = csvFileService.getPrinter(totalsInfo.physicalEquipmentIndicator(), exportDirectory);
             CSVPrinter csvVirtualEquipment = csvFileService.getPrinter(totalsInfo.virtualEquipmentIndicator(), exportDirectory);
             CSVPrinter csvAiService = csvFileService.getPrinter(FileType.AI_SERVICE_INDICATOR, exportDirectory);
             CSVPrinter csvApplication = csvFileService.getPrinter(FileType.APPLICATION_INDICATOR, exportDirectory);
             CSVPrinter csvInDatacenter = csvFileService.getPrinter(FileType.DATACENTER, exportDirectory);
             CSVPrinter csvInPhysicalEquipment = csvFileService.getPrinter(FileType.EQUIPEMENT_PHYSIQUE, exportDirectory);
             CSVPrinter csvInVirtualEquipment = csvFileService.getPrinter(FileType.VIRTUAL_EQUIPMENT, exportDirectory);
             CSVPrinter csvInAiService = csvFileService.getPrinter(FileType.AI_SERVICE, exportDirectory);
             CSVPrinter csvInApplication = csvFileService.getPrinter(FileType.APPLICATION, exportDirectory);
        ) {
            CsvPrinters printers = new CsvPrinters(csvPhysicalEquipment, csvVirtualEquipment, csvAiService, csvApplication,
                    csvInDatacenter, csvInPhysicalEquipment, csvInVirtualEquipment, csvInAiService, csvInApplication);

            writeDatacentersCsv(evaluateReportBO, datacenterByNameMap, printers);

            // manage virtual equipments without physical equipments (cloud)
            SaveResult saveResult = evaluateVirtualsEquipments(context, evaluateReportBO, null, null,
                    aggregationVirtualEquipments, aggregationApplications,
                    csvInVirtualEquipment, csvVirtualEquipment, csvInApplication, csvApplication,
                    criteriaSetup.refSip(), criteriaSetup.refShortcutBO(),
                    criteriaSetup.criteriaCodes(), lifecycleSteps, codeToCountryMap,
                    null, null);
            state.outVirtualEquipmentSize += saveResult.savedVirtualCount();
            state.outApplicationSize += saveResult.savedApplicationCount();

            // to check weather workspace level data
            long countItemImpactWorkspace = referentialGetService.countItemImpactsForWorkspace(context.getWorkspaceId());

            double processFactor = evaluateReportBO.isExport() ? 0.8 : 0.9;

            processPhysicalEquipmentPages(context, taskId, organization, evaluateReportBO, datacenterByNameMap,
                    vmsByPhysical, printers, criteriaSetup, lifecycleSteps, codeToCountryMap, inventoryName,
                    totalsInfo, countItemImpactWorkspace, processFactor, state);

            if (context.getInventoryId() != null) {
                processAiServicePages(context, taskId, evaluateReportBO, printers, criteriaSetup, lifecycleSteps,
                        inventoryName, totalsInfo, processFactor, state);
            }

        } catch (IOException e) {
            log.error("Cannot write csv output files", e);
            throw new AsyncTaskException("An error occurred on writing csv files", e);
        }

        saveRemainingAggregations(taskId, criteriaSetup.refShortcutBO(), state);

        log.info("End evaluating impacts for {}/{} in {}s and sizes: {}/{}/{}/{}", context.log(), taskId,
                (System.currentTimeMillis() - start) / 1000,
                state.outPhysicalEquipmentSize, state.outVirtualEquipmentSize, state.outApplicationSize, state.outAiServiceSize);

        saveOutputCounts(inventory, state);

        cleanEmptyFiles(exportDirectory, evaluateReportBO);
    }

    // retrieving the VM list for this DS, keyed by physical equipment name (ONLY VMs attached to a physical equipment)
    private Map<String, List<InVirtualEquipment>> buildVmsByPhysicalMap(Context context) {
        List<InVirtualEquipment> virtualEquipments = context.getInventoryId() != null ?
                inVirtualEquipmentRepository.findByInventoryId(context.getInventoryId()) :
                inVirtualEquipmentRepository.findByDigitalServiceVersionUid(context.getDigitalServiceVersionUid());

        return virtualEquipments.stream()
                .filter(vm -> vm.getPhysicalEquipmentName() != null)
                .collect(Collectors.groupingBy(InVirtualEquipment::getPhysicalEquipmentName));
    }

    private InventoryContext resolveInventoryContext(Task task, Context context) {
        Inventory inventory = task.getInventory();
        if (inventory == null) {
            return new InventoryContext(null, context.getDigitalServiceName());
        }
        inventory = inventoryRepository.findById(inventory.getId()).orElse(null);
        String inventoryName = (inventory == null) ? context.getDigitalServiceName() : inventory.getName();
        return new InventoryContext(inventory, inventoryName);
    }

    // Get datacenters by name (name, InDatacenter)
    private Map<String, InDatacenter> buildDatacenterByNameMap(Context context) {
        List<InDatacenter> datacenters = context.getInventoryId() == null ?
                inDatacenterRepository.findByDigitalServiceVersionUid(context.getDigitalServiceVersionUid()) :
                inDatacenterRepository.findByInventoryId(context.getInventoryId());
        return datacenters.stream().collect(Collectors.toMap(InDatacenter::getName, Function.identity()));
    }

    private CriteriaSetup buildCriteriaSetup(Task task, Context context, String organization, List<String> lifecycleSteps) {
        List<CriterionRest> activeCriteria = referentialService.getActiveCriteria(task.getCriteria().stream()
                .map(StringUtils::kebabToSnakeCase).toList());

        if (activeCriteria == null) {
            return null;
        }

        List<String> criteriaCodes = activeCriteria.stream().map(CriterionRest::getCode).toList();

        // get (criterion, unit) map
        Map<String, String> criteriaUnitMap = activeCriteria.stream().collect(Collectors.toMap(
                CriterionRest::getCode,
                CriterionRest::getUnit
        ));

        Map<String, ItemReferentialInfo> itemReferentialMap = referentialService.buildItemReferentialMap(context.getWorkspaceId());
        RefShortcutBO refShortcutBO = new RefShortcutBO(
                criteriaUnitMap,
                getShortcutMap(criteriaCodes),
                getShortcutMap(lifecycleSteps),
                electricityMixQuartilesCache,
                itemReferentialMap
        );

        List<HypothesisRest> hypothesisRestList = referentialService.getHypotheses(organization);
        Map<String, Double> refSip = referentialService.getSipValueMap(criteriaCodes);

        return new CriteriaSetup(activeCriteria, criteriaCodes, criteriaUnitMap, refShortcutBO, hypothesisRestList, refSip);
    }

    private EvaluateReportBO buildEvaluateReportBO(Inventory inventory, String inventoryName, Long taskId) {
        if (inventory != null && null == inventory.getDoExportVerbose()) {
            inventory.setDoExportVerbose(true);
        }
        return EvaluateReportBO.builder()
                .export(true)
                .verbose(inventory == null || inventory.getDoExportVerbose())
                .isDigitalService(inventory == null)
                .nbPhysicalEquipmentLines(0)
                .nbVirtualEquipmentLines(0)
                .nbApplicationLines(0)
                .nbAiServiceLines(0)
                .taskId(taskId)
                .name(inventoryName)
                .build();
    }

    private TotalsInfo computeTotals(Context context) {
        long totalPhysicalEquipments =
                context.getInventoryId() == null ?
                        inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(context.getDigitalServiceVersionUid()) :
                        inPhysicalEquipmentRepository.countByInventoryId(context.getInventoryId());

        long totalCloudVirtualEquipments = context.getInventoryId() == null ?
                inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(context.getDigitalServiceVersionUid(), CLOUD_SERVICES.name()) :
                inVirtualEquipmentRepository.countByInventoryIdAndInfrastructureType(context.getInventoryId(), CLOUD_SERVICES.name());

        long totalAiServices = context.getInventoryId() == null ? 0L : inAiServiceRepository.countByInventoryId(context.getInventoryId());
        long totalEquipments = totalPhysicalEquipments + totalCloudVirtualEquipments + totalAiServices;
        FileType physicalEquipmentIndicator = context.getDigitalServiceVersionUid() == null ? FileType.PHYSICAL_EQUIPMENT_INDICATOR :
                FileType.PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE;
        FileType virtualEquipmentIndicator = context.getDigitalServiceVersionUid() == null ? FileType.VIRTUAL_EQUIPMENT_INDICATOR :
                FileType.VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE;

        return new TotalsInfo(totalPhysicalEquipments, totalCloudVirtualEquipments, totalAiServices, totalEquipments,
                physicalEquipmentIndicator, virtualEquipmentIndicator);
    }

    private void writeDatacentersCsv(EvaluateReportBO evaluateReportBO, Map<String, InDatacenter> datacenterByNameMap,
                                      CsvPrinters printers) throws IOException {
        if (!evaluateReportBO.isExport()) {
            return;
        }
        for (InDatacenter inDatacenter : datacenterByNameMap.values()) {
            printers.inDatacenter().printRecord(inputToCsvRecord.toCsv(inDatacenter));
        }
    }

    private void processPhysicalEquipmentPages(Context context, Long taskId, String organization,
                                                EvaluateReportBO evaluateReportBO,
                                                Map<String, InDatacenter> datacenterByNameMap,
                                                Map<String, List<InVirtualEquipment>> vmsByPhysical,
                                                CsvPrinters printers, CriteriaSetup criteriaSetup,
                                                List<String> lifecycleSteps, Map<String, String> codeToCountryMap,
                                                String inventoryName, TotalsInfo totalsInfo,
                                                long countItemImpactWorkspace, double processFactor,
                                                EvaluationState state) throws IOException {

        final Sort sortByName = Sort.by("name");
        int pageNumber = 0;

        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, sortByName);
            final List<InPhysicalEquipment> physicalEquipments =
                    context.getInventoryId() == null ?
                            inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(context.getDigitalServiceVersionUid(), page) :
                            inPhysicalEquipmentRepository.findByInventoryId(context.getInventoryId(), page);

            if (physicalEquipments.isEmpty()) {
                break;
            }

            log.info("Evaluating {} physical equipments, page {}/{}", physicalEquipments.size(), pageNumber + 1,
                    (int) Math.ceil((double) totalsInfo.totalPhysicalEquipments() / Constants.BATCH_SIZE));

            int physicalSaveCounter = 0;
            for (InPhysicalEquipment physicalEquipment : physicalEquipments) {
                physicalSaveCounter = processOnePhysicalEquipment(context, taskId, organization, evaluateReportBO,
                        datacenterByNameMap, vmsByPhysical, printers, criteriaSetup, lifecycleSteps, codeToCountryMap,
                        inventoryName, totalsInfo, countItemImpactWorkspace, processFactor, state, physicalEquipment,
                        physicalSaveCounter);
            }

            printers.physicalEquipment().flush();
            printers.virtualEquipment().flush();
            printers.application().flush();

            pageNumber++;
            physicalEquipments.clear();
        }
    }

    private int processOnePhysicalEquipment(Context context, Long taskId, String organization,
                                             EvaluateReportBO evaluateReportBO,
                                             Map<String, InDatacenter> datacenterByNameMap,
                                             Map<String, List<InVirtualEquipment>> vmsByPhysical,
                                             CsvPrinters printers, CriteriaSetup criteriaSetup,
                                             List<String> lifecycleSteps, Map<String, String> codeToCountryMap,
                                             String inventoryName, TotalsInfo totalsInfo,
                                             long countItemImpactWorkspace, double processFactor,
                                             EvaluationState state, InPhysicalEquipment physicalEquipment,
                                             int physicalSaveCounter) throws IOException {

        if (state.aggregationPhysicalEquipments.size() > MAXIMUM_MAP_CAPACITY) {
            log.error("Exceeding aggregation size for physical equipments");
            throw new AsyncTaskException("Exceeding aggregation size for physical equipments, please reduce criteria number");
        }

        DatacenterInfo datacenterInfo = resolveDatacenterInfo(physicalEquipment, datacenterByNameMap);

        // Call external tools - lib calculs
        List<ImpactEquipementPhysique> impactEquipementPhysiqueList = evaluateNumEcoEvalService.calculatePhysicalEquipment(
                physicalEquipment, datacenterInfo.datacenter(),
                organization, criteriaSetup.activeCriteria(), lifecycleSteps, criteriaSetup.hypothesisRestList(),
                context.getWorkspaceId(), countItemImpactWorkspace);

        boolean hasNonCloudVM = hasNonCloudVirtualMachines(vmsByPhysical, physicalEquipment);

        if (evaluateReportBO.isExport()) {
            printers.inPhysicalEquipment().printRecord(inputToCsvRecord.toCsv(physicalEquipment, datacenterInfo.datacenter()));
        }

        aggregatePhysicalEquipmentImpacts(context, taskId, evaluateReportBO, printers, criteriaSetup, inventoryName,
                physicalEquipment, datacenterInfo.datacenter(), impactEquipementPhysiqueList, state);

        // set progress percentage
        state.processed++;
        if (state.processed % 20 == 0 || state.processed == totalsInfo.totalEquipments()) {
            updateProgress(taskId, state.processed, totalsInfo.totalEquipments(), processFactor);
        }

        /**
         * ------------------------------------------------------------------
         * VM RULE:
         * A physical equipment must run VM calculations ONLY IF:
         *    - It has â‰¥ 1 NON-CLOUD VM
         * Cloud VMs do NOT count for this condition.
         * ------------------------------------------------------------------
         */
        if (!hasNonCloudVM) {
            return physicalSaveCounter;
        }

        SaveResult saveResult = evaluateVirtualsEquipments(context, evaluateReportBO, physicalEquipment, impactEquipementPhysiqueList,
                state.aggregationVirtualEquipments, state.aggregationApplications,
                printers.inVirtualEquipment(), printers.virtualEquipment(), printers.inApplication(), printers.application(),
                criteriaSetup.refSip(), criteriaSetup.refShortcutBO(), criteriaSetup.criteriaCodes(), lifecycleSteps,
                codeToCountryMap, datacenterInfo.pue(), datacenterInfo.location());
        state.outVirtualEquipmentSize += saveResult.savedVirtualCount();
        state.outApplicationSize += saveResult.savedApplicationCount();

        return flushPhysicalEquipmentsIfFull(taskId, criteriaSetup.refShortcutBO(), state, physicalSaveCounter + 1);
    }

    private DatacenterInfo resolveDatacenterInfo(InPhysicalEquipment physicalEquipment,
                                                  Map<String, InDatacenter> datacenterByNameMap) {
        if (physicalEquipment.getDatacenterName() == null) {
            return new DatacenterInfo(null, null, null);
        }
        InDatacenter datacenter = datacenterByNameMap.get(physicalEquipment.getDatacenterName());
        if (datacenter == null) {
            return new DatacenterInfo(null, null, null);
        }
        // force location into physicalEquipment
        physicalEquipment.setLocation(datacenter.getLocation());
        return new DatacenterInfo(datacenter, datacenter.getPue(), datacenter.getLocation());
    }

    // Identify whether the physical equipment has NON-CLOUD VMs attached to it
    private boolean hasNonCloudVirtualMachines(Map<String, List<InVirtualEquipment>> vmsByPhysical,
                                                InPhysicalEquipment physicalEquipment) {
        List<InVirtualEquipment> allVMs = vmsByPhysical.getOrDefault(physicalEquipment.getName(), List.of());
        return allVMs.stream().anyMatch(vm -> !CLOUD_SERVICES.name().equals(vm.getInfrastructureType()));
    }

    // Aggregate physical equipment indicators in memory
    private void aggregatePhysicalEquipmentImpacts(Context context, Long taskId, EvaluateReportBO evaluateReportBO,
                                                    CsvPrinters printers, CriteriaSetup criteriaSetup, String inventoryName,
                                                    InPhysicalEquipment physicalEquipment, InDatacenter datacenter,
                                                    List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                                    EvaluationState state) throws IOException {
        for (ImpactEquipementPhysique impact : impactEquipementPhysiqueList) {
            Double sipValue = criteriaSetup.refSip().get(impact.getCritere());
            AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                    impact.getQuantite(), impact.getConsoElecMoyenne(),
                    impact.getImpactUnitaire(),
                    sipValue,
                    impact.getDureeDeVie(), null, null, false, impact.getSource());

            state.aggregationPhysicalEquipments
                    .computeIfAbsent(aggregationToOutput.keyPhysicalEquipment(physicalEquipment, datacenter, impact,
                                    criteriaSetup.refShortcutBO(), evaluateReportBO.isDigitalService()),
                            k -> new AggValuesBO())
                    .add(values);

            if (evaluateReportBO.isExport()) {
                printers.physicalEquipment().printRecord(impactToCsvRecord.toCsv(
                        context, taskId, inventoryName, physicalEquipment, impact, sipValue, evaluateReportBO.isVerbose())
                );
            }

            evaluateReportBO.setNbPhysicalEquipmentLines(evaluateReportBO.getNbPhysicalEquipmentLines() + 1);
        }
    }

    private int flushPhysicalEquipmentsIfFull(Long taskId, RefShortcutBO refShortcutBO, EvaluationState state,
                                               int physicalSaveCounter) {
        if (physicalSaveCounter < 10) {
            return physicalSaveCounter;
        }
        state.outPhysicalEquipmentSize += saveService.saveOutPhysicalEquipments(
                state.aggregationPhysicalEquipments, taskId, refShortcutBO);
        state.aggregationPhysicalEquipments = HashMap.newHashMap(INITIAL_MAP_CAPACITY);
        return 0;
    }

    private void processAiServicePages(Context context, Long taskId, EvaluateReportBO evaluateReportBO,
                                        CsvPrinters printers, CriteriaSetup criteriaSetup, List<String> lifecycleSteps,
                                        String inventoryName, TotalsInfo totalsInfo, double processFactor,
                                        EvaluationState state) throws IOException {
        final Sort sortById = Sort.by("id");
        int aiPageNumber = 0;
        while (true) {
            Pageable page = PageRequest.of(aiPageNumber, Constants.BATCH_SIZE, sortById);
            List<InAiService> aiServices = inAiServiceRepository.findByInventoryIdOrderByIdAsc(context.getInventoryId(), page);
            if (aiServices.isEmpty()) {
                break;
            }

            List<OutAiService> outAiServices = new ArrayList<>(
                    aiServices.size() * Math.max(criteriaSetup.criteriaCodes().size(), 1) * Math.max(lifecycleSteps.size(), 1));
            for (InAiService aiService : aiServices) {
                processOneAiService(context, taskId, evaluateReportBO, printers, criteriaSetup, lifecycleSteps,
                        inventoryName, totalsInfo, processFactor, state, aiService, outAiServices);
            }

            state.outAiServiceSize += saveService.saveOutAiServices(outAiServices);
            printers.aiService().flush();
            aiPageNumber++;
            aiServices.clear();
        }
    }

    private void processOneAiService(Context context, Long taskId, EvaluateReportBO evaluateReportBO,
                                      CsvPrinters printers, CriteriaSetup criteriaSetup, List<String> lifecycleSteps,
                                      String inventoryName, TotalsInfo totalsInfo, double processFactor,
                                      EvaluationState state, InAiService aiService,
                                      List<OutAiService> outAiServices) throws IOException {
        if (evaluateReportBO.isExport()) {
            printers.inAiService().printRecord(aiServiceToCsvRecord.toCsv(aiService));
        }

        List<ImpactBO> aiImpacts = evaluateEcologitsService.evaluate(aiService, criteriaSetup.criteriaCodes(), lifecycleSteps, countryNameToCodeMapCache);
        for (ImpactBO impact : aiImpacts) {
            OutAiService outAiService = toOutAiService(taskId, aiService, impact, criteriaSetup.criteriaUnitMap(), criteriaSetup.refSip());
            outAiServices.add(outAiService);
            if (evaluateReportBO.isExport()) {
                printers.aiService().printRecord(aiServiceImpactToCsvRecord.toCsv(context, taskId, inventoryName, aiService, outAiService));
            }
            evaluateReportBO.setNbAiServiceLines(evaluateReportBO.getNbAiServiceLines() + 1);
        }

        state.processed++;
        if (state.processed % 20 == 0 || state.processed == totalsInfo.totalEquipments()) {
            updateProgress(taskId, state.processed, totalsInfo.totalEquipments(), processFactor);
        }
    }

    // Store aggregated indicators remaining in memory after paging is complete
    private void saveRemainingAggregations(Long taskId, RefShortcutBO refShortcutBO, EvaluationState state) {
        log.info("Saving aggregated indicators");
        if (!state.aggregationPhysicalEquipments.isEmpty()) {
            state.outPhysicalEquipmentSize += saveService.saveOutPhysicalEquipments(
                    state.aggregationPhysicalEquipments, taskId, refShortcutBO);
            state.aggregationPhysicalEquipments.clear();
        }
        if (!state.aggregationVirtualEquipments.isEmpty()) {
            state.outVirtualEquipmentSize += saveService.saveOutVirtualEquipments(state.aggregationVirtualEquipments, taskId, refShortcutBO);
            state.aggregationVirtualEquipments.clear();
        }
        if (!state.aggregationApplications.isEmpty()) {
            state.outApplicationSize += saveService.saveOutApplications(state.aggregationApplications, taskId, refShortcutBO);
            state.aggregationApplications.clear();
        }
    }

    private void saveOutputCounts(Inventory inventory, EvaluationState state) {
        if (inventory == null) {
            return;
        }
        inventoryRepository.updateOutputCounts(
                inventory.getId(),
                (long) state.outPhysicalEquipmentSize,
                (long) state.outVirtualEquipmentSize,
                (long) state.outApplicationSize
        );
        log.info("Saved output counts to inventory: physical={}, virtual={}, application={}",
                state.outPhysicalEquipmentSize, state.outVirtualEquipmentSize, state.outApplicationSize);
    }

    // clean files if empty
    private void cleanEmptyFiles(Path exportDirectory, EvaluateReportBO evaluateReportBO) {
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
            if (evaluateReportBO.getNbAiServiceLines() == 0 || !evaluateReportBO.isExport()) {
                Files.deleteIfExists(exportDirectory.resolve(FileType.AI_SERVICE.getFileName() + Constants.CSV));
            }
        } catch (IOException e) {
            log.error("Cannot delete export local files", e);
            throw new AsyncTaskException("An error occurred on deleting empty csv files", e);
        }
    }

    // Holder for inventory resolution result
    private record InventoryContext(Inventory inventory, String inventoryName) {
    }

    // Holder for criteria/referential setup shared across the evaluation
    private record CriteriaSetup(List<CriterionRest> activeCriteria, List<String> criteriaCodes,
                                  Map<String, String> criteriaUnitMap, RefShortcutBO refShortcutBO,
                                  List<HypothesisRest> hypothesisRestList, Map<String, Double> refSip) {
    }

    // Holder for pre-computed totals and file types used for progress reporting and CSV export
    private record TotalsInfo(long totalPhysicalEquipments, long totalCloudVirtualEquipments, long totalAiServices,
                               long totalEquipments, FileType physicalEquipmentIndicator, FileType virtualEquipmentIndicator) {
    }

    // Holder for the resolved datacenter and its derived attributes for a physical equipment
    private record DatacenterInfo(InDatacenter datacenter, Double pue, String location) {
    }

    // Bundles the CSV printers opened for the duration of the evaluation
    private record CsvPrinters(CSVPrinter physicalEquipment, CSVPrinter virtualEquipment, CSVPrinter aiService,
                                CSVPrinter application, CSVPrinter inDatacenter, CSVPrinter inPhysicalEquipment,
                                CSVPrinter inVirtualEquipment, CSVPrinter inAiService, CSVPrinter inApplication) {
    }

    // Mutable accumulator of aggregation maps and counters shared across the paged processing steps
    private static final class EvaluationState {
        private Map<List<String>, AggValuesBO> aggregationPhysicalEquipments;
        private final Map<List<String>, AggValuesBO> aggregationVirtualEquipments;
        private final Map<List<String>, AggValuesBO> aggregationApplications;
        private long processed;
        private int outPhysicalEquipmentSize;
        private int outVirtualEquipmentSize;
        private int outApplicationSize;
        private int outAiServiceSize;

        private EvaluationState(Map<List<String>, AggValuesBO> aggregationVirtualEquipments,
                                 Map<List<String>, AggValuesBO> aggregationApplications) {
            this.aggregationPhysicalEquipments = HashMap.newHashMap(INITIAL_MAP_CAPACITY);
            this.aggregationVirtualEquipments = aggregationVirtualEquipments;
            this.aggregationApplications = aggregationApplications;
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
                    BoaResponseRest response =
                            boaviztapiService.runBoaviztCalculations(virtualEquipment);

                    Double avgPowerW =
                            boaviztapiService.extractAvgPowerW(response).orElse(null);

                    cloudElectricityKwh =
                            boaviztapiService.computeAnnualElectricityKwhRaw(
                                    avgPowerW,
                                    virtualEquipment.getDurationHour()
                            );
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
                    Double electricity =
                            isCloudService
                                    ? cloudElectricityKwh : impact.getConsoElecMoyenne();
                    AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                            virtualEquipment.getQuantity(),
                            electricity, impact.getImpactUnitaire(),
                            sipValue,
                            null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload(), isCloudService,impact.getSource());

                    aggregationVirtualEquipments
                            .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(physicalEquipment, virtualEquipment, impact, refShortcutBO, evaluateReportBO), k -> new AggValuesBO())
                            .add(values);

                    if (evaluateReportBO.isExport()) {
                        csvVirtualEquipment.printRecord(impactToCsvRecord.toCsv(
                                context, evaluateReportBO, virtualEquipment, impact, sipValue, electricity)
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
                        aggregationApplications, csvInApplication, csvApplication, refSip, refShortcutBO, cloudElectricityKwh);
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
                                     Map<String, Double> refSip, RefShortcutBO refShortcutBO,
                                     Double cloudElectricityKwh) throws IOException {

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
                Double electricity =
                        cloudElectricityKwh != null
                                ? cloudElectricityKwh
                                : impact.getConsoElecMoyenne();
                AggValuesBO values = createAggValuesBO(impact.getStatutIndicateur(), impact.getTrace(),
                        null, electricity, impact.getImpactUnitaire(),
                        sipValue,
                        null, null, null, false,null);

                aggregationApplications
                        .computeIfAbsent(aggregationToOutput.keyApplication(physicalEquipment, virtualEquipment, application, impact, refShortcutBO), k -> new AggValuesBO())
                        .add(values);

                if (evaluateReportBO.isExport()) {
                    csvApplication.printRecord(impactToCsvRecord.toCsv(
                            context, evaluateReportBO, application, impact, sipValue, electricity)
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
                                          Double workload, boolean isCloudService,
                                          String source) {

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
                .source(source)
                .build();
    }

    private void updateProgress(final Long taskId,
                                final long processed,
                                final long totalEquipments,
                                final double processFactor) {
        if (totalEquipments == 0) {
            return;
        }
        int progress = (int) ((processed * 100.0 * processFactor) / totalEquipments);
        taskRepository.updateProgress(taskId, progress + "%", LocalDateTime.now(clock));
    }

    private OutAiService toOutAiService(final Long taskId,
                                        final InAiService aiService,
                                        final ImpactBO impact,
                                        final Map<String, String> criteriaUnitMap,
                                        final Map<String, Double> refSip) {
        final boolean isOk = "OK".equals(impact.getIndicatorStatus());
        final Double unitImpact = isOk && impact.getUnitImpact() != null ? impact.getUnitImpact() : 0d;
        final Double sipValue = refSip.get(impact.getCriterion());
        final Double peopleEqImpact = isOk && sipValue != null && sipValue != 0 ? unitImpact / sipValue : 0d;
        final Set<String> errors = isOk || impact.getTrace() == null ? null : Set.of(impact.getTrace());

        return OutAiService.builder()
                .taskId(taskId)
                .name(aiService.getServiceName())
                .criterion(impact.getCriterion())
                .lifecycleStep(impact.getLifecycleStep())
                .provider(aiService.getProvider())
                .model(aiService.getModel())
                .location(org.apache.commons.lang3.StringUtils.defaultIfBlank(aiService.getLocation(), "WOR"))
                .engineName(com.soprasteria.g4it.backend.external.ecologits.business.EcologitsService.ECOLOGITS_ENGINE)
                .engineVersion(ecologitsVersion)
                .referentialVersion(ecologitsVersion)
                .statusIndicator(impact.getIndicatorStatus())
                .quantity(1d)
                .unitImpact(unitImpact)
                .peopleEqImpact(peopleEqImpact)
                .countValue(1L)
                .unit(criteriaUnitMap.getOrDefault(impact.getCriterion(), impact.getUnit()))
                .commonFilters(List.of(aiService.getServiceName()))
                .filters(List.of(aiService.getProvider()))
                .errors(errors)
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
