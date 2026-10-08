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
            VirtualEquipmentEvalContext cloudVirtualCtx = new VirtualEquipmentEvalContext(context, evaluateReportBO,
                    null, null, aggregationVirtualEquipments, aggregationApplications,
                    csvInVirtualEquipment, csvVirtualEquipment, csvInApplication, csvApplication,
                    criteriaSetup.refSip(), criteriaSetup.refShortcutBO(),
                    criteriaSetup.criteriaCodes(), lifecycleSteps, codeToCountryMap,
                    null, null);
            SaveResult saveResult = evaluateVirtualsEquipments(cloudVirtualCtx);
            state.outVirtualEquipmentSize += saveResult.savedVirtualCount();
            state.outApplicationSize += saveResult.savedApplicationCount();

            // to check weather workspace level data
            long countItemImpactWorkspace = referentialGetService.countItemImpactsForWorkspace(context.getWorkspaceId());

            double processFactor = evaluateReportBO.isExport() ? 0.8 : 0.9;

            EvaluationRunContext runContext = new EvaluationRunContext(context, taskId, organization, evaluateReportBO,
                    datacenterByNameMap, vmsByPhysical, printers, criteriaSetup, lifecycleSteps, codeToCountryMap,
                    inventoryName, totalsInfo, countItemImpactWorkspace, processFactor);

            processPhysicalEquipmentPages(runContext, state);

            if (context.getInventoryId() != null) {
                processAiServicePages(runContext, state);
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

    private void processPhysicalEquipmentPages(EvaluationRunContext runCtx, EvaluationState state) throws IOException {
        Context context = runCtx.context();
        CsvPrinters printers = runCtx.printers();

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
                    (int) Math.ceil((double) runCtx.totalsInfo().totalPhysicalEquipments() / Constants.BATCH_SIZE));

            int physicalSaveCounter = 0;
            for (InPhysicalEquipment physicalEquipment : physicalEquipments) {
                physicalSaveCounter = processOnePhysicalEquipment(runCtx, state, physicalEquipment, physicalSaveCounter);
            }

            printers.physicalEquipment().flush();
            printers.virtualEquipment().flush();
            printers.application().flush();

            pageNumber++;
            physicalEquipments.clear();
        }
    }

    private int processOnePhysicalEquipment(EvaluationRunContext runCtx, EvaluationState state,
                                             InPhysicalEquipment physicalEquipment, int physicalSaveCounter) throws IOException {
        Context context = runCtx.context();
        EvaluateReportBO evaluateReportBO = runCtx.evaluateReportBO();
        CriteriaSetup criteriaSetup = runCtx.criteriaSetup();
        CsvPrinters printers = runCtx.printers();

        if (state.aggregationPhysicalEquipments.size() > MAXIMUM_MAP_CAPACITY) {
            log.error("Exceeding aggregation size for physical equipments");
            throw new AsyncTaskException("Exceeding aggregation size for physical equipments, please reduce criteria number");
        }

        DatacenterInfo datacenterInfo = resolveDatacenterInfo(physicalEquipment, runCtx.datacenterByNameMap());

        // Call external tools - lib calculs
        List<ImpactEquipementPhysique> impactEquipementPhysiqueList = evaluateNumEcoEvalService.calculatePhysicalEquipment(
                physicalEquipment, datacenterInfo.datacenter(),
                runCtx.organization(), criteriaSetup.activeCriteria(), runCtx.lifecycleSteps(), criteriaSetup.hypothesisRestList(),
                context.getWorkspaceId(), runCtx.countItemImpactWorkspace());

        boolean hasNonCloudVM = hasNonCloudVirtualMachines(runCtx.vmsByPhysical(), physicalEquipment);

        if (evaluateReportBO.isExport()) {
            printers.inPhysicalEquipment().printRecord(inputToCsvRecord.toCsv(physicalEquipment, datacenterInfo.datacenter()));
        }

        aggregatePhysicalEquipmentImpacts(runCtx, state, physicalEquipment, datacenterInfo.datacenter(), impactEquipementPhysiqueList);

        // set progress percentage
        state.processed++;
        if (state.processed % 20 == 0 || state.processed == runCtx.totalsInfo().totalEquipments()) {
            updateProgress(runCtx.taskId(), state.processed, runCtx.totalsInfo().totalEquipments(), runCtx.processFactor());
        }

        /**
         * ------------------------------------------------------------------
         * VM RULE:
         * A physical equipment must run VM calculations ONLY IF:
         *    - It has >= 1 NON-CLOUD VM
         * Cloud VMs do NOT count for this condition.
         * ------------------------------------------------------------------
         */
        if (!hasNonCloudVM) {
            return physicalSaveCounter;
        }

        VirtualEquipmentEvalContext vmCtx = new VirtualEquipmentEvalContext(context, evaluateReportBO, physicalEquipment,
                impactEquipementPhysiqueList, state.aggregationVirtualEquipments, state.aggregationApplications,
                printers.inVirtualEquipment(), printers.virtualEquipment(), printers.inApplication(), printers.application(),
                criteriaSetup.refSip(), criteriaSetup.refShortcutBO(), criteriaSetup.criteriaCodes(), runCtx.lifecycleSteps(),
                runCtx.codeToCountryMap(), datacenterInfo.pue(), datacenterInfo.location());
        SaveResult saveResult = evaluateVirtualsEquipments(vmCtx);
        state.outVirtualEquipmentSize += saveResult.savedVirtualCount();
        state.outApplicationSize += saveResult.savedApplicationCount();

        return flushPhysicalEquipmentsIfFull(runCtx.taskId(), criteriaSetup.refShortcutBO(), state, physicalSaveCounter + 1);
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
    private void aggregatePhysicalEquipmentImpacts(EvaluationRunContext runCtx, EvaluationState state,
                                                    InPhysicalEquipment physicalEquipment, InDatacenter datacenter,
                                                    List<ImpactEquipementPhysique> impactEquipementPhysiqueList) throws IOException {
        EvaluateReportBO evaluateReportBO = runCtx.evaluateReportBO();
        CriteriaSetup criteriaSetup = runCtx.criteriaSetup();
        CSVPrinter csvPhysicalEquipment = runCtx.printers().physicalEquipment();

        for (ImpactEquipementPhysique impact : impactEquipementPhysiqueList) {
            Double sipValue = criteriaSetup.refSip().get(impact.getCritere());
            AggValuesBO values = createAggValuesBO(new AggValuesInput(impact.getStatutIndicateur(), impact.getTrace(),
                    impact.getQuantite(), impact.getConsoElecMoyenne(),
                    impact.getImpactUnitaire(),
                    sipValue,
                    impact.getDureeDeVie(), null, null, false, impact.getSource()));

            state.aggregationPhysicalEquipments
                    .computeIfAbsent(aggregationToOutput.keyPhysicalEquipment(physicalEquipment, datacenter, impact,
                                    criteriaSetup.refShortcutBO(), evaluateReportBO.isDigitalService()),
                            k -> new AggValuesBO())
                    .add(values);

            if (evaluateReportBO.isExport()) {
                csvPhysicalEquipment.printRecord(impactToCsvRecord.toCsv(
                        runCtx.context(), runCtx.taskId(), runCtx.inventoryName(), physicalEquipment, impact, sipValue, evaluateReportBO.isVerbose())
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

    private void processAiServicePages(EvaluationRunContext runCtx, EvaluationState state) throws IOException {
        Context context = runCtx.context();
        CriteriaSetup criteriaSetup = runCtx.criteriaSetup();
        CSVPrinter csvAiService = runCtx.printers().aiService();

        final Sort sortById = Sort.by("id");
        int aiPageNumber = 0;
        while (true) {
            Pageable page = PageRequest.of(aiPageNumber, Constants.BATCH_SIZE, sortById);
            List<InAiService> aiServices = inAiServiceRepository.findByInventoryIdOrderByIdAsc(context.getInventoryId(), page);
            if (aiServices.isEmpty()) {
                break;
            }

            List<OutAiService> outAiServices = new ArrayList<>(
                    aiServices.size() * Math.max(criteriaSetup.criteriaCodes().size(), 1) * Math.max(runCtx.lifecycleSteps().size(), 1));
            for (InAiService aiService : aiServices) {
                processOneAiService(runCtx, state, aiService, outAiServices);
            }

            state.outAiServiceSize += saveService.saveOutAiServices(outAiServices);
            csvAiService.flush();
            aiPageNumber++;
            aiServices.clear();
        }
    }

    private void processOneAiService(EvaluationRunContext runCtx, EvaluationState state, InAiService aiService,
                                      List<OutAiService> outAiServices) throws IOException {
        EvaluateReportBO evaluateReportBO = runCtx.evaluateReportBO();
        CriteriaSetup criteriaSetup = runCtx.criteriaSetup();
        CsvPrinters printers = runCtx.printers();

        if (evaluateReportBO.isExport()) {
            printers.inAiService().printRecord(aiServiceToCsvRecord.toCsv(aiService));
        }

        List<ImpactBO> aiImpacts = evaluateEcologitsService.evaluate(aiService, criteriaSetup.criteriaCodes(), runCtx.lifecycleSteps(), countryNameToCodeMapCache);
        for (ImpactBO impact : aiImpacts) {
            OutAiService outAiService = toOutAiService(runCtx.taskId(), aiService, impact, criteriaSetup.criteriaUnitMap(), criteriaSetup.refSip());
            outAiServices.add(outAiService);
            if (evaluateReportBO.isExport()) {
                printers.aiService().printRecord(aiServiceImpactToCsvRecord.toCsv(runCtx.context(), runCtx.taskId(), runCtx.inventoryName(), aiService, outAiService));
            }
            evaluateReportBO.setNbAiServiceLines(evaluateReportBO.getNbAiServiceLines() + 1);
        }

        state.processed++;
        if (state.processed % 20 == 0 || state.processed == runCtx.totalsInfo().totalEquipments()) {
            updateProgress(runCtx.taskId(), state.processed, runCtx.totalsInfo().totalEquipments(), runCtx.processFactor());
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
                (long) state.outApplicationSize,
                (long) state.outAiServiceSize
        );
        log.info("Saved output counts to inventory: physical={}, virtual={}, application={}, aiService={}",
                state.outPhysicalEquipmentSize, state.outVirtualEquipmentSize, state.outApplicationSize, state.outAiServiceSize);
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

    // Bundles the parameters shared across the physical-equipment and AI-service paged processing steps
    private record EvaluationRunContext(Context context, Long taskId, String organization,
                                         EvaluateReportBO evaluateReportBO,
                                         Map<String, InDatacenter> datacenterByNameMap,
                                         Map<String, List<InVirtualEquipment>> vmsByPhysical,
                                         CsvPrinters printers, CriteriaSetup criteriaSetup,
                                         List<String> lifecycleSteps, Map<String, String> codeToCountryMap,
                                         String inventoryName, TotalsInfo totalsInfo,
                                         long countItemImpactWorkspace, double processFactor) {
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
    private SaveResult evaluateVirtualsEquipments(VirtualEquipmentEvalContext ctx) throws IOException {

        if (!ctx.context().isHasVirtualEquipments()) return new SaveResult(0, 0);

        String physicalEquipmentName = ctx.physicalEquipment() == null ? null : ctx.physicalEquipment().getName();

        VirtualEvalState state = new VirtualEvalState();
        final Sort sortByName = Sort.by("name");

        while (true) {
            Pageable page = PageRequest.of(state.pageNumber, Constants.BATCH_SIZE, sortByName);
            List<InVirtualEquipment> virtualEquipments = fetchVirtualEquipmentsPage(ctx.context(), physicalEquipmentName, page);
            if (virtualEquipments.isEmpty()) {
                break;
            }

            processVirtualEquipmentsPage(ctx, virtualEquipments, state);

            state.pageNumber++;
            if (state.pageNumber % 5 == 0) {
                ctx.csvVirtualEquipment().flush();
                ctx.csvApplication().flush();
            }
            virtualEquipments.clear();
        }

        flushRemainingVirtualEquipments(ctx, state);
        return new SaveResult(state.savedVirtualCount, state.savedApplicationCount);
    }

    private List<InVirtualEquipment> fetchVirtualEquipmentsPage(Context context, String physicalEquipmentName, Pageable page) {
        if (context.getInventoryId() == null && physicalEquipmentName == null) {
            return inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndPhysicalEquipmentNameIsNull(
                    context.getDigitalServiceVersionUid(), page);
        }
        if (context.getInventoryId() == null) {
            return inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndPhysicalEquipmentName(
                    context.getDigitalServiceVersionUid(), physicalEquipmentName, page);
        }
        return inVirtualEquipmentRepository.findByInventoryIdAndPhysicalEquipmentName(
                context.getInventoryId(), physicalEquipmentName, page);
    }

    private void processVirtualEquipmentsPage(VirtualEquipmentEvalContext ctx, List<InVirtualEquipment> virtualEquipments,
                                               VirtualEvalState state) throws IOException {
        Double totalVcpuCoreNumber = evaluateNumEcoEvalService.getTotalVcpuCoreNumber(virtualEquipments);
        Double totalStorage = evaluateNumEcoEvalService.getTotalDiskSize(virtualEquipments);
        int virtualSize = virtualEquipments.size();

        for (InVirtualEquipment virtualEquipment : virtualEquipments) {
            processOneVirtualEquipment(ctx, virtualEquipment, virtualSize, totalVcpuCoreNumber, totalStorage, state);
        }
    }

    private void processOneVirtualEquipment(VirtualEquipmentEvalContext ctx, InVirtualEquipment virtualEquipment,
                                             int virtualSize, Double totalVcpuCoreNumber, Double totalStorage,
                                             VirtualEvalState state) throws IOException {
        boolean isCloudService = CLOUD_SERVICES.name().equals(virtualEquipment.getInfrastructureType());
        VirtualImpactResult impactResult = computeVirtualEquipmentImpacts(ctx, virtualEquipment, isCloudService,
                virtualSize, totalVcpuCoreNumber, totalStorage);

        String location = isCloudService ? ctx.codeToCountryMap().get(virtualEquipment.getLocation()) : virtualEquipment.getLocation();
        if (ctx.evaluateReportBO().isExport()) {
            ctx.csvInVirtualEquipment().printRecord(inputToCsvRecord.toCsv(virtualEquipment, location));
        }

        aggregateVirtualEquipmentImpacts(ctx, virtualEquipment, isCloudService, impactResult);

        checkAggregationCapacity(ctx.aggregationVirtualEquipments(), "virtual equipments");

        state.virtualSaveCounter++;
        flushVirtualEquipmentsIfFull(ctx, state);

        ApplicationEvalContext appCtx = new ApplicationEvalContext(ctx.context(), ctx.evaluateReportBO(), ctx.physicalEquipment(),
                virtualEquipment, impactResult.impactList(), ctx.aggregationApplications(), ctx.csvInApplication(),
                ctx.csvApplication(), ctx.refSip(), ctx.refShortcutBO(), impactResult.cloudElectricityKwh());
        state.savedApplicationCount += this.evaluateApplications(appCtx);
    }

    private VirtualImpactResult computeVirtualEquipmentImpacts(VirtualEquipmentEvalContext ctx, InVirtualEquipment virtualEquipment,
                                                                 boolean isCloudService, int virtualSize,
                                                                 Double totalVcpuCoreNumber, Double totalStorage) {
        if (isCloudService) {
            List<ImpactBO> impactBOList = evaluateBoaviztapiService.evaluate(virtualEquipment, ctx.criteria(), ctx.lifecycleSteps());
            List<ImpactEquipementVirtuel> impactEquipementVirtuelList = internalToNumEcoEvalImpact.map(impactBOList);
            BoaResponseRest response = boaviztapiService.runBoaviztCalculations(virtualEquipment);
            Double avgPowerW = boaviztapiService.extractAvgPowerW(response).orElse(null);
            Double cloudElectricityKwh = boaviztapiService.computeAnnualElectricityKwhRaw(avgPowerW, virtualEquipment.getDurationHour());
            return new VirtualImpactResult(impactEquipementVirtuelList, cloudElectricityKwh);
        }

        List<ImpactEquipementVirtuel> impactEquipementVirtuelList = evaluateNumEcoEvalService.calculateVirtualEquipment(
                virtualEquipment, ctx.impactEquipementPhysiqueList(), virtualSize, totalVcpuCoreNumber, totalStorage,
                ctx.equipmentPue(), ctx.equipmentLocation()
        );
        return new VirtualImpactResult(impactEquipementVirtuelList, null);
    }

    // Aggregate virtual equipment indicators in memory
    private void aggregateVirtualEquipmentImpacts(VirtualEquipmentEvalContext ctx, InVirtualEquipment virtualEquipment,
                                                   boolean isCloudService, VirtualImpactResult impactResult) throws IOException {
        EvaluateReportBO evaluateReportBO = ctx.evaluateReportBO();
        for (ImpactEquipementVirtuel impact : impactResult.impactList()) {
            Double sipValue = ctx.refSip().get(impact.getCritere());
            Double electricity = isCloudService ? impactResult.cloudElectricityKwh() : impact.getConsoElecMoyenne();
            AggValuesBO values = createAggValuesBO(new AggValuesInput(impact.getStatutIndicateur(), impact.getTrace(),
                    virtualEquipment.getQuantity(),
                    electricity, impact.getImpactUnitaire(),
                    sipValue,
                    null, virtualEquipment.getDurationHour(), virtualEquipment.getWorkload(), isCloudService, impact.getSource()));

            ctx.aggregationVirtualEquipments()
                    .computeIfAbsent(aggregationToOutput.keyVirtualEquipment(ctx.physicalEquipment(), virtualEquipment, impact,
                            ctx.refShortcutBO(), evaluateReportBO), k -> new AggValuesBO())
                    .add(values);

            if (evaluateReportBO.isExport()) {
                ctx.csvVirtualEquipment().printRecord(impactToCsvRecord.toCsv(
                        ctx.context(), evaluateReportBO, virtualEquipment, impact, sipValue, electricity)
                );
            }

            evaluateReportBO.setNbVirtualEquipmentLines(evaluateReportBO.getNbVirtualEquipmentLines() + 1);
        }
    }

    private void checkAggregationCapacity(Map<List<String>, AggValuesBO> aggregationMap, String label) {
        if (aggregationMap.size() > MAXIMUM_MAP_CAPACITY) {
            log.error("Exceeding aggregation size for {}", label);
            throw new AsyncTaskException("Exceeding aggregation size for " + label + ", please reduce criteria number");
        }
    }

    private void flushVirtualEquipmentsIfFull(VirtualEquipmentEvalContext ctx, VirtualEvalState state) {
        if (state.virtualSaveCounter < 10) {
            return;
        }
        state.savedVirtualCount += saveService.saveOutVirtualEquipments(
                ctx.aggregationVirtualEquipments(), ctx.evaluateReportBO().getTaskId(), ctx.refShortcutBO());
        ctx.aggregationVirtualEquipments().clear();
        state.virtualSaveCounter = 0;
    }

    private void flushRemainingVirtualEquipments(VirtualEquipmentEvalContext ctx, VirtualEvalState state) {
        if (ctx.aggregationVirtualEquipments().isEmpty()) {
            return;
        }
        state.savedVirtualCount += saveService.saveOutVirtualEquipments(
                ctx.aggregationVirtualEquipments(), ctx.evaluateReportBO().getTaskId(), ctx.refShortcutBO());
        ctx.aggregationVirtualEquipments().clear();
    }

    // Holder for the invariant parameters shared across the virtual equipment evaluation steps
    private record VirtualEquipmentEvalContext(Context context, EvaluateReportBO evaluateReportBO,
                                                InPhysicalEquipment physicalEquipment,
                                                List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                                Map<List<String>, AggValuesBO> aggregationVirtualEquipments,
                                                Map<List<String>, AggValuesBO> aggregationApplications,
                                                CSVPrinter csvInVirtualEquipment, CSVPrinter csvVirtualEquipment,
                                                CSVPrinter csvInApplication, CSVPrinter csvApplication,
                                                Map<String, Double> refSip, RefShortcutBO refShortcutBO,
                                                List<String> criteria, List<String> lifecycleSteps,
                                                Map<String, String> codeToCountryMap, Double equipmentPue,
                                                String equipmentLocation) {
    }

    // Holder for the impact computation result of a single virtual equipment
    private record VirtualImpactResult(List<ImpactEquipementVirtuel> impactList, Double cloudElectricityKwh) {
    }

    // Mutable counters shared across the paged processing of virtual equipments
    private static final class VirtualEvalState {
        private int pageNumber;
        private int virtualSaveCounter;
        private int savedVirtualCount;
        private int savedApplicationCount;
    }

    private int evaluateApplications(ApplicationEvalContext ctx) throws IOException {

        if (!ctx.context().isHasApplications()) return 0;

        String physicalEquipmentName = ctx.physicalEquipment() == null ? null : ctx.physicalEquipment().getName();
        List<InApplication> applicationList = inApplicationRepository.findByInventoryIdAndPhysicalEquipmentNameAndVirtualEquipmentName(
                ctx.context().getInventoryId(), physicalEquipmentName, ctx.virtualEquipment().getName());

        return processApplicationList(ctx, applicationList);
    }

    private int processApplicationList(ApplicationEvalContext ctx, List<InApplication> applicationList) throws IOException {
        int savedApplicationCount = 0;
        int applicationSaveCounter = 0;

        for (InApplication application : applicationList) {
            processOneApplication(ctx, application, applicationList.size());

            applicationSaveCounter++;
            if (applicationSaveCounter >= 10) {
                savedApplicationCount += flushApplications(ctx);
                applicationSaveCounter = 0;
            }
        }

        if (!ctx.aggregationApplications().isEmpty()) {
            savedApplicationCount += flushApplications(ctx);
        }
        return savedApplicationCount;
    }

    private void processOneApplication(ApplicationEvalContext ctx, InApplication application, int applicationListSize) throws IOException {
        EvaluateReportBO evaluateReportBO = ctx.evaluateReportBO();

        if (evaluateReportBO.isExport()) {
            ctx.csvInApplication().printRecord(inputToCsvRecord.toCsv(application));
        }

        List<ImpactApplication> impactApplicationList = evaluateNumEcoEvalService.calculateApplication(
                application, ctx.impactEquipementVirtuelList(), applicationListSize);

        aggregateApplicationImpacts(ctx, application, impactApplicationList);

        checkAggregationCapacity(ctx.aggregationApplications(), "applications");
    }

    // Aggregate application indicators in memory
    private void aggregateApplicationImpacts(ApplicationEvalContext ctx, InApplication application,
                                              List<ImpactApplication> impactApplicationList) throws IOException {
        EvaluateReportBO evaluateReportBO = ctx.evaluateReportBO();

        for (ImpactApplication impact : impactApplicationList) {
            Double sipValue = ctx.refSip().get(impact.getCritere());
            Double electricity = ctx.cloudElectricityKwh() != null ? ctx.cloudElectricityKwh() : impact.getConsoElecMoyenne();
            AggValuesBO values = createAggValuesBO(new AggValuesInput(impact.getStatutIndicateur(), impact.getTrace(),
                    null, electricity, impact.getImpactUnitaire(),
                    sipValue,
                    null, null, null, false, null));

            ctx.aggregationApplications()
                    .computeIfAbsent(aggregationToOutput.keyApplication(ctx.physicalEquipment(), ctx.virtualEquipment(),
                            application, impact, ctx.refShortcutBO()), k -> new AggValuesBO())
                    .add(values);

            if (evaluateReportBO.isExport()) {
                ctx.csvApplication().printRecord(impactToCsvRecord.toCsv(
                        ctx.context(), evaluateReportBO, application, impact, sipValue, electricity)
                );
            }

            evaluateReportBO.setNbApplicationLines(evaluateReportBO.getNbApplicationLines() + 1);
        }
    }

    private int flushApplications(ApplicationEvalContext ctx) {
        int saved = saveService.saveOutApplications(ctx.aggregationApplications(), ctx.evaluateReportBO().getTaskId(), ctx.refShortcutBO());
        ctx.aggregationApplications().clear();
        return saved;
    }

    // Holder for the invariant parameters shared across the application evaluation steps
    private record ApplicationEvalContext(Context context, EvaluateReportBO evaluateReportBO,
                                           InPhysicalEquipment physicalEquipment, InVirtualEquipment virtualEquipment,
                                           List<ImpactEquipementVirtuel> impactEquipementVirtuelList,
                                           Map<List<String>, AggValuesBO> aggregationApplications,
                                           CSVPrinter csvInApplication, CSVPrinter csvApplication,
                                           Map<String, Double> refSip, RefShortcutBO refShortcutBO,
                                           Double cloudElectricityKwh) {
    }

    /**
     * Create AggValuesBO from params with default values
     *
     * @param input the aggregation input values
     * @return the agg value
     */
    private AggValuesBO createAggValuesBO(AggValuesInput input) {

        boolean isOk = "OK".equals(input.indicatorStatus());

        String error = isOk ? null : input.trace();

        Double localQuantity = input.quantity() == null ? 1d : input.quantity();
        Double impact;

        if (input.isCloudService()) {
            impact = input.unitImpact() == null ? 0d : input.unitImpact() * localQuantity;
        } else {
            impact = input.unitImpact() == null ? 0d : input.unitImpact();
        }

        return AggValuesBO.builder()
                .countValue(1L)
                .unitImpact(impact)
                .peopleEqImpact(input.sipValue() == null ? 0d : impact / input.sipValue())
                .electricityConsumption(input.elecConsumption() == null ? 0d : input.elecConsumption() * localQuantity)
                .quantity(localQuantity)
                .lifespan(input.lifespan() == null ? 0d : input.lifespan() * localQuantity)
                .usageDuration(input.usageDuration() == null ? 0d : input.usageDuration())
                .workload(input.workload() == null ? 0d : input.workload())
                .errors(error == null ? Collections.emptySet() : Collections.singleton(error))
                .source(input.source())
                .build();
    }

    // Holder for the parameters used to compute a single AggValuesBO
    private record AggValuesInput(String indicatorStatus, String trace, Double quantity, Double elecConsumption,
                                   Double unitImpact, Double sipValue, Double lifespan, Double usageDuration,
                                   Double workload, boolean isCloudService, String source) {
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
        final Set<String> errors = isOk && impact.getTrace() == null ? null : Set.of(impact.getTrace());
        final List<String> commonFilters = aiService.getCommonFilters() == null ? List.of() : aiService.getCommonFilters();

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
                .commonFilters(commonFilters)
                .filters(List.of(aiService.getProvider()))
                .source(aiService.getSource())
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
