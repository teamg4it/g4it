package com.soprasteria.g4it.backend.apiaiservice.controller;

import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiModelConfigMapper;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiServiceEstimationMapper;
import com.soprasteria.g4it.backend.server.gen.api.AiServiceApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIModelConfigRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIServiceEstimationRest;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@NoArgsConstructor
public class AiServiceRestController implements AiServiceApiDelegate{

    @Autowired
    AiService aiService;

    @Autowired
    AiModelConfigMapper aiModelConfigMapper;

    @Autowired
    AiServiceEstimationMapper aiServiceEstimationMapper;

    @Override
    public ResponseEntity<List<AIModelConfigRest>> getAIModelConfigurations(String type) {
        return ResponseEntity.ok(
                aiModelConfigMapper.toAIModelConfigRest(aiService.getAIModelConfigurations(type))
        );
    }

    @Override
    public ResponseEntity<List<AIServiceEstimationRest>> launchAIServiceEstimation(String type, String stage, List<@Valid AIConfigurationRest> aiConfigurationRest) {
        return ResponseEntity.ok(aiServiceEstimationMapper.toAIServiceEstimationRest(aiService.runEstimation(type,stage,aiConfigurationRest)));
    }
}
