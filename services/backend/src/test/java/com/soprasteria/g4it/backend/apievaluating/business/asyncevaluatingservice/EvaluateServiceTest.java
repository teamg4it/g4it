package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.HypothesisRest;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.soprasteria.g4it.backend.common.utils.InfrastructureType.CLOUD_SERVICES;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EvaluateServiceTest {

    @Mock
    InDatacenterRepository inDatacenterRepository;
    @Mock
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Mock
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    InApplicationRepository inApplicationRepository;
    @Mock
    AggregationToOutput aggregationToOutput;
    @Mock
    EvaluateNumEcoEvalService evaluateNumEcoEvalService;
    @Mock
    ReferentialService referentialService;
    @Mock
    SaveService saveService;
    @Mock
    CsvFileService csvFileService;
    @Mock
    TaskRepository taskRepository;
    @Mock
    InputToCsvRecord inputToCsvRecord;
    @Mock
    BoaviztapiService boaviztapiService;
    @Mock
    private ImpactToCsvRecord impactToCsvRecord;
    @InjectMocks
    EvaluateService evaluateService;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(evaluateService, "localWorkingFolder", tempDir.toString());
    }

    private static CriterionRest criterion(String code, String unit) {
        CriterionRest c = new CriterionRest();
        c.setCode(code);
        c.setUnit(unit);
        return c;
    }

    @Test
    void doEvaluate_shouldReturn_whenActiveCriteriaIsNull() throws IOException {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(task.getCriteria()).thenReturn(List.of("criterion-1"));
        when(referentialService.getActiveCriteria(anyList())).thenReturn(null);

        evaluateService.doEvaluate(context, task, tempDir);

        verify(csvFileService, never()).getPrinter(any(), any());
        verifyNoInteractions(saveService);
    }

    @Test
    void doEvaluate_shouldThrowAsyncTaskException_whenCsvPrinterThrowsIOException() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/11");
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        when(referentialService.getActiveCriteria(anyList()))
                .thenReturn(List.of(criterion("criterion_1", "kg")));

        when(csvFileService.getPrinter(any(FileType.class), any(Path.class)))
                .thenThrow(new IOException("boom"));

        assertThrows(AsyncTaskException.class, () -> evaluateService.doEvaluate(context, task, tempDir));
    }

    @Test
    void doEvaluate_shouldRunSuccessfully_whenNoEquipments() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/13");
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        when(referentialService.getActiveCriteria(anyList()))
                .thenReturn(List.of(criterion("criterion_1", "kg")));

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString())).thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList())).thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("France", "FR"));

        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("DSV1");
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.isHasApplications()).thenReturn(false);

        Inventory inv = mock(Inventory.class);
        when(inv.getDoExportVerbose()).thenReturn(true);
        when(inv.getName()).thenReturn("INV");
        when(task.getInventory()).thenReturn(inv);
        when(task.getId()).thenReturn(10L);

        when(inDatacenterRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(List.of());
        when(inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(anyString())).thenReturn(0L);
        when(inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(anyString(), anyString())).thenReturn(0L);

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(anyString(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> evaluateService.doEvaluate(context, task, tempDir));

        verify(saveService, never()).saveOutPhysicalEquipments(any(), anyLong(), any(RefShortcutBO.class));
        verify(saveService, never()).saveOutVirtualEquipments(any(), anyLong(), any(RefShortcutBO.class));
        verify(saveService, never()).saveOutApplications(any(), anyLong(), any(RefShortcutBO.class));
    }

    @Test
    void doEvaluate_shouldExportDatacenterRecords_whenExportEnabled() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/15");
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));
        when(referentialService.getActiveCriteria(anyList()))
                .thenReturn(List.of(criterion("criterion_1", "kg")));

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString())).thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList())).thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("France", "FR"));

        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("DSV1");
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.isHasApplications()).thenReturn(false);

        Inventory inv = mock(Inventory.class);
        when(inv.getDoExportVerbose()).thenReturn(true);
        when(inv.getName()).thenReturn("INV");
        when(task.getInventory()).thenReturn(inv);
        when(task.getId()).thenReturn(11L);

        InDatacenter dc = mock(InDatacenter.class);
        when(dc.getName()).thenReturn("DC1");
        when(inDatacenterRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(List.of(dc));

        when(inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(anyString())).thenReturn(0L);
        when(inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(anyString(), anyString())).thenReturn(0L);
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(anyString(), any())).thenReturn(List.of());

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        when(inputToCsvRecord.toCsv(any(InDatacenter.class))).thenReturn(List.of("DC1"));

        evaluateService.doEvaluate(context, task, tempDir);

        verify(printer, atLeastOnce()).printRecord(anyList());
    }

    @Test
    void doEvaluate_shouldUpdateProgress_whenPhysicalEquipmentsExist() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/20");
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        when(referentialService.getActiveCriteria(anyList()))
                .thenReturn(List.of(criterion("criterion_1", "kg")));

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString())).thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList())).thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("France", "FR"));

        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("DSV1");
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.isHasApplications()).thenReturn(false);

        Inventory inv = mock(Inventory.class);
        when(inv.getDoExportVerbose()).thenReturn(true);
        when(inv.getName()).thenReturn("INV");
        when(task.getInventory()).thenReturn(inv);
        when(task.getId()).thenReturn(99L);

        when(inDatacenterRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(List.of());

        when(inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(anyString())).thenReturn(1L);
        when(inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(anyString(), anyString())).thenReturn(0L);

        InPhysicalEquipment pe = mock(InPhysicalEquipment.class);
        when(pe.getDatacenterName()).thenReturn(null);

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(anyString(), any()))
                .thenReturn(new java.util.ArrayList<>(List.of(pe)))
                .thenReturn(new java.util.ArrayList<>());

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(any(), any(), anyString(), anyList(), anyList(), anyList()))
                .thenReturn(List.of());

        evaluateService.doEvaluate(context, task, tempDir);

        verify(taskRepository, atLeastOnce()).updateProgress(eq(99L), anyString(), any(LocalDateTime.class));
    }

    @Test
    void doEvaluate_shouldThrowAsyncTaskException_whenProgressUpdateFails() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/21");
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        when(referentialService.getActiveCriteria(anyList()))
                .thenReturn(List.of(criterion("criterion_1", "kg")));

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString())).thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList())).thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("France", "FR"));

        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("DSV1");
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.isHasApplications()).thenReturn(false);

        Inventory inv = mock(Inventory.class);
        when(inv.getDoExportVerbose()).thenReturn(true);
        when(inv.getName()).thenReturn("INV");
        when(task.getInventory()).thenReturn(inv);
        when(task.getId()).thenReturn(100L);

        when(inDatacenterRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(List.of());

        when(inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(anyString())).thenReturn(1L);
        when(inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(anyString(), anyString())).thenReturn(0L);

        InPhysicalEquipment pe = mock(InPhysicalEquipment.class);
        when(pe.getDatacenterName()).thenReturn(null);

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(anyString(), any()))
                .thenReturn(List.of(pe))
                .thenReturn(List.of());

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(any(), any(), anyString(), anyList(), anyList(), anyList()))
                .thenReturn(List.of());

        doThrow(new RuntimeException("db down"))
                .when(taskRepository).updateProgress(eq(100L), anyString(), any(LocalDateTime.class));

        assertThrows(RuntimeException.class, () -> evaluateService.doEvaluate(context, task, tempDir));
    }

    @Test
    void doEvaluate_shouldDeleteFiles_whenNoExportOrNoLines() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/22");
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));
        when(referentialService.getActiveCriteria(anyList()))
                .thenReturn(List.of(criterion("criterion_1", "kg")));

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString())).thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList())).thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("France", "FR"));

        when(context.getInventoryId()).thenReturn(null);
        when(context.getDigitalServiceVersionUid()).thenReturn("DSV1");
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.isHasApplications()).thenReturn(false);

        when(task.getInventory()).thenReturn(null);
        when(task.getId()).thenReturn(101L);

        when(inDatacenterRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(List.of());
        when(inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(anyString())).thenReturn(0L);
        when(inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(anyString(), eq(CLOUD_SERVICES.name()))).thenReturn(0L);
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(anyString(), any())).thenReturn(List.of());

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any())).thenReturn(true);

            evaluateService.doEvaluate(context, task, tempDir);

            filesMock.verify(() -> Files.deleteIfExists(any()), atLeastOnce());
        }
    }

    @Test
    void doEvaluate_shouldHandleImpactWithErrorStatus() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/ERR");
        when(context.getInventoryId()).thenReturn(1L);
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.isHasApplications()).thenReturn(false);

        when(task.getId()).thenReturn(202L);
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        Inventory inv = mock(Inventory.class);
        when(inv.getDoExportVerbose()).thenReturn(true);
        when(inv.getName()).thenReturn("INV");
        when(task.getInventory()).thenReturn(inv);

        CriterionRest criterionRest = new CriterionRest();
        criterionRest.setCode("criterion_1");
        criterionRest.setUnit("kg");

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getActiveCriteria(anyList())).thenReturn(List.of(criterionRest));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString())).thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList())).thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("France", "FR"));

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        when(inDatacenterRepository.findByInventoryId(1L)).thenReturn(new ArrayList<>());

        when(inPhysicalEquipmentRepository.countByInventoryId(1L)).thenReturn(1L);
        when(inVirtualEquipmentRepository.countByInventoryIdAndInfrastructureType(1L, "CLOUD_SERVICES")).thenReturn(0L);

        InPhysicalEquipment pe = mock(InPhysicalEquipment.class);
        when(pe.getName()).thenReturn("PE1");

        when(inPhysicalEquipmentRepository.findByInventoryId(eq(1L), any()))
                .thenReturn(new ArrayList<>(List.of(pe)))
                .thenReturn(new ArrayList<>());

        ImpactEquipementPhysique impactPE = mock(ImpactEquipementPhysique.class);
        when(impactPE.getStatutIndicateur()).thenReturn("KO");
        when(impactPE.getTrace()).thenReturn("bad trace");
        when(impactPE.getQuantite()).thenReturn(1.0);
        when(impactPE.getConsoElecMoyenne()).thenReturn(0.0);
        when(impactPE.getImpactUnitaire()).thenReturn(10.0);
        when(impactPE.getCritere()).thenReturn("criterion_1");
        when(impactPE.getDureeDeVie()).thenReturn(5.0);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(any(), any(), anyString(), anyList(), anyList(), anyList()))
                .thenReturn(List.of(impactPE));

        when(aggregationToOutput.keyPhysicalEquipment(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(List.of("k1"));

        when(saveService.saveOutPhysicalEquipments(anyMap(), eq(202L), any(RefShortcutBO.class))).thenReturn(1);

        assertDoesNotThrow(() -> evaluateService.doEvaluate(context, task, tempDir));

        verify(saveService, atLeastOnce()).saveOutPhysicalEquipments(anyMap(), eq(202L), any(RefShortcutBO.class));
    }

    @Test
    void doEvaluate_shouldNotFailAndStillSaveOutput_whenImpactStatusIsKO_andVerboseDisabled() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/ERR");
        when(context.getInventoryId()).thenReturn(1L);
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.isHasApplications()).thenReturn(false);

        when(task.getId()).thenReturn(202L);
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        Inventory inv = mock(Inventory.class);

        when(inv.getDoExportVerbose()).thenReturn(false);

        when(inv.getName()).thenReturn("INV");
        when(task.getInventory()).thenReturn(inv);

        CriterionRest criterionRest = new CriterionRest();
        criterionRest.setCode("criterion_1");
        criterionRest.setUnit("kg");

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getActiveCriteria(anyList())).thenReturn(List.of(criterionRest));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString())).thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList())).thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("France", "FR"));

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        when(inDatacenterRepository.findByInventoryId(1L)).thenReturn(new ArrayList<>());

        when(inPhysicalEquipmentRepository.countByInventoryId(1L)).thenReturn(1L);
        when(inVirtualEquipmentRepository.countByInventoryIdAndInfrastructureType(1L, "CLOUD_SERVICES")).thenReturn(0L);

        InPhysicalEquipment pe = mock(InPhysicalEquipment.class);
        when(pe.getName()).thenReturn("PE1");

        when(inPhysicalEquipmentRepository.findByInventoryId(eq(1L), any()))
                .thenReturn(new ArrayList<>(List.of(pe)))
                .thenReturn(new ArrayList<>());

        ImpactEquipementPhysique impactPE = mock(ImpactEquipementPhysique.class);
        when(impactPE.getStatutIndicateur()).thenReturn("KO");
        when(impactPE.getTrace()).thenReturn("bad trace");
        when(impactPE.getQuantite()).thenReturn(1.0);
        when(impactPE.getConsoElecMoyenne()).thenReturn(0.0);
        when(impactPE.getImpactUnitaire()).thenReturn(10.0);
        when(impactPE.getCritere()).thenReturn("criterion_1");
        when(impactPE.getDureeDeVie()).thenReturn(5.0);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(any(), any(), anyString(), anyList(), anyList(), anyList()))
                .thenReturn(List.of(impactPE));

        when(aggregationToOutput.keyPhysicalEquipment(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(List.of("k1"));

        when(saveService.saveOutPhysicalEquipments(anyMap(), eq(202L), any(RefShortcutBO.class))).thenReturn(1);

        assertDoesNotThrow(() -> evaluateService.doEvaluate(context, task, tempDir));

        verify(saveService, atLeastOnce())
                .saveOutPhysicalEquipments(anyMap(), eq(202L), any(RefShortcutBO.class));
    }


    @Test
    void doEvaluate_shouldProcessPhysicalNonCloudVirtualAndApplications_andSaveAggregations() throws Exception {
        // ---------- Context & Task ----------
        Context context = mockBaseContext(true, true);
        Task task = mockTask(200L);
        mockReferential();

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class)))
                .thenReturn(printer);

        // ---------- Physical equipment ----------
        InPhysicalEquipment pe = mock(InPhysicalEquipment.class);
        when(pe.getName()).thenReturn("PE1");

        when(inPhysicalEquipmentRepository.countByInventoryId(1L)).thenReturn(1L);
        when(inPhysicalEquipmentRepository.findByInventoryId(eq(1L), any()))
                .thenReturn(new ArrayList<>(List.of(pe)))
                .thenReturn(new ArrayList<>());

        ImpactEquipementPhysique impactPE = mock(ImpactEquipementPhysique.class);
        when(impactPE.getStatutIndicateur()).thenReturn("OK");
        when(impactPE.getCritere()).thenReturn("criterion_1");
        when(impactPE.getQuantite()).thenReturn(1.0);
        when(impactPE.getImpactUnitaire()).thenReturn(10.0);
        when(impactPE.getDureeDeVie()).thenReturn(5.0);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(
                any(), any(), anyString(), anyList(), anyList(), anyList()
        )).thenReturn(List.of(impactPE));

        when(aggregationToOutput.keyPhysicalEquipment(
                any(), any(), any(), any(), anyBoolean()
        )).thenReturn(List.of("PHYSICAL_KEY"));

        // ---------- NON-CLOUD virtual equipment ----------
        InVirtualEquipment ve = mock(InVirtualEquipment.class);
        when(ve.getName()).thenReturn("VE1");
        when(ve.getPhysicalEquipmentName()).thenReturn("PE1");
        when(ve.getInfrastructureType()).thenReturn("ON_PREM"); // non-cloud
        when(ve.getQuantity()).thenReturn(1.0);
        when(ve.getDurationHour()).thenReturn(10.0);
        when(ve.getWorkload()).thenReturn(0.5);
        when(ve.getLocation()).thenReturn("France");

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("DSV1"))
                .thenReturn(List.of(ve));

        when(inVirtualEquipmentRepository.findByInventoryIdAndPhysicalEquipmentName(
                eq(1L), eq("PE1"), any()
        ))
                .thenReturn(new ArrayList<>(List.of(ve)))
                .thenReturn(new ArrayList<>());

        when(evaluateNumEcoEvalService.getTotalVcpuCoreNumber(anyList()))
                .thenReturn(4.0);
        when(evaluateNumEcoEvalService.getTotalDiskSize(anyList()))
                .thenReturn(100.0);

        // Virtual impact exists but does NOT lead to aggregation save
        ImpactEquipementVirtuel impactVE = mock(ImpactEquipementVirtuel.class);
        when(impactVE.getStatutIndicateur()).thenReturn("OK");
        when(impactVE.getCritere()).thenReturn("criterion_1");
        when(impactVE.getImpactUnitaire()).thenReturn(20.0);

        when(evaluateNumEcoEvalService.calculateVirtualEquipment(
                any(), anyList(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyString()
        )).thenReturn(List.of(impactVE));

        // ---------- Application ----------
        InApplication app = mock(InApplication.class);
        when(app.getName()).thenReturn("APP1");

        when(inApplicationRepository
                .findByInventoryIdAndPhysicalEquipmentNameAndVirtualEquipmentName(
                        1L, "PE1", "VE1"
                ))
                .thenReturn(List.of(app));

        ImpactApplication appImpact = mock(ImpactApplication.class);
        when(appImpact.getStatutIndicateur()).thenReturn("OK");
        when(appImpact.getCritere()).thenReturn("criterion_1");

        when(evaluateNumEcoEvalService.calculateApplication(any(), anyList(), anyInt()))
                .thenReturn(List.of(appImpact));

        // ---------- Execute ----------
        assertDoesNotThrow(() ->
                evaluateService.doEvaluate(context, task, tempDir)
        );

        // ---------- Verify ----------
        verify(saveService)
                .saveOutPhysicalEquipments(anyMap(), eq(200L), any());

        verify(saveService)
                .saveOutApplications(anyMap(), eq(200L), any());

        // 🔑 OPTION 1: virtual aggregation is NOT saved
        verify(saveService, never())
                .saveOutVirtualEquipments(anyMap(), anyLong(), any());
    }

    @Test
    void doEvaluate_shouldSkipVmCalculation_whenOnlyCloudVmsExist() throws Exception {
        Context context = mockBaseContext(true, false);
        Task task = mockTask(300L);
        mockReferential();

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class)))
                .thenReturn(printer);

        InPhysicalEquipment pe = mock(InPhysicalEquipment.class);
        when(pe.getName()).thenReturn("PE1");

        when(inPhysicalEquipmentRepository.countByInventoryId(1L)).thenReturn(1L);
        when(inPhysicalEquipmentRepository.findByInventoryId(eq(1L), any()))
                .thenReturn(new ArrayList<>(List.of(pe)))
                .thenReturn(new ArrayList<>());

        ImpactEquipementPhysique impactPE = mock(ImpactEquipementPhysique.class);
        when(impactPE.getStatutIndicateur()).thenReturn("OK");
        when(impactPE.getCritere()).thenReturn("criterion_1");

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(any(), any(), anyString(), anyList(), anyList(), anyList()))
                .thenReturn(List.of(impactPE));

        when(aggregationToOutput.keyPhysicalEquipment(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(List.of("PHYSICAL_KEY"));

        // CLOUD VM ONLY
        InVirtualEquipment cloudVm = mock(InVirtualEquipment.class);
        when(cloudVm.getPhysicalEquipmentName()).thenReturn("PE1");
        when(cloudVm.getInfrastructureType()).thenReturn(CLOUD_SERVICES.name());

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("DSV1"))
                .thenReturn(List.of(cloudVm));

        // Run
        evaluateService.doEvaluate(context, task, tempDir);

        // Physical is calculated
        verify(evaluateNumEcoEvalService).calculatePhysicalEquipment(any(), any(), any(), any(), any(), any());

        // VM calculation MUST NOT happen
        verify(evaluateNumEcoEvalService, never())
                .calculateVirtualEquipment(any(), any(), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyString());

        // No virtual outputs saved
        verify(saveService, never()).saveOutVirtualEquipments(anyMap(), anyLong(), any());
    }

    private Context mockBaseContext(boolean hasVms, boolean hasApps) {
        Context context = mock(Context.class);
        when(context.log()).thenReturn("ORG/DS/TEST");
        when(context.getInventoryId()).thenReturn(1L);
        when(context.getOrganization()).thenReturn("ORG");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.getDigitalServiceVersionUid()).thenReturn("DSV1");
        when(context.isHasVirtualEquipments()).thenReturn(hasVms);
        when(context.isHasApplications()).thenReturn(hasApps);
        return context;
    }

    private Task mockTask(long taskId) {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(taskId);
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        Inventory inv = mock(Inventory.class);
        when(inv.getDoExportVerbose()).thenReturn(true);
        when(inv.getName()).thenReturn("INV");
        when(task.getInventory()).thenReturn(inv);

        return task;
    }

    private void mockReferential() {
        CriterionRest criterionRest = new CriterionRest();
        criterionRest.setCode("criterion_1");
        criterionRest.setUnit("kg");

        when(referentialService.getLifecycleSteps()).thenReturn(List.of("STEP1"));
        when(referentialService.getActiveCriteria(anyList()))
                .thenReturn(List.of(criterionRest));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(anyString()))
                .thenReturn(List.of(mock(HypothesisRest.class)));
        when(referentialService.getSipValueMap(anyList()))
                .thenReturn(Map.of("criterion_1", 1.0));
        when(boaviztapiService.getCountryMap())
                .thenReturn(Map.of("France", "FR"));
    }

}
