///*
// * G4IT
// * Copyright 2023 Sopra Steria
// *
// * This product includes software developed by
// * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
// */
//
//package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;
//
//import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
//import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
//import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
//import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
//import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
//import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
//import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
//import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
//import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
//import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
//import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
//import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
//import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
//import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
//import com.soprasteria.g4it.backend.apiparameterai.repository.AiParameterRepository;
//import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
//import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
//import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
//import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigInteger;
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class EvaluateAiServiceTest {
//    @Mock
//    private DigitalServiceRepository digitalServiceRepository;
//    @Mock
//    private AiParameterRepository inAIParameterRepository;
//    @Mock
//    private InAiInfrastructureRepository inAiInfrastructureRepository;
//    @Mock
//    private InDatacenterRepository inDatacenterRepository;
//    @Mock
//    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
//    @Mock
//    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
//    @Mock
//    private OutAiRecoRepository outAiRecoRepository;
//    @Mock
//    private TaskRepository taskRepository;
//    @Mock
//    private SaveService saveService;
//    @Mock
//    private ReferentialService referentialService;
//    @Mock
//    private CsvFileService csvFileService;
//    @Mock
//    private InputToCsvRecord inputToCsvRecord;
//    @Mock
//    private ImpactToCsvRecord impactToCsvRecord;
//    @Mock
//    private AiConfigurationMapper aiConfigurationMapper;
//    @Mock
//    private AiService aiService;
//    @InjectMocks
//    private EvaluateAiService evaluateAiService;
//    private AiParameter aiParameter;
//    private InAiInfrastructure inAiInfrastructure;
//    private String digitalServiceUid;
//    private List<InDatacenter> inDatacenterList;
//    private List<InPhysicalEquipment> inPhysicalEquipmentList;
//    private List<InVirtualEquipment> inVirtualEquipmentList;
//
//    @BeforeEach
//    void setUp() {
//
//        digitalServiceUid = "3f1db90c-c3cf-471c-bb48-ffde4bab99dc";
//
//        // Setup AiParameter entity
//        aiParameter = AiParameter.builder().build();
//        aiParameter.setDigitalServiceUid(digitalServiceUid);
//        aiParameter.setModelName("llama3");
//        aiParameter.setType("LLM");
//        aiParameter.setNbParameters("1000000");
//        aiParameter.setFramework("PyTorch");
//        aiParameter.setQuantization("INT8");
//        aiParameter.setTotalGeneratedTokens(BigInteger.valueOf(5000000));
//        aiParameter.setNumberUserYear(BigInteger.valueOf(10000));
//        aiParameter.setAverageNumberRequest(BigInteger.valueOf(500));
//        aiParameter.setAverageNumberToken(BigInteger.valueOf(100));
//        aiParameter.setIsInference(true);
//        aiParameter.setIsFinetuning(false);
//
//        // Setup AiInfrastructure entity
//        inAiInfrastructure = InAiInfrastructure.builder().build();
//
//        // Setup DC list
//        InDatacenter inDatacenter = InDatacenter.builder().build();
//        inDatacenterList.add(inDatacenter);
//
//        // Setup InPhysicalEquipment list
//        InPhysicalEquipment inPhysicalEquipment = InPhysicalEquipment.builder().build();
//        inPhysicalEquipmentList.add(inPhysicalEquipment);
//
//        // Setup InVirtualEquipment list
//        InVirtualEquipment inVirtualEquipment = InVirtualEquipment.builder().build();
//        inVirtualEquipmentList.add(inVirtualEquipment);
//    }
//
//    @Test
//    void doEvaluateAi() {
//        when(inAIParameterRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(aiParameter);
//        when(inAiInfrastructureRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(inAiInfrastructure);
//        when(inDatacenterRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(inDatacenterList);
//        when(inPhysicalEquipmentRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(inPhysicalEquipmentList);
//        when(inVirtualEquipmentRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(inVirtualEquipmentList);
//
//        //TODO tout le reste
//    }
//}
