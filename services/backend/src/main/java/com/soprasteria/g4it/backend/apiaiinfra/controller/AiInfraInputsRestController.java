package com.soprasteria.g4it.backend.apiaiinfra.controller;

import com.soprasteria.g4it.backend.apiaiinfra.business.AiInfraInputsService;
import com.soprasteria.g4it.backend.server.gen.api.AiInfraInputsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiInfrastructureRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AiInfraInputsRestController implements AiInfraInputsApiDelegate {

    @Autowired
    AiInfraInputsService aiInfraInputsApiService;

    @Override
    public ResponseEntity<InPhysicalEquipmentRest> postDigitalServiceInputsInAiInfrastructureRest(String subscriber, Long organization, String digitalServiceUid, InAiInfrastructureRest aiInfraRest) {
        return new ResponseEntity<>(aiInfraInputsApiService.postDigitalServiceInputsAiInfra(digitalServiceUid, aiInfraRest), HttpStatus.CREATED);
    }
}
