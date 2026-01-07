package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.InAiParameterRepository;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.OutputEstimation;
import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EvaluateAiServiceTest {

    @InjectMocks
    private EvaluateAiService aiEvaluationService;

    @Mock
    private InAiParameterRepository inAIParameterRepository;
    @Mock
    private InAiInfrastructureRepository inAiInfrastructureRepository;
    @Mock
    private InDatacenterRepository inDatacenterRepository;
    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private OutAiRecoRepository outAiRecoRepository;
    @Mock
    private ReferentialService referentialService;
    @Mock
    private CsvFileService csvFileService;
    @Mock
    private EvaluateNumEcoEvalService evaluateNumEcoEvalService;
    @Mock
    private SaveService saveService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private AiService aiService;
    @Mock
    private AiConfigurationMapper aiConfigurationMapper;
    @Mock
    private InputToCsvRecord inputToCsvRecord;
    @Mock
    private ImpactToCsvRecord impactToCsvRecord;

    @Mock
    private AggregationToOutput aggregationToOutput;

    @Mock
    private Context context;
    @Mock
    private Task task;

    @Mock
    private Path exportDirectory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private InAiParameter mockAiParameter() {
        InAiParameter param = new InAiParameter();
        param.setFramework("llamacpp");
        param.setModelName("llama3");
        param.setQuantization("q2k");
        param.setNbParameters("8");
        param.setTotalGeneratedTokens(BigInteger.valueOf(100));
        param.setIsInference(true);
        param.setType("MODEL");
        return param;
    }


    private InDatacenter mockDatacenter(String name, String location) {
        InDatacenter dc = new InDatacenter();
        dc.setName(name);
        dc.setLocation(location);
        return dc;
    }


    @Test
    void testDoEvaluateAiSuccess() throws Exception {

        // -------- Context --------
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.getOrganization()).thenReturn("ORG");
        when(context.isHasVirtualEquipments()).thenReturn(true);
        when(task.getId()).thenReturn(1L);
        when(task.getCriteria()).thenReturn(List.of("CLIMATE_CHANGE"));

        // -------- AI PARAMETERS --------
        InAiParameter aiParam = new InAiParameter();
        aiParam.setFramework("llamacpp");
        aiParam.setModelName("llama3");
        aiParam.setQuantization("q4");
        aiParam.setNbParameters("8");
        aiParam.setTotalGeneratedTokens(BigInteger.valueOf(100));
        aiParam.setNumberUserYear(BigInteger.ONE);
        aiParam.setAverageNumberRequest(BigInteger.ONE);
        aiParam.setAverageNumberToken(BigInteger.ONE);
        aiParam.setIsInference(true);
        aiParam.setIsFinetuning(false);

        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(aiParam);

        // -------- INFRA --------
        InAiInfrastructure infra = new InAiInfrastructure();
        infra.setNbGpu(2L);
        infra.setGpuMemory(16L);
        infra.setInfrastructureType("server-28");

        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(infra);

        // -------- DATACENTER --------
        InDatacenter dc = new InDatacenter();
        dc.setName("DC1");
        dc.setLocation("FR");
        dc.setPue(1.5);

        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(dc));

        // -------- PHYSICAL EQUIPMENT --------
        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setDatacenterName("DC1");
        physical.setCpuCoreNumber(8.0);
        physical.setSizeMemoryGb(64.0);

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(new ArrayList<>(List.of(physical)));


        // -------- VIRTUAL EQUIPMENT --------
        InVirtualEquipment virtual = new InVirtualEquipment();
        virtual.setLocation("FR");
        virtual.setWorkload(0.5);

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(new ArrayList<>(List.of(virtual)));


        // -------- NumEcoEval mocks --------
        ImpactEquipementPhysique physicalImpact = mock(ImpactEquipementPhysique.class);
        when(physicalImpact.getStatutIndicateur()).thenReturn("OK");
        when(physicalImpact.getImpactUnitaire()).thenReturn(10.0);
        when(physicalImpact.getConsoElecMoyenne()).thenReturn(5.0);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(
                any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(physicalImpact));

        ImpactEquipementVirtuel virtualImpact = mock(ImpactEquipementVirtuel.class);
        when(virtualImpact.getImpactUnitaire()).thenReturn(2.0);
        when(virtualImpact.getConsoElecMoyenne()).thenReturn(1.0);

        when(evaluateNumEcoEvalService.calculateVirtualEquipment(
                any(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(List.of(virtualImpact));

        when(evaluateNumEcoEvalService.getTotalVcpuCoreNumber(any())).thenReturn(4.0);
        when(evaluateNumEcoEvalService.getTotalDiskSize(any())).thenReturn(100.0);

        // -------- CSV mocks --------
        when(csvFileService.getPrinter(any(), any()))
                .thenReturn(mock(CSVPrinter.class));

        when(aggregationToOutput.keyPhysicalEquipment(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(List.of("PHYSICAL"));

        when(aggregationToOutput.keyVirtualEquipment(any(), any(), any(), any(), any()))
                .thenReturn(List.of("VIRTUAL"));

        OutputEstimation estimation = new OutputEstimation();
        estimation.setElectricityConsumption(BigDecimal.valueOf(100));
        estimation.setRuntime(BigDecimal.valueOf(200));
        estimation.setRecommendations(List.of());

        when(aiService.runEstimation(any()))
                .thenReturn(estimation);

        // -------- Execute --------
        aiEvaluationService.doEvaluateAi(context, task, exportDirectory);

        // -------- Verify --------
        verify(taskRepository).save(task);
        verify(outAiRecoRepository).save(any());
    }


    @Test
    void testDoEvaluateAiMissingAiInfraThrows() {
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new InAiParameter());
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid")).thenReturn(null);

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
        assertTrue(ex.getMessage().contains("the ai infrastructure doesn't exist"));
    }

    @Test
    void testDoEvaluateAiMissingDatacenterThrows() {
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new InAiParameter());
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new InAiInfrastructure());
        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new ArrayList<>());

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
        assertTrue(ex.getMessage().contains("the data center doesn't exist"));
    }

    @Test
    void testDoEvaluateAiMissingPhysicalEqThrows() {
        InDatacenter inDatacenter = mockDatacenter("DC1", "FR");
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new InAiParameter());
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new InAiInfrastructure());
        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new ArrayList<>(List.of(inDatacenter)));
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new ArrayList<>());

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
        assertTrue(ex.getMessage().contains("the physical equipements doesn't exist"));
    }

    @Test
    void testDoEvaluateAiMissingVirutalEqThrows() {
        InDatacenter inDatacenter = mockDatacenter("DC1", "FR");
        InPhysicalEquipment inPhysicalEquipment = InPhysicalEquipment.builder().name("name").build();
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new InAiParameter());
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new InAiInfrastructure());
        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new ArrayList<>(List.of(inDatacenter)));
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new ArrayList<>(List.of(inPhysicalEquipment)));
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(Collections.emptyList());

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
        assertTrue(ex.getMessage().contains("the virtual equipements doesn't exist"));
    }

    @Test
    void testDoEvaluateAiWithoutVirtualEquipments() throws Exception {
        when(context.isHasVirtualEquipments()).thenReturn(false);
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(context.getDigitalServiceName()).thenReturn("DS");
        when(context.log()).thenReturn("log");

        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(mockAiParameter());

        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(mock(InAiInfrastructure.class));

        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(mockDatacenter("DC1", "FR")));

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(new ArrayList<>(List.of(mock(InPhysicalEquipment.class))));

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(new ArrayList<>()); // empty

        assertThrows(G4itRestException.class, () ->
                aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
    }

    @Test
    void testVirtualEquipmentCalculationReturnsEmptyList() throws Exception {

        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(context.isHasVirtualEquipments()).thenReturn(true);

        // --- Mock AI parameter
        InAiParameter param = new InAiParameter();
        param.setFramework("llama");
        param.setModelName("model");
        param.setQuantization("q4");
        param.setNbParameters("7");
        param.setTotalGeneratedTokens(BigInteger.valueOf(100));
        param.setIsInference(true);

        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(param);

        // --- Mock infra
        InAiInfrastructure infra = new InAiInfrastructure();
        infra.setGpuMemory(16L);
        infra.setNbGpu(2L);

        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(infra);

        // --- Mock datacenter
        InDatacenter dc = new InDatacenter();
        dc.setName("dc1");
        dc.setLocation("FR");

        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(new ArrayList<>(List.of(dc)));

        // --- Mock physical equipment
        InPhysicalEquipment pe = new InPhysicalEquipment();
        pe.setDatacenterName("dc1");
        pe.setCpuCoreNumber(8.0);
        pe.setSizeMemoryGb(64.0);

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(new ArrayList<>(List.of(pe)));

        // --- Mock virtual equipment
        InVirtualEquipment ve = new InVirtualEquipment();
        ve.setLocation("FR");
        ve.setWorkload(0.5);

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(new ArrayList<>(List.of(ve)));


        OutputEstimation estimation = new OutputEstimation();
        estimation.setElectricityConsumption(BigDecimal.valueOf(100));
        estimation.setRuntime(BigDecimal.valueOf(10));
        estimation.setRecommendations(List.of());

        when(aiService.runEstimation(any())).thenReturn(estimation);

        // Mock evaluation engine
        when(evaluateNumEcoEvalService.getTotalVcpuCoreNumber(any())).thenReturn(4.0);
        when(evaluateNumEcoEvalService.getTotalDiskSize(any())).thenReturn(100.0);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(mock(ImpactEquipementPhysique.class)));

        when(evaluateNumEcoEvalService.calculateVirtualEquipment(any(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(List.of(mock(ImpactEquipementVirtuel.class)));

        when(csvFileService.getPrinter(any(), any())).thenReturn(mock(CSVPrinter.class));

        // ACT
        aiEvaluationService.doEvaluateAi(context, task, Path.of("tmp"));

        // ASSERT
        verify(outAiRecoRepository).save(any());
    }


    @Test
    void testEvaluateEcomindReturnsNull() {
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");

        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(mockAiParameter());

        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(mock(InAiInfrastructure.class));

        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(mockDatacenter("DC1", "FR")));

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(mock(InPhysicalEquipment.class)));

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(mock(InVirtualEquipment.class)));

        // 👇 simulate Ecomind failure
        when(aiService.runEstimation(any())).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
    }

    @Test
    void testAggregationOverflowThrowsException() throws Exception {
        setupMinimalHappyPath();

        when(aggregationToOutput.keyVirtualEquipment(any(), any(), any(), any(), any()))
                .thenReturn(List.of("X"));

        // Force large aggregation map
        for (int i = 0; i < 600_000; i++) {
            evaluateNumEcoEvalService.calculateVirtualEquipment(
                    any(), any(), anyInt(), any(), any(), any(), any());
        }

        assertThrows(RuntimeException.class,
                () -> aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
    }

    private void setupMinimalHappyPath() {
        when(context.isHasVirtualEquipments()).thenReturn(true);
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(context.getDigitalServiceName()).thenReturn("DS");

        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(mockAiParameter());

        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(mock(InAiInfrastructure.class));

        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(mockDatacenter("DC1", "FR")));

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(mock(InPhysicalEquipment.class)));

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid"))
                .thenReturn(List.of(mock(InVirtualEquipment.class)));

        when(evaluateNumEcoEvalService.getTotalVcpuCoreNumber(any())).thenReturn(4.0);
        when(evaluateNumEcoEvalService.getTotalDiskSize(any())).thenReturn(100.0);
    }


}