package com.soprasteria.g4it.backend.apiaiinfra.business;

import com.soprasteria.g4it.backend.apiaiinfra.mapper.AiInfraMapper;
import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
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
import com.soprasteria.g4it.backend.server.gen.api.dto.AiInfraRest;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private AiInfraInputsService aiInfraInputsService;
    private String digitalServiceUid;
    private AiInfraRest aiInfraRest;
    private AiInfraBO aiInfraBO;
    private DigitalService digitalService;
    private InDatacenter inDatacenter;
    private InPhysicalEquipment inPhysicalEquipment;
    private InVirtualEquipment inVirtualEquipment;
    private InPhysicalEquipmentRest expectedResult;

    @BeforeEach
    void setUp() {
        digitalServiceUid = "3f1db90c-c3cf-471c-bb48-ffde4bab99dc";
        // Setup AiInfraRest
        aiInfraRest = AiInfraRest.builder().build();
        aiInfraRest.setLocation("Paris");
        aiInfraRest.setPue(1.5);
        aiInfraRest.setNbCpuCores(8L);
        aiInfraRest.setRamSize(16L);

        // Setup AiInfraBO
        aiInfraBO = AiInfraBO.builder().build();
        aiInfraBO.setLocation("Paris");
        aiInfraBO.setPue(1.5);
        aiInfraBO.setNbCpuCores(8L);
        aiInfraBO.setRamSize(16L);

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

        // Verify mapper interactions
        verify(aiInfraMapper).toBO(aiInfraRest);
        verify(inDatacenterMapper).toEntity(any(InDatacenterRest.class));
        verify(inPhysicalEquipmentMapper).toEntity(any(InPhysicalEquipmentRest.class));
        verify(inVirtualEquipmentMapper).toEntity(any(InVirtualEquipmentRest.class));
        verify(inPhysicalEquipmentMapper).toRest(inPhysicalEquipment);
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
        assertEquals(AiInfraRest.InfrastructureTypeEnum.SERVER_DC.getValue(),
                savedVirtualEquipment.getInfrastructureType());
        assertEquals(16.0, savedVirtualEquipment.getSizeMemoryGb());
        assertEquals(8.0, savedVirtualEquipment.getVcpuCoreNumber());
        assertEquals(1.0, savedVirtualEquipment.getQuantity());
        assertNotNull(savedVirtualEquipment.getCreationDate());
        assertNotNull(savedVirtualEquipment.getLastUpdateDate());
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
