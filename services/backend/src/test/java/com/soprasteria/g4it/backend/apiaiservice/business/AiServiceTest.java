package com.soprasteria.g4it.backend.apiaiservice.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.InfrastructureType;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.InputEstimationLLMInference;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.LLMModelConfig;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.OutputEstimation;
import com.soprasteria.g4it.backend.external.ecomindai.client.AiModelapiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
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
    void testGetAIModelConfigurations() throws JsonProcessingException {
        // Given
        String json = """
                [{"modelName":"llama3","nbParameters":"13b","framework":"llamacpp","quantization":"q2k"},{"modelName":"llama3","nbParameters":"8b","framework":"vllm","quantization":"none"},{"modelName":"llama3","nbParameters":"13b","framework":"vllm","quantization":"none"},{"modelName":"llama2","nbParameters":"13b","framework":"vllm","quantization":"none"}]
                """;
        String type = "LLM";

        ObjectMapper objMap = new ObjectMapper();


        when(aiModelapiClient.getAiModelConfig()).thenReturn(objMap.readValue(json, new TypeReference<List<LLMModelConfig>>() {
        }));

        // When
        List<LLMModelConfig> result = aiService.getAIModelConfigurations(type);

        // Then
        assertEquals(4, result.size());
        assertEquals("llama3", result.getFirst().getModelName());
        verify(aiModelapiClient, times(1)).getAiModelConfig();
    }

    @Test
    void testRunEstimation() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();


        // Given

        InputEstimationLLMInference inputEstimation = new InputEstimationLLMInference();
        inputEstimation.setRamSize(256);
        inputEstimation.setNbGpu(1);
        inputEstimation.setGpuMemory(24);
        inputEstimation.setNbCpuCores(4);
        inputEstimation.setInfrastructureType(InfrastructureType.SERVER_DC);
        inputEstimation.setModelName("llama3");
        inputEstimation.setNbParameters("13b");
        inputEstimation.setFramework("llamacpp");
        inputEstimation.setQuantization("8bit0");
        inputEstimation.setTotalGeneratedTokens(5000000);


        String estimationJson = """
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
                """;

        OutputEstimation outputEstimation = objectMapper.readValue(estimationJson, OutputEstimation.class);


        when(aiModelapiClient.runEstimation(inputEstimation)).thenReturn(outputEstimation);

        OutputEstimation result = aiService.runEstimation(inputEstimation);

        assertEquals(BigDecimal.valueOf(14.85), result.getElectricityConsumption());

        verify(aiModelapiClient).runEstimation(inputEstimation);
    }
}