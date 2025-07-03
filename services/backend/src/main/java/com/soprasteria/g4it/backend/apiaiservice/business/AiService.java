package com.soprasteria.g4it.backend.apiaiservice.business;

import com.soprasteria.g4it.backend.apiaiservice.mapper.AiConfigurationMapper;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.InputEstimationLLMInference;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.LLMModelConfig;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.OutputEstimation;
import com.soprasteria.g4it.backend.external.ecomindai.client.AiModelapiClient;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AiService {

    public static final String ECOMINAPI_VERSION = "1.0";
    public static final String ECOMINAPI_ENGINE = "EcomindAPI";
    /**
     * EcomindClient
     */
    private AiModelapiClient aiModelapiClient;
    @Autowired
    private AiConfigurationMapper aiConfigurationMapper;

    /**
     * Get BoaviztAPI countries with code.
     *
     * @return country map.
     */
    @SneakyThrows
    @Cacheable("GetAIModelConfigurations")
    public java.util.List<LLMModelConfig> getAIModelConfigurations(String type) {
        List<LLMModelConfig> configs = aiModelapiClient.getAiModelConfig();
        return configs;
    }

    @SneakyThrows
    @Cacheable("runEstimation")
    public OutputEstimation runEstimation(InputEstimationLLMInference inputEstimationLLMInference) {

        return aiModelapiClient.runEstimation(inputEstimationLLMInference);
    }

}
