package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InAiServiceMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import com.soprasteria.g4it.backend.apiinout.repository.InAiServiceRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiServiceRest;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadAiServiceServiceTest {

    @Mock
    private InAiServiceMapper inAiServiceMapper;

    @Mock
    private InAiServiceRepository inAiServiceRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Context context;

    @Mock
    private FileToLoad fileToLoad;

    @InjectMocks
    private LoadAiServiceService loadAiServiceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                loadAiServiceService,
                "entityManager",
                entityManager);
    }


    @Test
    void execute_shouldReturnEmptyList_whenAiServicesIsEmpty() {
        // Given
        List<InAiServiceRest> aiServices = List.of();

        // When
        List<LineError> result =
                loadAiServiceService.execute(context, fileToLoad, 0, aiServices);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(
                inAiServiceMapper,
                inAiServiceRepository,
                entityManager,
                messageSource,
                context,
                fileToLoad
        );
    }

    @Test
    void execute_shouldSaveValidAiService() {
        // Given
        InAiServiceRest aiService = createValidAiService();
        InAiService entity = mock(InAiService.class);

        when(inAiServiceMapper.toEntity(aiService))
                .thenReturn(entity);

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(aiService)
                );

        // Then
        assertTrue(result.isEmpty());

        verify(inAiServiceMapper).toEntity(aiService);
        verify(inAiServiceRepository).saveAll(List.of(entity));
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void execute_shouldReturnError_whenServiceNameIsMissing() {
        // Given
        mockContextAndFile();

        InAiServiceRest aiService = createValidAiService();
        aiService.setServiceName(null);

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(aiService)
                );

        // Then
        assertEquals(1, result.size());

        verify(inAiServiceMapper, never()).toEntity(any());

        verify(messageSource).getMessage(
                eq("field.mandatory"),
                argThat(args ->
                        args.length == 1 &&
                                "serviceName".equals(args[0])
                ),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void execute_shouldReturnError_whenProviderIsMissing() {
        // Given
        mockContextAndFile();

        InAiServiceRest aiService = createValidAiService();
        aiService.setProvider(null);

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(aiService)
                );

        // Then
        assertEquals(1, result.size());

        verify(inAiServiceMapper, never()).toEntity(any());

        verify(messageSource).getMessage(
                eq("field.mandatory"),
                argThat(args ->
                        args.length == 1 &&
                                "provider".equals(args[0])
                ),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void execute_shouldReturnError_whenModelIsMissing() {
        // Given
        mockContextAndFile();

        InAiServiceRest aiService = createValidAiService();
        aiService.setModel(null);

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(aiService)
                );

        // Then
        assertEquals(1, result.size());

        verify(inAiServiceMapper, never()).toEntity(any());

        verify(messageSource).getMessage(
                eq("field.mandatory"),
                argThat(args ->
                        args.length == 1 &&
                                "model".equals(args[0])
                ),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void execute_shouldReturnError_whenOutputTokensIsMissing() {
        // Given
        mockContextAndFile();

        InAiServiceRest aiService = createValidAiService();
        aiService.setOutputTokens(null);

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(aiService)
                );

        // Then
        assertEquals(1, result.size());

        verify(inAiServiceMapper, never()).toEntity(any());

        verify(messageSource).getMessage(
                eq("field.mandatory"),
                argThat(args ->
                        args.length == 1 &&
                                "outputTokens".equals(args[0])
                ),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void execute_shouldReturnErrorsForAllMissingMandatoryFields() {
        // Given
        mockContextAndFile();

        InAiServiceRest aiService = new InAiServiceRest();

        // serviceName = null
        // provider = null
        // model = null
        // outputTokens = null

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(aiService)
                );

        // Then
        assertEquals(4, result.size());

        verify(inAiServiceMapper, never()).toEntity(any());

        verify(messageSource, times(4)).getMessage(
                eq("field.mandatory"),
                any(Object[].class),
                eq(Locale.ENGLISH)
        );

        verify(inAiServiceRepository).saveAll(List.of());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void execute_shouldTreatBlankStringsAsMissing() {
        // Given
        mockContextAndFile();

        InAiServiceRest aiService = createValidAiService();

        aiService.setServiceName("   ");
        aiService.setProvider("");
        aiService.setModel("\t");

        // outputTokens remains valid

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(aiService)
                );

        // Then
        assertEquals(3, result.size());

        verify(inAiServiceMapper, never()).toEntity(any());

        verify(messageSource, times(3)).getMessage(
                eq("field.mandatory"),
                any(Object[].class),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void execute_shouldSaveOnlyValidRows_whenSomeRowsAreInvalid() {
        // Given
        mockContextAndFile();

        InAiServiceRest validService = createValidAiService();

        InAiServiceRest invalidService = createValidAiService();
        invalidService.setServiceName(null);

        InAiService validEntity = mock(InAiService.class);

        when(inAiServiceMapper.toEntity(validService))
                .thenReturn(validEntity);

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        0,
                        List.of(validService, invalidService)
                );

        // Then
        assertEquals(1, result.size());

        verify(inAiServiceMapper).toEntity(validService);
        verify(inAiServiceMapper, never()).toEntity(invalidService);

        ArgumentCaptor<List<InAiService>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(inAiServiceRepository).saveAll(captor.capture());

        List<InAiService> savedEntities = captor.getValue();

        assertEquals(1, savedEntities.size());
        assertSame(validEntity, savedEntities.get(0));

        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void execute_shouldCalculateCorrectLineNumber() {
        // Given
        mockContextAndFile();

        InAiServiceRest aiService = new InAiServiceRest();

        int pageNumber = 2;

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        pageNumber,
                        List.of(aiService)
                );

        // Then
        assertEquals(4, result.size());

        verify(messageSource, times(4)).getMessage(
                eq("field.mandatory"),
                any(Object[].class),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void execute_shouldCalculateCorrectLineNumberForSecondRow() {
        // Given
        mockContextAndFile();

        InAiServiceRest validService = createValidAiService();

        InAiServiceRest invalidService = new InAiServiceRest();

        InAiService entity = mock(InAiService.class);

        when(inAiServiceMapper.toEntity(validService))
                .thenReturn(entity);

        int pageNumber = 1;

        // When
        List<LineError> result =
                loadAiServiceService.execute(
                        context,
                        fileToLoad,
                        pageNumber,
                        List.of(validService, invalidService)
                );

        // Then
        assertEquals(4, result.size());

        verify(inAiServiceMapper).toEntity(validService);
        verify(inAiServiceMapper, never()).toEntity(invalidService);
    }

    @Test
    void execute_shouldDeleteExistingAiServicesWithSameServiceName_beforeSaving() {
        // Given
        Long inventoryId = 42L;
        when(context.getInventoryId()).thenReturn(inventoryId);

        InAiServiceRest aiService = createValidAiService();
        InAiService entity = mock(InAiService.class);
        when(entity.getServiceName()).thenReturn(aiService.getServiceName());

        when(inAiServiceMapper.toEntity(aiService)).thenReturn(entity);

        // When
        loadAiServiceService.execute(context, fileToLoad, 0, List.of(aiService));

        // Then
        verify(inAiServiceRepository).deleteByInventoryIdAndServiceNameIn(
                eq(inventoryId), eq(java.util.Set.of(aiService.getServiceName())));

        // Delete must happen before save to avoid duplicate rows on reload
        var inOrder = inOrder(inAiServiceRepository);
        inOrder.verify(inAiServiceRepository).deleteByInventoryIdAndServiceNameIn(any(), any());
        inOrder.verify(inAiServiceRepository).saveAll(anyList());
    }

    @Test
    void execute_shouldNotDeleteAiServices_whenInventoryIdIsNull() {
        // Given
        when(context.getInventoryId()).thenReturn(null);

        InAiServiceRest aiService = createValidAiService();
        InAiService entity = mock(InAiService.class);

        when(inAiServiceMapper.toEntity(aiService)).thenReturn(entity);

        // When
        loadAiServiceService.execute(context, fileToLoad, 0, List.of(aiService));

        // Then
        verify(inAiServiceRepository, never()).deleteByInventoryIdAndServiceNameIn(any(), any());
        verify(inAiServiceRepository).saveAll(List.of(entity));
    }

    @Test
    void getAiServiceCount_shouldReturnNumberOfAiServices() {
        // Given
        Long inventoryId = 123L;

        InAiService first = mock(InAiService.class);
        InAiService second = mock(InAiService.class);
        InAiService third = mock(InAiService.class);

        when(inAiServiceRepository.findByInventoryId(inventoryId))
                .thenReturn(List.of(first, second, third));

        // When
        Long result =
                loadAiServiceService.getAiServiceCount(inventoryId);

        // Then
        assertEquals(3L, result);

        verify(inAiServiceRepository)
                .findByInventoryId(inventoryId);
    }

    @Test
    void getAiServiceCount_shouldReturnZero_whenNoAiServicesExist() {
        // Given
        Long inventoryId = 123L;

        when(inAiServiceRepository.findByInventoryId(inventoryId))
                .thenReturn(List.of());

        // When
        Long result =
                loadAiServiceService.getAiServiceCount(inventoryId);

        // Then
        assertEquals(0L, result);

        verify(inAiServiceRepository)
                .findByInventoryId(inventoryId);
    }


    private InAiServiceRest createValidAiService() {
        InAiServiceRest aiService = new InAiServiceRest();

        aiService.setServiceName("OpenAI Service");
        aiService.setProvider("OpenAI");
        aiService.setModel("GPT");
        aiService.setOutputTokens(1000L);

        return aiService;
    }

    private void mockContextAndFile() {
        when(fileToLoad.getFilename()).thenReturn("test.csv");
        when(context.getLocale()).thenReturn(Locale.ENGLISH);
    }
}
