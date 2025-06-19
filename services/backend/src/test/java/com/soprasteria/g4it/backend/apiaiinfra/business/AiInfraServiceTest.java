package com.soprasteria.g4it.backend.apiaiinfra.business;

import com.soprasteria.g4it.backend.apiaiinfra.mapper.AiInfraMapper;
import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InDatacenterMapper;
import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiInfrastructureRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AiInfraServiceTest {
    @Mock
    private AiInfraMapper aiInfraMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private InDatacenterMapper inDatacenterMapper;

    @Mock
    private InDatacenterRepository inDatacenterRepository;

    @Mock
    private InPhysicalEquipmentMapper inPhysicalEquipmentMapper;

    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @Mock
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;

    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private InAiInfrastructureRepository inAiInfrastructureRepository;
    @InjectMocks
    private AiInfraInputsService aiInfraInputsService;
    private String digitalServiceUid;
    private InAiInfrastructureRest aiInfraRest;
    private AiInfraBO aiInfraBO;
    private DigitalService digitalService;
    private InDatacenter inDatacenter;
    private InPhysicalEquipment inPhysicalEquipment;
    private InVirtualEquipment inVirtualEquipment;
    private InPhysicalEquipmentRest expectedResult;
    private InAiInfrastructure inAiInfrastructure;

    @BeforeEach
    void setUp() {
        digitalServiceUid = "3f1db90c-c3cf-471c-bb48-ffde4bab99dc";
        // Setup InAiInfrastructureRest
        aiInfraRest = InAiInfrastructureRest.builder().build();
        aiInfraRest.setLocation("Paris");
        aiInfraRest.setPue(1.5);
        aiInfraRest.setNbCpuCores(8L);
        aiInfraRest.setRamSize(16L);
        aiInfraRest.setNbGpu(4L);
        aiInfraRest.setGpuMemory(8L);
        aiInfraRest.setComplementaryPue(2.0);
        aiInfraRest.setInfrastructureType(InAiInfrastructureRest.InfrastructureTypeEnum.SERVER_DC);

        // Setup AiInfraBO
        aiInfraBO = AiInfraBO.builder().build();
        aiInfraBO.setLocation("Paris");
        aiInfraBO.setPue(1.5);
        aiInfraBO.setNbCpuCores(8L);
        aiInfraBO.setRamSize(16L);
        aiInfraBO.setNbGpu(4L);
        aiInfraBO.setGpuMemory(8L);
        aiInfraBO.setComplementaryPue(2.0);
        aiInfraBO.setInfrastructureType(InAiInfrastructureRest.InfrastructureTypeEnum.SERVER_DC);

        inAiInfrastructure = new InAiInfrastructure();

        // Setup entities
        digitalService = new DigitalService();
        digitalService.setUid(digitalServiceUid);

        inDatacenter = new InDatacenter();
        inPhysicalEquipment = new InPhysicalEquipment();
        inVirtualEquipment = new InVirtualEquipment();

        expectedResult = new InPhysicalEquipmentRest();
    }

    @Test
    void postDigitalServiceInputsAiInfra_Success() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest)).thenReturn(aiInfraBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inDatacenterRepository.save(any(InDatacenter.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentRepository.save(any(InPhysicalEquipment.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentRepository.save(any(InVirtualEquipment.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(inPhysicalEquipment)).thenReturn(expectedResult);
        when(aiInfraMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);
        when(inAiInfrastructureRepository.save(any(InAiInfrastructure.class))).thenReturn(inAiInfrastructure);

        // When
        InPhysicalEquipmentRest result = aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);

        // Verify repository interactions
        verify(digitalServiceRepository).findById(digitalServiceUid);
        verify(inDatacenterRepository).save(any(InDatacenter.class));
        verify(inPhysicalEquipmentRepository).save(any(InPhysicalEquipment.class));
        verify(inVirtualEquipmentRepository).save(any(InVirtualEquipment.class));
        verify(inAiInfrastructureRepository).save(any(InAiInfrastructure.class));

        // Verify mapper interactions
        verify(aiInfraMapper).toBO(aiInfraRest);
        verify(inDatacenterMapper).toEntity(any(InDatacenterRest.class));
        verify(inPhysicalEquipmentMapper).toEntity(any(InPhysicalEquipmentRest.class));
        verify(inVirtualEquipmentMapper).toEntity(any(InVirtualEquipmentRest.class));
        verify(inPhysicalEquipmentMapper).toRest(inPhysicalEquipment);
        verify(aiInfraMapper).toEntity(aiInfraRest);
    }

    @Test
    void postDigitalServiceInputsAiInfra_DigitalServiceNotFound_ThrowsException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.empty());

        // When & Then
        G4itRestException exception = assertThrows(G4itRestException.class,
                () -> aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));

        assertEquals("404", exception.getCode());
        assertEquals(String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid),
                exception.getMessage());

        // Verify no other interactions
        verify(digitalServiceRepository).findById(digitalServiceUid);
        verifyNoMoreInteractions(digitalServiceRepository, inDatacenterRepository,
                inPhysicalEquipmentRepository, inVirtualEquipmentRepository);
    }

    @Test
    void postDigitalServiceInputsAiInfra_VerifyDatacenterCreation() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest)).thenReturn(aiInfraBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);
        when(aiInfraMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);

        ArgumentCaptor<InDatacenter> datacenterCaptor = ArgumentCaptor.forClass(InDatacenter.class);

        // When
        aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        verify(inDatacenterRepository).save(datacenterCaptor.capture());
        InDatacenter savedDatacenter = datacenterCaptor.getValue();

        assertEquals(digitalServiceUid, savedDatacenter.getDigitalServiceUid());
        assertEquals("Paris", savedDatacenter.getLocation());
        assertEquals(1.5, savedDatacenter.getPue());
        assertEquals("DataCenter1", savedDatacenter.getName());
        assertNotNull(savedDatacenter.getCreationDate());
        assertNotNull(savedDatacenter.getLastUpdateDate());
    }

    @Test
    void postDigitalServiceInputsAiInfra_VerifyPhysicalEquipmentCreation() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest)).thenReturn(aiInfraBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);
        when(aiInfraMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);

        ArgumentCaptor<InPhysicalEquipment> physicalEquipmentCaptor = ArgumentCaptor.forClass(InPhysicalEquipment.class);

        // When
        aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        verify(inPhysicalEquipmentRepository).save(physicalEquipmentCaptor.capture());
        InPhysicalEquipment savedPhysicalEquipment = physicalEquipmentCaptor.getValue();

        assertEquals(digitalServiceUid, savedPhysicalEquipment.getDigitalServiceUid());
        assertEquals("Server1", savedPhysicalEquipment.getName());
        assertEquals("blade-server--28", savedPhysicalEquipment.getModel());
        assertEquals("Manufacturer1", savedPhysicalEquipment.getManufacturer());
        assertEquals("DataCenter1", savedPhysicalEquipment.getDatacenterName());
        assertEquals(8.0, savedPhysicalEquipment.getCpuCoreNumber());
        assertEquals("Paris", savedPhysicalEquipment.getLocation());
        assertEquals("CpuType1", savedPhysicalEquipment.getCpuType());
        assertEquals(16.0, savedPhysicalEquipment.getSizeMemoryGb());
        assertEquals(1.0, savedPhysicalEquipment.getQuantity());
        assertNotNull(savedPhysicalEquipment.getDatePurchase());
        assertNotNull(savedPhysicalEquipment.getCreationDate());
        assertNotNull(savedPhysicalEquipment.getLastUpdateDate());
    }

    @Test
    void postDigitalServiceInputsAiInfra_VerifyVirtualEquipmentCreation() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest)).thenReturn(aiInfraBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);
        when(aiInfraMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);

        ArgumentCaptor<InVirtualEquipment> virtualEquipmentCaptor = ArgumentCaptor.forClass(InVirtualEquipment.class);

        // When
        aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        verify(inVirtualEquipmentRepository).save(virtualEquipmentCaptor.capture());
        InVirtualEquipment savedVirtualEquipment = virtualEquipmentCaptor.getValue();

        assertEquals(digitalServiceUid, savedVirtualEquipment.getDigitalServiceUid());
        assertEquals("Paris", savedVirtualEquipment.getLocation());
        assertEquals("VirtualEquipement1", savedVirtualEquipment.getName());
        assertEquals("Server1", savedVirtualEquipment.getPhysicalEquipmentName());
        assertEquals(InAiInfrastructureRest.InfrastructureTypeEnum.SERVER_DC.getValue(),
                savedVirtualEquipment.getInfrastructureType());
        assertEquals(16.0, savedVirtualEquipment.getSizeMemoryGb());
        assertEquals(8.0, savedVirtualEquipment.getVcpuCoreNumber());
        assertEquals(1.0, savedVirtualEquipment.getQuantity());
        assertNotNull(savedVirtualEquipment.getCreationDate());
        assertNotNull(savedVirtualEquipment.getLastUpdateDate());
    }

    @Test
    void postDigitalServiceInputsAiInfra_VerifyInAiInfrastructureCreation() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest)).thenReturn(aiInfraBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);
        when(aiInfraMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);

        ArgumentCaptor<InAiInfrastructure> inAiInfrastructureArgumentCaptor = ArgumentCaptor.forClass(InAiInfrastructure.class);

        // When
        aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        verify(inAiInfrastructureRepository).save(inAiInfrastructureArgumentCaptor.capture());
        InAiInfrastructure savedInAiInfrastructure = inAiInfrastructureArgumentCaptor.getValue();

        assertEquals(digitalServiceUid, savedInAiInfrastructure.getDigitalServiceUid());
        assertEquals(InAiInfrastructureRest.InfrastructureTypeEnum.SERVER_DC, savedInAiInfrastructure.getInfrastructureTypeEnum());
        assertEquals(4L, savedInAiInfrastructure.getNbGpu());
        assertEquals(8L, savedInAiInfrastructure.getGpuMemory());
        assertEquals(2.0, savedInAiInfrastructure.getComplementaryPue());
    }

    @Test
    void postDigitalServiceInputsAiInfra_WithNullValues_HandlesGracefully() {
        // Given
        aiInfraRest.setLocation(null);
        aiInfraRest.setPue(null);
        aiInfraRest.setNbCpuCores(null);
        aiInfraRest.setRamSize(null);

        aiInfraBO.setLocation(null);
        aiInfraBO.setPue(null);
        aiInfraBO.setNbCpuCores(null);
        aiInfraBO.setRamSize(null);

        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest)).thenReturn(aiInfraBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);
        when(aiInfraMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() ->
                aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));
    }

    @Test
    void postDigitalServiceInputsAiInfra_RepositorySaveException_PropagatesException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest)).thenReturn(aiInfraBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inDatacenterRepository.save(any(InDatacenter.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));
    }

    @Test
    void postDigitalServiceInputsAiInfra_MapperException_PropagatesException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiInfraMapper.toBO(aiInfraRest))
                .thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> aiInfraInputsService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));
    }
}
