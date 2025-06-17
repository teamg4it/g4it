package com.soprasteria.g4it.backend.apiaiinfra.controller;

import com.soprasteria.g4it.backend.apiaiinfra.business.InAiInfrastructureService;
import com.soprasteria.g4it.backend.apiaiinfra.mapper.InAiInfrastructureMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceRestMapper;
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
public class InAiInfrastructureRestController implements AiInfraInputsApiDelegate {

    @Autowired
    InAiInfrastructureService inAiInfrastructureService;

    @Autowired
    private InAiInfrastructureMapper inAiInfrastructureMapper;

    @Override
    public ResponseEntity<InAiInfrastructureRest> getDigitalServiceInputsAiInfraRest(String subscriber, Long organization, String digitalServiceUid) {
        return new ResponseEntity<>(inAiInfrastructureMapper.toRest(inAiInfrastructureService.getDigitalServiceInputsAiInfraRest(digitalServiceUid)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<InPhysicalEquipmentRest> postDigitalServiceInputsAiInfraRest(String subscriber, Long organization, String digitalServiceUid, InAiInfrastructureRest aiInfraRest) {
        return new ResponseEntity<>(inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceUid,aiInfraRest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<InPhysicalEquipmentRest> updateDigitalServiceInputsAiInfraRest(String subscriber, Long organization, String digitalServiceUid, InAiInfrastructureRest aiInfraRest) {
        return new ResponseEntity<>(inAiInfrastructureService.updateDigitalServiceInputsAiInfraRest(digitalServiceUid,aiInfraRest), HttpStatus.OK);
    }
}
