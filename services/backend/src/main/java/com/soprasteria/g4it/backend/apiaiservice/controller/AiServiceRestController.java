package com.soprasteria.g4it.backend.apiaiservice.controller;

import com.soprasteria.g4it.backend.apiaiservice.business.AiService;
import com.soprasteria.g4it.backend.apiaiservice.mapper.AiModelConfigMapper;
import com.soprasteria.g4it.backend.server.gen.api.AiServiceApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIModelConfigRest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@NoArgsConstructor
public class AiServiceRestController implements AiServiceApiDelegate {

    @Autowired
    AiService aiService;

    @Autowired
    AiModelConfigMapper aiModelConfigMapper;

    @Override
    public ResponseEntity<List<AIModelConfigRest>> getAIModelConfigurations(String type) {
        return ResponseEntity.ok(
                aiModelConfigMapper.toAIModelConfigRest(aiService.getAIModelConfigurations(type))
        );
    }


}
