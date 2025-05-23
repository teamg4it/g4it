package com.soprasteria.g4it.backend.apiparameterai.controller;


import com.soprasteria.g4it.backend.apiparameterai.business.AiParameterService;
import com.soprasteria.g4it.backend.apiparameterai.mapper.AiParameterMapper;
//import com.soprasteria.g4it.backend.apiparameterai.model.AiParameterBO;
import com.soprasteria.g4it.backend.server.gen.api.AiParameterApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

/**
 * Digital Service endpoints.
 */
@AllArgsConstructor
@Validated
@Slf4j
@Service
public class ParamAiController implements AiParameterApiDelegate {

    @Autowired
    private AiParameterService aiParameterService;

    @Autowired
    private AiParameterMapper aiParameterMapper;

    @Override
    public ResponseEntity<AiParameterRest> createAiParameter(String subscriber, Long organization, String digitalServiceUid, AiParameterRest aiParameterRest) {

        AiParameterRest created = aiParameterService.createAiParameter(digitalServiceUid,aiParameterRest);

        return ResponseEntity.ok(aiParameterMapper.toDto(created));
    }
}


