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
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.Recommendation;
import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
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
import java.util.Map;

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
        InAiParameter param = mock(InAiParameter.class);
        when(param.getFramework()).thenReturn("llamacpp");
        when(param.getModelName()).thenReturn("llama3");
        when(param.getQuantization()).thenReturn("q2k");
        when(param.getNbParameters()).thenReturn(String.valueOf(8));
        when(param.getTotalGeneratedTokens()).thenReturn(BigInteger.valueOf(100));
        when(param.getIsInference()).thenReturn(true);
        when(param.getType()).thenReturn("MODEL");
        return param;
    }

    private InDatacenter mockDatacenter(String name, String location) {
        InDatacenter dc = mock(InDatacenter.class);
        when(dc.getName()).thenReturn(name);
        when(dc.getLocation()).thenReturn(location);
        return dc;
    }

    @Test
    void testDoEvaluateAiSuccess() throws Exception {
        when(context.getOrganization()).thenReturn("org");
        when(context.getDigitalServiceUid()).thenReturn("uid");
        when(context.getDigitalServiceName()).thenReturn("Digital Service 1");
        when(context.log()).thenReturn("context-log");
        when(context.isHasVirtualEquipments()).thenReturn(true);
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(context.getDigitalServiceVersionName()).thenReturn("Digital Service Version 1");

        when(task.getId()).thenReturn(99L);
        when(task.getCriteria()).thenReturn(List.of("CLIMATE_CHANGE"));

        InAiParameter aiParam = mockAiParameter();
        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(aiParam);
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid("uid")).thenReturn(mock(InAiInfrastructure.class));

        InDatacenter datacenter = mockDatacenter("DC1", "FR");
        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(List.of(datacenter));

        InPhysicalEquipment physicalEquipment = mock(InPhysicalEquipment.class);
        when(physicalEquipment.getDatacenterName()).thenReturn("DC1");
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new ArrayList<>(List.of(physicalEquipment)));

        InVirtualEquipment virtualEquipment = mock(InVirtualEquipment.class);
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(new ArrayList<>(List.of(virtualEquipment)));
        when(virtualEquipment.getLocation()).thenReturn("FR");
        when(virtualEquipment.getQuantity()).thenReturn(1.0);
        when(virtualEquipment.getDurationHour()).thenReturn(10.0);
        when(virtualEquipment.getWorkload()).thenReturn(0.5);

        OutputEstimation estimation = new OutputEstimation();
        estimation.setElectricityConsumption(BigDecimal.valueOf(100f));
        estimation.setRuntime(BigDecimal.valueOf(200f));
        estimation.setRecommendations(List.of(new Recommendation()));

        when(aiConfigurationMapper.toAIModelConfigRest(any())).thenReturn(List.of(mock(AIConfigurationRest.class)));
        when(aiService.runEstimation(any())).thenReturn(estimation);

        CriterionRest criterionRest = new CriterionRest("CLIMATE_CHANGE");
        criterionRest.setUnit("kg CO2 eq");
        when(referentialService.getLifecycleSteps()).thenReturn(List.of("use"));
        when(referentialService.getActiveCriteria(task.getCriteria().stream()
                .map(StringUtils::kebabToSnakeCase).toList())).thenReturn(List.of(criterionRest));
        when(referentialService.getElectricityMixQuartiles()).thenReturn(Map.of());
        when(referentialService.getHypotheses(any())).thenReturn(List.of());
        when(referentialService.getSipValueMap(any())).thenReturn(Map.of("CLIMATE_CHANGE", 2.0));

        CSVPrinter printer = mock(CSVPrinter.class);
        when(csvFileService.getPrinter(any(), eq(exportDirectory))).thenReturn(printer);

        ImpactEquipementPhysique impact = mock(ImpactEquipementPhysique.class);
        when(impact.getStatutIndicateur()).thenReturn("OK");
        when(impact.getTrace()).thenReturn("trace");
        when(impact.getCritere()).thenReturn("CLIMATE_CHANGE");
        when(impact.getQuantite()).thenReturn(1.0);
        when(impact.getImpactUnitaire()).thenReturn(2.0);
        when(impact.getConsoElecMoyenne()).thenReturn(10.0);
        when(impact.getDureeDeVie()).thenReturn(5.0);

        when(evaluateNumEcoEvalService.calculatePhysicalEquipment(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(impact));
        when(aggregationToOutput.keyPhysicalEquipment(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(List.of("PHYSICAL_KEY"));
        ImpactEquipementVirtuel vImpact = mock(ImpactEquipementVirtuel.class);
        when(vImpact.getStatutIndicateur()).thenReturn("OK");
        when(vImpact.getTrace()).thenReturn("trace");
        when(vImpact.getCritere()).thenReturn("CLIMATE_CHANGE");
        when(vImpact.getImpactUnitaire()).thenReturn(2.0);
        when(vImpact.getConsoElecMoyenne()).thenReturn(1.0);

        when(evaluateNumEcoEvalService.calculateVirtualEquipment(any(), any(), anyInt(), any(), any()))
                .thenReturn(List.of(vImpact));
        when(aggregationToOutput.keyVirtualEquipment(any(), any(), any(), any(), any()))
                .thenReturn(List.of("VIRTUAL_KEY"));
        when(evaluateNumEcoEvalService.getTotalVcpuCoreNumber(any())).thenReturn(4.0);
        when(evaluateNumEcoEvalService.getTotalDiskSize(any())).thenReturn(100.0);

        when(saveService.saveOutPhysicalEquipments(any(), anyLong(), any())).thenReturn(1);
        when(saveService.saveOutVirtualEquipments(any(), anyLong(), any())).thenReturn(1);

        aiEvaluationService.doEvaluateAi(context, task, exportDirectory);

        verify(taskRepository).save(task);
        verify(outAiRecoRepository).save(any());
        verify(csvFileService, atLeastOnce()).getPrinter(any(), eq(exportDirectory));
        verify(inPhysicalEquipmentRepository).save(any());
        verify(inVirtualEquipmentRepository).save(any());
    }

    @Test
    void testDoEvaluateAiMissingAiParamsThrows() {
        when(context.getDigitalServiceVersionUid()).thenReturn("uid");
        when(inAIParameterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(null);

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
        assertTrue(ex.getMessage().contains("the ai parameter doesn't exist"));
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
        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(Collections.emptyList());

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
        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(List.of(inDatacenter));
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(Collections.emptyList());

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
        when(inDatacenterRepository.findByDigitalServiceVersionUid("uid")).thenReturn(List.of(inDatacenter));
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(List.of(inPhysicalEquipment));
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid("uid")).thenReturn(Collections.emptyList());

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                aiEvaluationService.doEvaluateAi(context, task, exportDirectory));
        assertTrue(ex.getMessage().contains("the virtual equipements doesn't exist"));
    }

}