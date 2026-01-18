package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalImpact;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.*;
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.soprasteria.g4it.backend.common.utils.InfrastructureType.CLOUD_SERVICES;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    ImpactToCsvRecord impactToCsvRecord;
    @Mock
    RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository;
    @Mock
    EvaluateNumEcoEvalService evaluateNumEcoEvalService;
    @Mock
    ReferentialService referentialService;
    @Mock
    SaveService saveService;
    @Mock
    OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    @Mock
    OutApplicationRepository outApplicationRepository;
    @Mock
    CsvFileService csvFileService;
    @Mock
    TaskRepository taskRepository;
    @Mock
    InputToCsvRecord inputToCsvRecord;
    @Mock
    EvaluateBoaviztapiService evaluateBoaviztapiService;
    @Mock
    InternalToNumEcoEvalImpact internalToNumEcoEvalImpact;
    @Mock
    BoaviztapiService boaviztapiService;

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

        // minimal required stubs
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
                .thenReturn(List.of()); // no loop

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

        // physical impacts empty -> no aggregation key call needed
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

        // inventory null => digital service => export = true, but still lines = 0 -> delete happens
        when(task.getInventory()).thenReturn(null);
        when(task.getId()).thenReturn(101L);

        when(inDatacenterRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(List.of());
        when(inPhysicalEquipmentRepository.countByDigitalServiceVersionUid(anyString())).thenReturn(0L);
        when(inVirtualEquipmentRepository.countByDigitalServiceVersionUidAndInfrastructureType(anyString(), eq(CLOUD_SERVICES.name()))).thenReturn(0L);
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(anyString(), any())).thenReturn(List.of());

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        // mock static Files.deleteIfExists to confirm deletion attempt
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any())).thenReturn(true);

            evaluateService.doEvaluate(context, task, tempDir);

            filesMock.verify(() -> Files.deleteIfExists(any()), atLeastOnce());
        }
    }
}
