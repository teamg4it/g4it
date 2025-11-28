package com.soprasteria.g4it.backend.apiaiinfra.business;

import com.soprasteria.g4it.backend.apiaiinfra.mapper.InAiInfrastructureMapper;
import com.soprasteria.g4it.backend.apiaiinfra.model.InAiInfrastructureBO;
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceVersionService;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.EcomindTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.EcomindTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AiInfraServiceTest {
    @Mock
    private InAiInfrastructureMapper inAiInfrastructureMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

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
    private DigitalServiceService digitalServiceService;

    @Mock
    private DigitalServiceVersionService digitalServiceVersionService;

    @Mock
    private InAiInfrastructureRepository inAiInfrastructureRepository;

    @Mock
    private InDatacenterService inDatacenterService;

    @Mock
    private InPhysicalEquipmentService inPhysicalEquipmentService;

    @Mock
    private InVirtualEquipmentService inVirtualEquipmentService;

    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;

    @InjectMocks
    private InAiInfrastructureService inAiInfrastructureService;
    private String digitalServiceUid;
    private InAiInfrastructureRest aiInfraRest;
    private InAiInfrastructureBO inAiInfrastructureBO;
    private InAiInfrastructure inAiInfrastructure;
    private DigitalServiceVersion digitalService;
    private InDatacenter inDatacenter;
    private InPhysicalEquipment inPhysicalEquipment;
    private InVirtualEquipment inVirtualEquipment;
    private InPhysicalEquipmentRest expectedResult;

    @BeforeEach
    void setUp() {
        digitalServiceUid = "3f1db90c-c3cf-471c-bb48-ffde4bab99dc";
        // Setup AiInfraRest
        aiInfraRest = InAiInfrastructureRest.builder().build();
        aiInfraRest.setLocation("Paris");
        aiInfraRest.setPue(1.5);
        aiInfraRest.setNbCpuCores(8L);
        aiInfraRest.setRamSize(16L);

        // Setup AiInfraBO
        inAiInfrastructureBO = InAiInfrastructureBO.builder().build();
        EcomindTypeBO ecomindTypeBO = new EcomindTypeBO();
        ecomindTypeBO.setCode("laptop-3");
        ecomindTypeBO.setLifespan(5.0);
        ecomindTypeBO.setValue("Laptop");
        inAiInfrastructureBO.setInfrastructureType(ecomindTypeBO);
        inAiInfrastructureBO.setLocation("Paris");
        inAiInfrastructureBO.setPue(1.5);
        inAiInfrastructureBO.setNbCpuCores(8L);
        inAiInfrastructureBO.setRamSize(16L);

        // Setup AiInfra
        inAiInfrastructure = new InAiInfrastructure();
        inAiInfrastructure.setComplementaryPue(1.5);
        inAiInfrastructure.setNbGpu(8L);
        inAiInfrastructure.setGpuMemory(16L);
        inAiInfrastructure.setInfrastructureType("laptop-3");

        // Setup entities
        digitalService = new DigitalServiceVersion();
        digitalService.setUid(digitalServiceUid);

        inDatacenter = new InDatacenter();
        inPhysicalEquipment = new InPhysicalEquipment();
        inVirtualEquipment = new InVirtualEquipment();

        expectedResult = new InPhysicalEquipmentRest();
    }

    @Test
    void postDigitalServiceInputsAiInfra_Success() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(inAiInfrastructureMapper.toBO(aiInfraRest)).thenReturn(inAiInfrastructureBO);
        when(inAiInfrastructureMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inDatacenterRepository.save(any(InDatacenter.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentRepository.save(any(InPhysicalEquipment.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentRepository.save(any(InVirtualEquipment.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(inPhysicalEquipment)).thenReturn(expectedResult);

        // When
        InPhysicalEquipmentRest result = inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);

        // Verify repository interactions
        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verify(inDatacenterRepository).save(any(InDatacenter.class));
        verify(inPhysicalEquipmentRepository).save(any(InPhysicalEquipment.class));
        verify(inVirtualEquipmentRepository).save(any(InVirtualEquipment.class));

        // Verify mapper interactions
        verify(inAiInfrastructureMapper).toBO(aiInfraRest);
        verify(inDatacenterMapper).toEntity(any(InDatacenterRest.class));
        verify(inPhysicalEquipmentMapper).toEntity(any(InPhysicalEquipmentRest.class));
        verify(inVirtualEquipmentMapper).toEntity(any(InVirtualEquipmentRest.class));
        verify(inPhysicalEquipmentMapper).toRest(inPhysicalEquipment);
    }

    @Test
    void testGetDigitalServiceInputsAiInfraRest_shouldReturnCorrectData() {
        String uid = "ds-123";

        DigitalServiceVersionBO mockDigitalServiceVersion = DigitalServiceVersionBO.builder()
                .uid("ds-123")
                .description("Test Service")
                .userName("tester")
                .isAi(true)
                .build();

        InAiInfrastructure entity = new InAiInfrastructure();

        InDatacenterRest dc = new InDatacenterRest();
        dc.setPue(1.4);
        dc.setLocation("Paris");

        InPhysicalEquipmentRest pe = new InPhysicalEquipmentRest();
        pe.setCpuCoreNumber(16.0);
        pe.setSizeMemoryGb(64.0);

        EcomindTypeRef ecomindTypeRef = new EcomindTypeRef();
        ecomindTypeRef.setReference("laptop-3");
        ecomindTypeRef.setLifespan(5.0);
        ecomindTypeRef.setDescription("Laptop");

        when(digitalServiceVersionService.getDigitalServiceVersion(uid)).thenReturn(mockDigitalServiceVersion);
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid(uid)).thenReturn(entity);
        when(inDatacenterService.getByDigitalServiceVersion(uid)).thenReturn(List.of(dc));
        when(inPhysicalEquipmentService.getByDigitalServiceVersion(uid)).thenReturn(List.of(pe));
        when(digitalServiceReferentialService.getEcomindDeviceType(inAiInfrastructureBO.getInfrastructureType().getCode())).thenReturn(ecomindTypeRef);
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid(uid)).thenReturn(inAiInfrastructure);
        when(inAiInfrastructureMapper.entityToBO(inAiInfrastructure)).thenReturn(inAiInfrastructureBO);

        InAiInfrastructureBO result = inAiInfrastructureService.getDigitalServiceInputsAiInfraRest(uid);

        assertEquals("Paris", result.getLocation());
        assertEquals(1.4, result.getPue());
        assertEquals(16L, result.getNbCpuCores());
        assertEquals(64L, result.getRamSize());
    }

    @Test
    void testGetDigitalServiceInputsAiInfraRest_shouldThrowIfDigitalServiceNotFound() {
        String uid = "ds-404";
        when(digitalServiceVersionService.getDigitalServiceVersion(uid)).thenReturn(null);

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                inAiInfrastructureService.getDigitalServiceInputsAiInfraRest(uid)
        );

        assertEquals("404", ex.getCode());
        assertTrue(ex.getMessage().contains("doesn't exist"));
    }

    @Test
    void testUpdateDigitalServiceInputsAiInfraRest_shouldUpdateAllAndReturnInPhysicalEquipmentRest() {
        // Arrange
        String uid = "ds-001";

        // Input REST object

        InAiInfrastructureRest inRest = InAiInfrastructureRest.builder()
                .location("Paris")
                .pue(1.3)
                .nbCpuCores(16L)
                .ramSize(64L)
                .build();

        // Mock DigitalService exists
        when(digitalServiceVersionRepository.findById(uid))
                .thenReturn(Optional.of(new DigitalServiceVersion()));

        // Mock mapping BO
        EcomindTypeBO ecomindTypeBO = new EcomindTypeBO();
        ecomindTypeBO.setCode("laptop-3");
        ecomindTypeBO.setLifespan(5.0);
        ecomindTypeBO.setValue("Laptop");
        InAiInfrastructureBO bo = InAiInfrastructureBO.builder()
                .infrastructureType(ecomindTypeBO)
                .location("Paris")
                .pue(1.3)
                .nbCpuCores(16L)
                .ramSize(64L)
                .build();
        when(inAiInfrastructureMapper.toBO(inRest)).thenReturn(bo);

        // Mock existing AI infra entity
        InAiInfrastructure entity = new InAiInfrastructure();
        entity.setId(42L);
        when(inAiInfrastructureRepository.findByDigitalServiceVersionUid(uid)).thenReturn(entity);
        when(inAiInfrastructureMapper.toEntity(inRest)).thenReturn(entity);

        // Mock datacenter
        InDatacenterRest datacenterRest = new InDatacenterRest();
        datacenterRest.setId(1L);
        when(inDatacenterService.getByDigitalServiceVersion(uid)).thenReturn(List.of(datacenterRest));

        // Mock physical equipment
        InPhysicalEquipmentRest physicalRest = new InPhysicalEquipmentRest();
        physicalRest.setId(2L);
        physicalRest.setName("PhysicalNode-1");
        when(inPhysicalEquipmentService.getByDigitalServiceVersion(uid)).thenReturn(List.of(physicalRest));

        // Mock virtual equipment
        InVirtualEquipmentRest virtualRest = new InVirtualEquipmentRest();
        virtualRest.setId(3L);
        when(inVirtualEquipmentService.getByDigitalServiceVersion(uid)).thenReturn(List.of(virtualRest));

        // Act
        InPhysicalEquipmentRest result = inAiInfrastructureService.updateDigitalServiceInputsAiInfraRest(uid, inRest);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Paris", result.getLocation());
        assertEquals(16.0, result.getCpuCoreNumber());
        assertEquals(64.0, result.getSizeMemoryGb());

        // Verify updates called
        verify(inDatacenterService).updateInDatacenter(eq(uid), eq(1L), any());
        verify(inPhysicalEquipmentService).updateInPhysicalEquipment(eq(uid), eq(2L), any());
        verify(inVirtualEquipmentService).updateInVirtualEquipment(eq(uid), eq(3L), any());
    }

    @Test
    void testUpdateDigitalServiceInputsAiInfraRest_shouldThrowIfDigitalServiceNotFound() {
        String uid = "ds-404";
        when(digitalServiceVersionRepository.findById(uid)).thenReturn(Optional.empty());

        // Input REST object
        InAiInfrastructureRest inRest = InAiInfrastructureRest.builder()
                .location("Paris")
                .pue(1.3)
                .nbCpuCores(16L)
                .ramSize(64L)
                .build();

        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                inAiInfrastructureService.updateDigitalServiceInputsAiInfraRest(uid, inRest)
        );

        assertEquals("404", ex.getCode());
        assertTrue(ex.getMessage().contains("doesn't exist"));
    }

    @Test
    void postDigitalServiceInputsAiInfra_DigitalServiceNotFound_ThrowsException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.empty());

        // When & Then
        G4itRestException exception = assertThrows(G4itRestException.class,
                () -> inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));

        assertEquals("404", exception.getCode());
        assertEquals(String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid),
                exception.getMessage());

        // Verify no other interactions
        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verifyNoMoreInteractions(digitalServiceVersionRepository, inDatacenterRepository,
                inPhysicalEquipmentRepository, inVirtualEquipmentRepository);
    }

    @Test
    void postDigitalServiceInputsAiInfra_VerifyDatacenterCreation() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(inAiInfrastructureMapper.toBO(aiInfraRest)).thenReturn(inAiInfrastructureBO);
        when(inAiInfrastructureMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);

        ArgumentCaptor<InDatacenter> datacenterCaptor = ArgumentCaptor.forClass(InDatacenter.class);

        // When
        inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        verify(inDatacenterRepository).save(datacenterCaptor.capture());
        InDatacenter savedDatacenter = datacenterCaptor.getValue();

        assertEquals(digitalServiceUid, savedDatacenter.getDigitalServiceVersionUid());
        assertEquals("Paris", savedDatacenter.getLocation());
        assertEquals(1.5, savedDatacenter.getPue());
        assertEquals("Datacenter", savedDatacenter.getName());
        assertNotNull(savedDatacenter.getCreationDate());
        assertNotNull(savedDatacenter.getLastUpdateDate());
    }

    @Test
    void postDigitalServiceInputsAiInfra_VerifyPhysicalEquipmentCreation() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(inAiInfrastructureMapper.toBO(aiInfraRest)).thenReturn(inAiInfrastructureBO);
        when(inAiInfrastructureMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);

        ArgumentCaptor<InPhysicalEquipment> physicalEquipmentCaptor = ArgumentCaptor.forClass(InPhysicalEquipment.class);

        // When
        inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        verify(inPhysicalEquipmentRepository).save(physicalEquipmentCaptor.capture());
        InPhysicalEquipment savedPhysicalEquipment = physicalEquipmentCaptor.getValue();

        assertEquals(digitalServiceUid, savedPhysicalEquipment.getDigitalServiceVersionUid());
        assertEquals("Physical equipment", savedPhysicalEquipment.getName());
        assertEquals("laptop-3", savedPhysicalEquipment.getModel());
        assertEquals("Manufacturer1", savedPhysicalEquipment.getManufacturer());
        assertEquals("Datacenter", savedPhysicalEquipment.getDatacenterName());
        assertEquals(8.0, savedPhysicalEquipment.getCpuCoreNumber());
        assertEquals("Paris", savedPhysicalEquipment.getLocation());
        assertEquals(16.0, savedPhysicalEquipment.getSizeMemoryGb());
        assertEquals(1.0, savedPhysicalEquipment.getQuantity());
        assertNotNull(savedPhysicalEquipment.getDatePurchase());
        assertNotNull(savedPhysicalEquipment.getCreationDate());
        assertNotNull(savedPhysicalEquipment.getLastUpdateDate());
    }

    @Test
    void postDigitalServiceInputsAiInfra_VerifyVirtualEquipmentCreation() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(inAiInfrastructureMapper.toBO(aiInfraRest)).thenReturn(inAiInfrastructureBO);
        when(inAiInfrastructureMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);

        ArgumentCaptor<InVirtualEquipment> virtualEquipmentCaptor = ArgumentCaptor.forClass(InVirtualEquipment.class);

        // When
        inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest);

        // Then
        verify(inVirtualEquipmentRepository).save(virtualEquipmentCaptor.capture());
        InVirtualEquipment savedVirtualEquipment = virtualEquipmentCaptor.getValue();

        assertEquals(digitalServiceUid, savedVirtualEquipment.getDigitalServiceVersionUid());
        assertEquals("Paris", savedVirtualEquipment.getLocation());
        assertEquals("Virtual equipment", savedVirtualEquipment.getName());
        assertEquals("Physical equipment", savedVirtualEquipment.getPhysicalEquipmentName());
        assertEquals("laptop-3",
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

        inAiInfrastructureBO.setLocation(null);
        inAiInfrastructureBO.setPue(null);
        inAiInfrastructureBO.setNbCpuCores(null);
        inAiInfrastructureBO.setRamSize(null);

        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(inAiInfrastructureMapper.toBO(aiInfraRest)).thenReturn(inAiInfrastructureBO);
        when(inAiInfrastructureMapper.toEntity(aiInfraRest)).thenReturn(inAiInfrastructure);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inPhysicalEquipmentMapper.toEntity(any(InPhysicalEquipmentRest.class))).thenReturn(inPhysicalEquipment);
        when(inVirtualEquipmentMapper.toEntity(any(InVirtualEquipmentRest.class))).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(any(InPhysicalEquipment.class))).thenReturn(expectedResult);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() ->
                inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));
    }

    @Test
    void postDigitalServiceInputsAiInfra_RepositorySaveException_PropagatesException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(inAiInfrastructureMapper.toBO(aiInfraRest)).thenReturn(inAiInfrastructureBO);
        when(inDatacenterMapper.toEntity(any(InDatacenterRest.class))).thenReturn(inDatacenter);
        when(inDatacenterRepository.save(any(InDatacenter.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));
    }

    @Test
    void postDigitalServiceInputsAiInfra_MapperException_PropagatesException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(inAiInfrastructureMapper.toBO(aiInfraRest))
                .thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest));
    }
}
