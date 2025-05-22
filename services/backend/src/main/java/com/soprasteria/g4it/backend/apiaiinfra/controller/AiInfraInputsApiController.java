package com.soprasteria.g4it.backend.apiaiinfra.controller;

import com.soprasteria.g4it.backend.apiaiinfra.business.AiInfraInputsApiService;
import com.soprasteria.g4it.backend.server.gen.api.AiInfraInputsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiInfraRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class AiInfraInputsApiController implements AiInfraInputsApiDelegate {

    @Autowired
    private AiInfraInputsApiService aiInfraInputsApiService;

    @Override
    public ResponseEntity<InDatacenterRest> postDigitalServiceInputsAiInfraRest(String subscriber, Long organization, String digitalServiceUid, AiInfraRest aiInfraRest) {
        return ResponseEntity.ok(aiInfraInputsApiService.postDigitalServiceInputsAiInfra(digitalServiceUid,aiInfraRest));
    }
}
