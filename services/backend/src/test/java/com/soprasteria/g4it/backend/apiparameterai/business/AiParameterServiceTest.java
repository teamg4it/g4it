package com.soprasteria.g4it.backend.apiparameterai.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiparameterai.mapper.AiParameterMapper;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.AiParameterRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiParameterServiceTest {

    @Mock
    private AiParameterRepository aiParameterRepository;

    @Mock
    private AiParameterMapper aiParameterMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @InjectMocks
    private AiParameterService aiParameterService;

    private String digitalServiceUid;
    private AiParameterRest aiParameterRest;
    private AiParameter aiParameterEntity;
    private AiParameter savedAiParameterEntity;
    private DigitalService digitalService;
    private AiParameterRest expectedResult;

    @BeforeEach
    void setUp() {
        digitalServiceUid = "3f1db90c-c3cf-471c-bb48-ffde4bab99dc";

        // Setup AiParameterRest
        aiParameterRest = AiParameterRest.builder().build();
        aiParameterRest.setModelName("llama3");
        aiParameterRest.setStage("LLM");
        aiParameterRest.setNbParameters("1000000");
        aiParameterRest.setFramework("PyTorch");
        aiParameterRest.setQuantization("INT8");
        aiParameterRest.setTotalGeneratedTokens(Long.valueOf(5000000));
        aiParameterRest.setNumberUserYear(Long.valueOf(10000));
        aiParameterRest.setAverageNumberRequest(Long.valueOf(500));
        aiParameterRest.setAverageNumberToken(Long.valueOf(100));
        aiParameterRest.setIsInference(true);
        aiParameterRest.setIsFinetuning(false);

        // Setup AiParameter entity
        aiParameterEntity =  AiParameter.builder().build();
        aiParameterEntity.setModelName("llama3");
        aiParameterEntity.setStage("LLM");
        aiParameterEntity.setNbParameters("1000000");
        aiParameterEntity.setFramework("PyTorch");
        aiParameterEntity.setQuantization("INT8");
        aiParameterEntity.setTotalGeneratedTokens(BigInteger.valueOf(5000000));
        aiParameterEntity.setNumberUserYear(BigInteger.valueOf(10000));
        aiParameterEntity.setAverageNumberRequest(BigInteger.valueOf(500));
        aiParameterEntity.setAverageNumberToken(BigInteger.valueOf(100));
        aiParameterEntity.setIsInference(true);
        aiParameterEntity.setIsFinetuning(false);

        // Setup saved entity (with ID and dates)
        savedAiParameterEntity =  AiParameter.builder().build();
        savedAiParameterEntity.setId(1L);
        savedAiParameterEntity.setModelName("llama3");
        savedAiParameterEntity.setStage("LLM");
        savedAiParameterEntity.setNbParameters("1000000");
        savedAiParameterEntity.setFramework("PyTorch");
        savedAiParameterEntity.setQuantization("INT8");
        savedAiParameterEntity.setTotalGeneratedTokens(BigInteger.valueOf(5000000));
        savedAiParameterEntity.setNumberUserYear(BigInteger.valueOf(10000));
        savedAiParameterEntity.setAverageNumberRequest(BigInteger.valueOf(500));
        savedAiParameterEntity.setAverageNumberToken(BigInteger.valueOf(100));
        savedAiParameterEntity.setIsInference(true);
        savedAiParameterEntity.setIsFinetuning(false);
        savedAiParameterEntity.setDigitalServiceUid(digitalServiceUid);
        savedAiParameterEntity.setCreationDate(LocalDateTime.now());
        savedAiParameterEntity.setLastUpdateDate(LocalDateTime.now());

        // Setup DigitalService
        digitalService = new DigitalService();
        digitalService.setUid(digitalServiceUid);

        // Setup expected result
        expectedResult = new AiParameterRest();
        expectedResult.setId(1L);
        expectedResult.setModelName("llama3");
        expectedResult.setStage("LLM");
        expectedResult.setNbParameters("1000000");
        expectedResult.setFramework("PyTorch");
        expectedResult.setQuantization("INT8");
        expectedResult.setTotalGeneratedTokens(Long.valueOf(5000000));
        expectedResult.setNumberUserYear(Long.valueOf(10000));
        expectedResult.setAverageNumberRequest(Long.valueOf(500));
        expectedResult.setAverageNumberToken(Long.valueOf(100));
        expectedResult.setIsInference(true);
        expectedResult.setIsFinetuning(false);
    }

    @Test
    void createAiParameter_Success() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(aiParameterEntity);
        when(aiParameterRepository.save(any(AiParameter.class)))
                .thenReturn(savedAiParameterEntity);
        when(aiParameterMapper.toBusinessObject(savedAiParameterEntity))
                .thenReturn(expectedResult);

        // When
        AiParameterRest result = aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);

        // Verify repository interactions
        verify(digitalServiceRepository).findById(digitalServiceUid);
        verify(aiParameterRepository).save(any(AiParameter.class));

        // Verify mapper interactions
        verify(aiParameterMapper).toEntity(aiParameterRest);
        verify(aiParameterMapper).toBusinessObject(savedAiParameterEntity);
    }

    @Test
    void createAiParameter_DigitalServiceNotFound_ThrowsException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.empty());

        // When & Then
        G4itRestException exception = assertThrows(G4itRestException.class,
                () -> aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        assertEquals("404", exception.getCode());
        assertEquals(String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid),
                exception.getMessage());

        // Verify only digitalServiceRepository was called
        verify(digitalServiceRepository).findById(digitalServiceUid);
        verifyNoInteractions(aiParameterMapper, aiParameterRepository);
    }

    @Test
    void createAiParameter_VerifyEntitySetup() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(aiParameterEntity);
        when(aiParameterRepository.save(any(AiParameter.class)))
                .thenReturn(savedAiParameterEntity);
        when(aiParameterMapper.toBusinessObject(savedAiParameterEntity))
                .thenReturn(expectedResult);

        ArgumentCaptor<AiParameter> entityCaptor = ArgumentCaptor.forClass(AiParameter.class);

        // When
        aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then
        verify(aiParameterRepository).save(entityCaptor.capture());
        AiParameter capturedEntity = entityCaptor.getValue();

        assertEquals(digitalServiceUid, capturedEntity.getDigitalServiceUid());
        assertNotNull(capturedEntity.getCreationDate());
        assertNotNull(capturedEntity.getLastUpdateDate());
        assertEquals(capturedEntity.getCreationDate(), capturedEntity.getLastUpdateDate());
    }

    @Test
    void createAiParameter_WithNullParameterRest_ThrowsException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(null))
                .thenThrow(new IllegalArgumentException("Parameter cannot be null"));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> aiParameterService.createAiParameter(digitalServiceUid, null));

        verify(digitalServiceRepository).findById(digitalServiceUid);
        verify(aiParameterMapper).toEntity(null);
        verifyNoInteractions(aiParameterRepository);
    }

    @Test
    void createAiParameter_WithNullDigitalServiceUid_ThrowsException() {
        // When & Then
        assertThrows(Exception.class,
                () -> aiParameterService.createAiParameter(null, aiParameterRest));

        verify(digitalServiceRepository).findById(null);
        verifyNoInteractions(aiParameterMapper, aiParameterRepository);
    }

    @Test
    void createAiParameter_MapperToEntityException_PropagatesException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(aiParameterRest))
                .thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        verify(digitalServiceRepository).findById(digitalServiceUid);
        verify(aiParameterMapper).toEntity(aiParameterRest);
        verifyNoInteractions(aiParameterRepository);
    }

    @Test
    void createAiParameter_RepositorySaveException_PropagatesException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(aiParameterEntity);
        when(aiParameterRepository.save(any(AiParameter.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        verify(digitalServiceRepository).findById(digitalServiceUid);
        verify(aiParameterMapper).toEntity(aiParameterRest);
        verify(aiParameterRepository).save(any(AiParameter.class));
    }

    @Test
    void createAiParameter_MapperToBusinessObjectException_PropagatesException() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(aiParameterEntity);
        when(aiParameterRepository.save(any(AiParameter.class)))
                .thenReturn(savedAiParameterEntity);
        when(aiParameterMapper.toBusinessObject(savedAiParameterEntity))
                .thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        verify(digitalServiceRepository).findById(digitalServiceUid);
        verify(aiParameterMapper).toEntity(aiParameterRest);
        verify(aiParameterRepository).save(any(AiParameter.class));
        verify(aiParameterMapper).toBusinessObject(savedAiParameterEntity);
    }

    @Test
    void createAiParameter_WithValidBoundaryValues_Success() {
        // Given - Test with boundary values
        AiParameterRest boundaryParameterRest = AiParameterRest.builder().build();
        boundaryParameterRest.setModelName("llama3");
        boundaryParameterRest.setStage("LLM");
        boundaryParameterRest.setNbParameters("999999999");
        boundaryParameterRest.setFramework("Custom Framework");
        boundaryParameterRest.setQuantization("INT4");
        boundaryParameterRest.setTotalGeneratedTokens(Long.valueOf("999999999999999999"));
        boundaryParameterRest.setNumberUserYear(Long.valueOf("1000000000"));
        boundaryParameterRest.setAverageNumberRequest(Long.valueOf("50000"));
        boundaryParameterRest.setAverageNumberToken(Long.valueOf("10000"));
        boundaryParameterRest.setIsInference(true);
        boundaryParameterRest.setIsFinetuning(true);

        AiParameter boundaryParameterEntity = AiParameter.builder().build();
        boundaryParameterEntity.setModelName("llama3");
        boundaryParameterEntity.setStage("LLM");
        boundaryParameterEntity.setNbParameters("999999999");
        boundaryParameterEntity.setFramework("Custom Framework");
        boundaryParameterEntity.setQuantization("INT4");
        boundaryParameterEntity.setTotalGeneratedTokens(BigInteger.valueOf(Long.parseLong("999999999999999999")));
        boundaryParameterEntity.setNumberUserYear(BigInteger.valueOf(Long.parseLong(("1000000000"))));
        boundaryParameterEntity.setAverageNumberRequest(BigInteger.valueOf(Long.parseLong(("50000"))));
        boundaryParameterEntity.setAverageNumberToken(BigInteger.valueOf(Long.parseLong(("10000"))));
        boundaryParameterEntity.setIsInference(true);
        boundaryParameterEntity.setIsFinetuning(true);

        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(boundaryParameterRest))
                .thenReturn(boundaryParameterEntity);
        when(aiParameterRepository.save(any(AiParameter.class)))
                .thenReturn(savedAiParameterEntity);
        when(aiParameterMapper.toBusinessObject(savedAiParameterEntity))
                .thenReturn(expectedResult);

        // When
        AiParameterRest result = aiParameterService.createAiParameter(digitalServiceUid, boundaryParameterRest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void createAiParameter_ValidatesDateTimeSettings() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(aiParameterEntity);
        when(aiParameterRepository.save(any(AiParameter.class)))
                .thenReturn(savedAiParameterEntity);
        when(aiParameterMapper.toBusinessObject(savedAiParameterEntity))
                .thenReturn(expectedResult);

        ArgumentCaptor<AiParameter> entityCaptor = ArgumentCaptor.forClass(AiParameter.class);

        // When
        aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then
        verify(aiParameterRepository).save(entityCaptor.capture());
        AiParameter capturedEntity = entityCaptor.getValue();

        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

        assertTrue(capturedEntity.getCreationDate().isAfter(beforeCall));
        assertTrue(capturedEntity.getCreationDate().isBefore(afterCall));
        assertTrue(capturedEntity.getLastUpdateDate().isAfter(beforeCall));
        assertTrue(capturedEntity.getLastUpdateDate().isBefore(afterCall));
    }

    @Test
    void createAiParameter_VerifyMethodCallOrder() {
        // Given
        when(digitalServiceRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalService));
        when(aiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(aiParameterEntity);
        when(aiParameterRepository.save(any(AiParameter.class)))
                .thenReturn(savedAiParameterEntity);
        when(aiParameterMapper.toBusinessObject(savedAiParameterEntity))
                .thenReturn(expectedResult);

        // When
        aiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then - Verify call order using InOrder
        var inOrder = inOrder(digitalServiceRepository, aiParameterMapper, aiParameterRepository);
        inOrder.verify(digitalServiceRepository).findById(digitalServiceUid);
        inOrder.verify(aiParameterMapper).toEntity(aiParameterRest);
        inOrder.verify(aiParameterRepository).save(any(AiParameter.class));
        inOrder.verify(aiParameterMapper).toBusinessObject(savedAiParameterEntity);
    }
}
