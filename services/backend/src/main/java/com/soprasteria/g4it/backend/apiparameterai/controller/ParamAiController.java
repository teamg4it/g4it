package com.soprasteria.g4it.backend.apiparameterai.controller;


import com.soprasteria.g4it.backend.apiparameterai.business.AiParameterService;
import com.soprasteria.g4it.backend.apiparameterai.mapper.AiParameterMapper;
import com.soprasteria.g4it.backend.apiparameterai.model.AiParameterBO;
import com.soprasteria.g4it.backend.server.gen.api.AiParameterApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

/**
 * Digital Service endpoints.
 */

public class ParamAiController implements AiParameterApiDelegate {

    @Autowired
    private AiParameterService aiParameterService;

    @Autowired
    private AiParameterMapper aiParameterMapper;

    @Override
    public ResponseEntity<AiParameterRest> createAiParameter(
            String subscriber,
            Long organization,
            AiParameterRest aiParameterRest
    ) {

        AiParameterBO aiParameterBO = aiParameterMapper.toBusinessObject(aiParameterRest);

        AiParameterBO created = aiParameterService.createAiParameter(aiParameterBO);

        return ResponseEntity.ok(aiParameterMapper.toDto(created));

    }
}

