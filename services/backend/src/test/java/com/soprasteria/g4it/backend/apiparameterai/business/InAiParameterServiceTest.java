package com.soprasteria.g4it.backend.apiparameterai.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiparameterai.mapper.InAiParameterMapper;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.InAiParameterRepository;
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
class InAiParameterServiceTest {

    @Mock
    private InAiParameterRepository inAiParameterRepository;

    @Mock
    private InAiParameterMapper inAiParameterMapper;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @InjectMocks
    private InAiParameterService inAiParameterService;

    private String digitalServiceUid;
    private AiParameterRest aiParameterRest;
    private InAiParameter inAiParameterEntity;
    private InAiParameter savedInAiParameterEntity;
    private DigitalServiceVersion digitalServiceVersion;
    private AiParameterRest expectedResult;

    @BeforeEach
    void setUp() {
        digitalServiceUid = "3f1db90c-c3cf-471c-bb48-ffde4bab99dc";

        // Setup AiParameterRest
        aiParameterRest = AiParameterRest.builder().build();
        aiParameterRest.setModelName("llama3");
        aiParameterRest.setType("LLM");
        aiParameterRest.setNbParameters("1000000");
        aiParameterRest.setFramework("PyTorch");
        aiParameterRest.setQuantization("INT8");
        aiParameterRest.setTotalGeneratedTokens(5000000L);
        aiParameterRest.setNumberUserYear(10000L);
        aiParameterRest.setAverageNumberRequest(500L);
        aiParameterRest.setAverageNumberToken(100L);
        aiParameterRest.setIsInference(true);
        aiParameterRest.setIsFinetuning(false);

        // Setup AiParameter entity
        inAiParameterEntity = InAiParameter.builder().build();
        inAiParameterEntity.setModelName("llama3");
        inAiParameterEntity.setType("LLM");
        inAiParameterEntity.setNbParameters("1000000");
        inAiParameterEntity.setFramework("PyTorch");
        inAiParameterEntity.setQuantization("INT8");
        inAiParameterEntity.setTotalGeneratedTokens(BigInteger.valueOf(5000000));
        inAiParameterEntity.setNumberUserYear(BigInteger.valueOf(10000));
        inAiParameterEntity.setAverageNumberRequest(BigInteger.valueOf(500));
        inAiParameterEntity.setAverageNumberToken(BigInteger.valueOf(100));
        inAiParameterEntity.setIsInference(true);
        inAiParameterEntity.setIsFinetuning(false);

        // Setup saved entity (with ID and dates)
        savedInAiParameterEntity = InAiParameter.builder().build();
        savedInAiParameterEntity.setId(1L);
        savedInAiParameterEntity.setModelName("llama3");
        savedInAiParameterEntity.setType("LLM");
        savedInAiParameterEntity.setNbParameters("1000000");
        savedInAiParameterEntity.setFramework("PyTorch");
        savedInAiParameterEntity.setQuantization("INT8");
        savedInAiParameterEntity.setTotalGeneratedTokens(BigInteger.valueOf(5000000));
        savedInAiParameterEntity.setNumberUserYear(BigInteger.valueOf(10000));
        savedInAiParameterEntity.setAverageNumberRequest(BigInteger.valueOf(500));
        savedInAiParameterEntity.setAverageNumberToken(BigInteger.valueOf(100));
        savedInAiParameterEntity.setIsInference(true);
        savedInAiParameterEntity.setIsFinetuning(false);
        savedInAiParameterEntity.setDigitalServiceUid(digitalServiceUid);
        savedInAiParameterEntity.setCreationDate(LocalDateTime.now());
        savedInAiParameterEntity.setLastUpdateDate(LocalDateTime.now());

        // Setup DigitalService
        digitalServiceVersion = new DigitalServiceVersion();
        digitalServiceVersion.setUid(digitalServiceUid);

        // Setup expected result
        expectedResult = new AiParameterRest();
        expectedResult.setId(1L);
        expectedResult.setModelName("llama3");
        expectedResult.setType("LLM");
        expectedResult.setNbParameters("1000000");
        expectedResult.setFramework("PyTorch");
        expectedResult.setQuantization("INT8");
        expectedResult.setTotalGeneratedTokens(5000000L);
        expectedResult.setNumberUserYear(10000L);
        expectedResult.setAverageNumberRequest(500L);
        expectedResult.setAverageNumberToken(100L);
        expectedResult.setIsInference(true);
        expectedResult.setIsFinetuning(false);
    }

    @Test
    void getAiParameter_shouldThrowIfDigitalServiceNotFound() {
        String uid = "non-existent-uid";

        when(digitalServiceVersionRepository.findById(uid)).thenReturn(Optional.empty());

        G4itRestException ex = assertThrows(G4itRestException.class,
                () -> inAiParameterService.getAiParameter(uid));

        assertEquals("404", ex.getCode());
        assertTrue(ex.getMessage().contains(uid));

        verify(digitalServiceVersionRepository).findById(uid);
        verifyNoInteractions(inAiParameterRepository, inAiParameterMapper);
    }

    @Test
    void getAiParameter_shouldReturnAiParameterRestIfFound() {
        String uid = "existing-uid";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        InAiParameter inAiParameter = new InAiParameter();
        AiParameterRest aiParameter = AiParameterRest.builder()
                .type("LLM")
                .build();

        when(digitalServiceVersionRepository.findById(uid)).thenReturn(Optional.of(dsv));
        when(inAiParameterRepository.findByDigitalServiceVersionUid(uid)).thenReturn(inAiParameter);
        when(inAiParameterMapper.toBusinessObject(inAiParameter)).thenReturn(aiParameter);

        AiParameterRest result = inAiParameterService.getAiParameter(uid);

        assertNotNull(result);
        assertEquals("LLM", result.getType());

        verify(digitalServiceVersionRepository).findById(uid);
        verify(inAiParameterRepository).findByDigitalServiceVersionUid(uid);
        verify(inAiParameterMapper).toBusinessObject(inAiParameter);
    }

    @Test
    void updateAiParameter_shouldThrowIfDigitalServiceNotFound() {
        String uid = "non-existent-uid";
        AiParameterRest inputDto = AiParameterRest.builder().type("LLM").build();

        when(digitalServiceVersionRepository.findById(uid)).thenReturn(Optional.empty());

        G4itRestException ex = assertThrows(G4itRestException.class,
                () -> inAiParameterService.updateAiParameter(uid, inputDto));

        assertEquals("404", ex.getCode());
        assertTrue(ex.getMessage().contains(uid));

        verify(digitalServiceVersionRepository).findById(uid);
        verifyNoMoreInteractions(inAiParameterRepository, inAiParameterMapper);
    }

    @Test
    void updateAiParameter_shouldUpdateAndReturnUpdatedEntity() {
        String uid = "existing-uid";

        DigitalServiceVersion dsv = new DigitalServiceVersion();
        AiParameterRest inputDto = AiParameterRest.builder().type("LLM").build();
        InAiParameter entity = new InAiParameter();

        when(digitalServiceVersionRepository.findById(uid)).thenReturn(Optional.of(dsv));
        when(inAiParameterRepository.findByDigitalServiceVersionUid(uid)).thenReturn(entity);

        doNothing().when(inAiParameterMapper).updateEntityFromDto(inputDto, entity);
        when(inAiParameterRepository.save(entity)).thenReturn(entity);
        when(inAiParameterMapper.toBusinessObject(entity)).thenReturn(inputDto);

        AiParameterRest result = inAiParameterService.updateAiParameter(uid, inputDto);

        assertNotNull(result);
        assertEquals("LLM", result.getType());

        verify(digitalServiceVersionRepository).findById(uid);
        verify(inAiParameterRepository).findByDigitalServiceVersionUid(uid);
        verify(inAiParameterMapper).updateEntityFromDto(inputDto, entity);
        verify(inAiParameterRepository).save(entity);
        verify(inAiParameterMapper).toBusinessObject(entity);
    }

    @Test
    void createAiParameter_Success() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(inAiParameterEntity);
        when(inAiParameterRepository.save(any(InAiParameter.class)))
                .thenReturn(savedInAiParameterEntity);
        when(inAiParameterMapper.toBusinessObject(savedInAiParameterEntity))
                .thenReturn(expectedResult);

        // When
        AiParameterRest result = inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);

        // Verify repository interactions
        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verify(inAiParameterRepository).save(any(InAiParameter.class));

        // Verify mapper interactions
        verify(inAiParameterMapper).toEntity(aiParameterRest);
        verify(inAiParameterMapper).toBusinessObject(savedInAiParameterEntity);
    }

    @Test
    void createAiParameter_DigitalServiceNotFound_ThrowsException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.empty());

        // When & Then
        G4itRestException exception = assertThrows(G4itRestException.class,
                () -> inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        assertEquals("404", exception.getCode());
        assertEquals(String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid),
                exception.getMessage());

        // Verify only digitalServiceRepository was called
        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verifyNoInteractions(inAiParameterMapper, inAiParameterRepository);
    }

    @Test
    void createAiParameter_VerifyEntitySetup() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(inAiParameterEntity);
        when(inAiParameterRepository.save(any(InAiParameter.class)))
                .thenReturn(savedInAiParameterEntity);
        when(inAiParameterMapper.toBusinessObject(savedInAiParameterEntity))
                .thenReturn(expectedResult);

        ArgumentCaptor<InAiParameter> entityCaptor = ArgumentCaptor.forClass(InAiParameter.class);

        // When
        inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then
        verify(inAiParameterRepository).save(entityCaptor.capture());
        InAiParameter capturedEntity = entityCaptor.getValue();

        assertEquals(digitalServiceUid, capturedEntity.getDigitalServiceVersionUid());
        assertNotNull(capturedEntity.getCreationDate());
        assertNotNull(capturedEntity.getLastUpdateDate());
        assertEquals(capturedEntity.getCreationDate(), capturedEntity.getLastUpdateDate());
    }

    @Test
    void createAiParameter_WithNullParameterRest_ThrowsException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(null))
                .thenThrow(new IllegalArgumentException("Parameter cannot be null"));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> inAiParameterService.createAiParameter(digitalServiceUid, null));

        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verify(inAiParameterMapper).toEntity(null);
        verifyNoInteractions(inAiParameterRepository);
    }

    @Test
    void createAiParameter_WithNullDigitalServiceUid_ThrowsException() {
        // When & Then
        assertThrows(Exception.class,
                () -> inAiParameterService.createAiParameter(null, aiParameterRest));

        verify(digitalServiceVersionRepository).findById(any());
        verifyNoInteractions(inAiParameterMapper, inAiParameterRepository);
    }

    @Test
    void createAiParameter_MapperToEntityException_PropagatesException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(aiParameterRest))
                .thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verify(inAiParameterMapper).toEntity(aiParameterRest);
        verifyNoInteractions(inAiParameterRepository);
    }

    @Test
    void createAiParameter_RepositorySaveException_PropagatesException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(inAiParameterEntity);
        when(inAiParameterRepository.save(any(InAiParameter.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verify(inAiParameterMapper).toEntity(aiParameterRest);
        verify(inAiParameterRepository).save(any(InAiParameter.class));
    }

    @Test
    void createAiParameter_MapperToBusinessObjectException_PropagatesException() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(inAiParameterEntity);
        when(inAiParameterRepository.save(any(InAiParameter.class)))
                .thenReturn(savedInAiParameterEntity);
        when(inAiParameterMapper.toBusinessObject(savedInAiParameterEntity))
                .thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest));

        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        verify(inAiParameterMapper).toEntity(aiParameterRest);
        verify(inAiParameterRepository).save(any(InAiParameter.class));
        verify(inAiParameterMapper).toBusinessObject(savedInAiParameterEntity);
    }

    @Test
    void createAiParameter_WithValidBoundaryValues_Success() {
        // Given - Test with boundary values
        AiParameterRest boundaryParameterRest = AiParameterRest.builder().build();
        boundaryParameterRest.setModelName("llama3");
        boundaryParameterRest.setType("LLM");
        boundaryParameterRest.setNbParameters("999999999");
        boundaryParameterRest.setFramework("Custom Framework");
        boundaryParameterRest.setQuantization("INT4");
        boundaryParameterRest.setTotalGeneratedTokens(Long.valueOf("999999999999999999"));
        boundaryParameterRest.setNumberUserYear(Long.valueOf("1000000000"));
        boundaryParameterRest.setAverageNumberRequest(Long.valueOf("50000"));
        boundaryParameterRest.setAverageNumberToken(Long.valueOf("10000"));
        boundaryParameterRest.setIsInference(true);
        boundaryParameterRest.setIsFinetuning(true);

        InAiParameter boundaryParameterEntity = InAiParameter.builder().build();
        boundaryParameterEntity.setModelName("llama3");
        boundaryParameterEntity.setType("LLM");
        boundaryParameterEntity.setNbParameters("999999999");
        boundaryParameterEntity.setFramework("Custom Framework");
        boundaryParameterEntity.setQuantization("INT4");
        boundaryParameterEntity.setTotalGeneratedTokens(BigInteger.valueOf(Long.parseLong("999999999999999999")));
        boundaryParameterEntity.setNumberUserYear(BigInteger.valueOf(Long.parseLong(("1000000000"))));
        boundaryParameterEntity.setAverageNumberRequest(BigInteger.valueOf(Long.parseLong(("50000"))));
        boundaryParameterEntity.setAverageNumberToken(BigInteger.valueOf(Long.parseLong(("10000"))));
        boundaryParameterEntity.setIsInference(true);
        boundaryParameterEntity.setIsFinetuning(true);

        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(boundaryParameterRest))
                .thenReturn(boundaryParameterEntity);
        when(inAiParameterRepository.save(any(InAiParameter.class)))
                .thenReturn(savedInAiParameterEntity);
        when(inAiParameterMapper.toBusinessObject(savedInAiParameterEntity))
                .thenReturn(expectedResult);

        // When
        AiParameterRest result = inAiParameterService.createAiParameter(digitalServiceUid, boundaryParameterRest);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void createAiParameter_WithEmptyModelName_ThrowsException() {
        // Given
        AiParameterRest invalidParameterRest = AiParameterRest.builder().build();
        invalidParameterRest.setModelName(""); // Empty model name
        invalidParameterRest.setType("LLM");
        invalidParameterRest.setNbParameters("999999999");
        invalidParameterRest.setFramework("Custom Framework");
        invalidParameterRest.setQuantization("INT4");
        invalidParameterRest.setTotalGeneratedTokens(Long.valueOf("999999999999999999"));
        invalidParameterRest.setNumberUserYear(Long.valueOf("1000000000"));
        invalidParameterRest.setAverageNumberRequest(Long.valueOf("50000"));
        invalidParameterRest.setAverageNumberToken(Long.valueOf("10000"));
        invalidParameterRest.setIsInference(true);
        invalidParameterRest.setIsFinetuning(false);


        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));

        // When & Then
        assertThrows(NullPointerException.class,
                () -> inAiParameterService.createAiParameter(digitalServiceUid, invalidParameterRest));
    }

    @Test
    void createAiParameter_WithAllValidTypes_Success() {
        // Test all valid stage values if they exist
        String[] validTypes = {"LLM", "CLASSIFICATION", "REGRESSION"}; // Adjust based on your enum/constants

        for (String type : validTypes) {
            // Given
            AiParameterRest boundaryParameterRest = AiParameterRest.builder().build();
            boundaryParameterRest.setModelName("llama3");
            boundaryParameterRest.setType(type);
            boundaryParameterRest.setNbParameters("999999999");
            boundaryParameterRest.setFramework("Custom Framework");
            boundaryParameterRest.setQuantization("INT4");
            boundaryParameterRest.setTotalGeneratedTokens(Long.valueOf("999999999999999999"));
            boundaryParameterRest.setNumberUserYear(Long.valueOf("1000000000"));
            boundaryParameterRest.setAverageNumberRequest(Long.valueOf("50000"));
            boundaryParameterRest.setAverageNumberToken(Long.valueOf("10000"));
            boundaryParameterRest.setIsInference(true);
            boundaryParameterRest.setIsFinetuning(true);

            InAiParameter boundaryParameterEntity = InAiParameter.builder().build();
            boundaryParameterEntity.setModelName("llama3");
            boundaryParameterEntity.setType(type);
            boundaryParameterEntity.setNbParameters("999999999");
            boundaryParameterEntity.setFramework("Custom Framework");
            boundaryParameterEntity.setQuantization("INT4");
            boundaryParameterEntity.setTotalGeneratedTokens(BigInteger.valueOf(Long.parseLong("999999999999999999")));
            boundaryParameterEntity.setNumberUserYear(BigInteger.valueOf(Long.parseLong(("1000000000"))));
            boundaryParameterEntity.setAverageNumberRequest(BigInteger.valueOf(Long.parseLong(("50000"))));
            boundaryParameterEntity.setAverageNumberToken(BigInteger.valueOf(Long.parseLong(("10000"))));
            boundaryParameterEntity.setIsInference(true);
            boundaryParameterEntity.setIsFinetuning(true);


            when(digitalServiceVersionRepository.findById(digitalServiceUid))
                    .thenReturn(Optional.of(digitalServiceVersion));
            when(inAiParameterMapper.toEntity(boundaryParameterRest))
                    .thenReturn(boundaryParameterEntity);
            when(inAiParameterRepository.save(any(InAiParameter.class)))
                    .thenReturn(savedInAiParameterEntity);
            when(inAiParameterMapper.toBusinessObject(savedInAiParameterEntity))
                    .thenReturn(expectedResult);

            // When
            AiParameterRest result = inAiParameterService.createAiParameter(digitalServiceUid, boundaryParameterRest);

            // Then
            assertNotNull(result);

            // Reset mocks for next iteration
            reset(digitalServiceVersionRepository, inAiParameterMapper, inAiParameterRepository);
        }
    }

    @Test
    void createAiParameter_ValidatesDateTimeSettings() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(inAiParameterEntity);
        when(inAiParameterRepository.save(any(InAiParameter.class)))
                .thenReturn(savedInAiParameterEntity);
        when(inAiParameterMapper.toBusinessObject(savedInAiParameterEntity))
                .thenReturn(expectedResult);

        ArgumentCaptor<InAiParameter> entityCaptor = ArgumentCaptor.forClass(InAiParameter.class);

        // When
        inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then
        verify(inAiParameterRepository).save(entityCaptor.capture());
        InAiParameter capturedEntity = entityCaptor.getValue();

        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

        assertTrue(capturedEntity.getCreationDate().isAfter(beforeCall));
        assertTrue(capturedEntity.getCreationDate().isBefore(afterCall));
        assertTrue(capturedEntity.getLastUpdateDate().isAfter(beforeCall));
        assertTrue(capturedEntity.getLastUpdateDate().isBefore(afterCall));
    }

    @Test
    void createAiParameter_VerifyMethodCallOrder() {
        // Given
        when(digitalServiceVersionRepository.findById(digitalServiceUid))
                .thenReturn(Optional.of(digitalServiceVersion));
        when(inAiParameterMapper.toEntity(aiParameterRest))
                .thenReturn(inAiParameterEntity);
        when(inAiParameterRepository.save(any(InAiParameter.class)))
                .thenReturn(savedInAiParameterEntity);
        when(inAiParameterMapper.toBusinessObject(savedInAiParameterEntity))
                .thenReturn(expectedResult);

        // When
        inAiParameterService.createAiParameter(digitalServiceUid, aiParameterRest);

        // Then - Verify call order using InOrder
        var inOrder = inOrder(digitalServiceVersionRepository, inAiParameterMapper, inAiParameterRepository);
        inOrder.verify(digitalServiceVersionRepository).findById(digitalServiceUid);
        inOrder.verify(inAiParameterMapper).toEntity(aiParameterRest);
        inOrder.verify(inAiParameterRepository).save(any(InAiParameter.class));
        inOrder.verify(inAiParameterMapper).toBusinessObject(savedInAiParameterEntity);
    }
}
