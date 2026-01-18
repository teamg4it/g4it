package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalImpact;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.repository.*;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
        // Only set what is needed for local file operations
        ReflectionTestUtils.setField(evaluateService, "localWorkingFolder", tempDir.toString());
    }

    @Test
    void doEvaluate_shouldReturn_whenActiveCriteriaIsNull() throws IOException {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(task.getCriteria()).thenReturn(List.of("criterion-1"));
        when(referentialService.getActiveCriteria(anyList())).thenReturn(null);

        evaluateService.doEvaluate(context, task, tempDir);

        verify(csvFileService, never()).getPrinter(any(), any());
    }

    @Test
    void doEvaluate_shouldThrowAsyncTaskException_whenCsvPrinterThrowsIOException() throws Exception {
        Context context = mock(Context.class);
        Task task = mock(Task.class);

        when(context.log()).thenReturn("ORG/DS/11");
        when(task.getCriteria()).thenReturn(List.of("criterion-1"));

        CriterionRest criterionRest = new CriterionRest();
        criterionRest.setCode("criterion_1");
        criterionRest.setUnit("kg");
        when(referentialService.getActiveCriteria(anyList())).thenReturn(List.of(criterionRest));

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

        CriterionRest criterionRest = new CriterionRest();
        criterionRest.setCode("criterion_1");
        criterionRest.setUnit("kg");
        when(referentialService.getActiveCriteria(anyList())).thenReturn(List.of(criterionRest));

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(FileType.class), any(Path.class))).thenReturn(printer);

        assertDoesNotThrow(() -> evaluateService.doEvaluate(context, task, tempDir));
    }
}
