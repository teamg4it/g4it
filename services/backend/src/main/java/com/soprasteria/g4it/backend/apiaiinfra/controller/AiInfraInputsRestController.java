package com.soprasteria.g4it.backend.apiaiinfra.controller;

import com.soprasteria.g4it.backend.apiaiinfra.business.AiInfraInputsService;
import com.soprasteria.g4it.backend.server.gen.api.AiInfraInputsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiInfraRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AiInfraInputsRestController implements AiInfraInputsApiDelegate {

    @Autowired
    AiInfraInputsService aiInfraInputsApiService;

    @Override
    public ResponseEntity<InPhysicalEquipmentRest> postDigitalServiceInputsAiInfraRest(String subscriber, Long organization, String digitalServiceUid, AiInfraRest aiInfraRest) {
        return ResponseEntity.ok(aiInfraInputsApiService.postDigitalServiceInputsAiInfra(digitalServiceUid,aiInfraRest));
    }
}
