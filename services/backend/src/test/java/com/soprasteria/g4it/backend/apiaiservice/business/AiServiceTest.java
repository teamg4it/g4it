package com.soprasteria.g4it.backend.apiaiservice.business;

import com.soprasteria.g4it.backend.external.ecomindai.client.AiModelapiClient;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIModelConfigBO;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIServiceEstimationBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class AiServiceTest {

    @Mock
    private AiModelapiClient aiModelapiClient;

    @Mock
    private AiConfigurationMapper aiConfigurationMapper;

    @InjectMocks
    private AiService aiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAIModelConfigurations() {
        // Given
        String json = """
                [{"modelName":"llama3","parameters":"13b","framework":"llamacpp","quantization":"q2k"},{"modelName":"llama3","parameters":"8b","framework":"vllm","quantization":"none"},{"modelName":"llama3","parameters":"13b","framework":"vllm","quantization":"none"},{"modelName":"llama2","parameters":"13b","framework":"vllm","quantization":"none"}]
                """;
        String type ="LLM";
        when(aiModelapiClient.getAiModelConfig(type)).thenReturn(json);

        // When
        List<AIModelConfigBO> result = aiService.getAIModelConfigurations(type);

        // Then
        assertEquals(4, result.size());
        assertEquals("llama3", result.getFirst().getModelName());
        verify(aiModelapiClient, times(1)).getAiModelConfig(type);
    }

    @Test
    void testRunEstimation() {
        // Given
        AIConfigurationRest rest = AIConfigurationRest.builder().build();
        rest.setModelName("llama3");
        rest.setNbParameters("13b");
        rest.setFramework("llamacpp");
        rest.setQuantization("8bit0");
        rest.setTotalGeneratedTokens(5000000L);

        List<AIConfigurationRest> restList = List.of(rest);

        // BO équivalent (simulé)
        AIConfigurationBO bo = AIConfigurationBO.builder().build();
        bo.setModelName("llama3");
        bo.setNbParameters("13b");
        bo.setFramework("llamacpp");
        bo.setQuantization("8bit0");
        bo.setTotalGeneratedTokens(5000000L);

        List<AIConfigurationBO> boList = List.of(bo);

        String estimationJson = """
        [
          {
            "electricityConsumption": 14.85,
            "runtime": 1322,
            "recommendations": [
              {
                "type": "Quantified",
                "topic": "⚡ Use the right framework !",
                "example": "Using the framework vllm instead of llamacpp for some model can lead to a reduction of impact by",
                "expectedReduction": "18%"
              }
            ]
          }
        ]
    """;

        when(aiConfigurationMapper.toAIConfigurationBO(restList)).thenReturn(boList);
        when(aiModelapiClient.runEstimation("LLM", "INFERENCE", boList)).thenReturn(estimationJson);

        // When
        List<AIServiceEstimationBO> result = aiService.runEstimation("LLM", "INFERENCE", restList);

        // Then
        assertEquals(1, result.size());
        assertEquals(14.85, result.getFirst().getElectricityConsumption(), 0.001);
        assertEquals(1, result.getFirst().getRecommendations().size());

        verify(aiConfigurationMapper).toAIConfigurationBO(restList);
        verify(aiModelapiClient).runEstimation("LLM", "INFERENCE", boList);
    }
}