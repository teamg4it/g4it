package com.soprasteria.g4it.backend.apiaiinfra.controller;

import com.soprasteria.g4it.backend.apiaiinfra.business.InAiInfrastructureService;
import com.soprasteria.g4it.backend.apiaiinfra.mapper.InAiInfrastructureMapper;
import com.soprasteria.g4it.backend.common.utils.AuthorizationUtils;
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
    @Autowired
    private AuthorizationUtils authorizationUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InAiInfrastructureRest> getDigitalServiceInputsAiInfraRest(String organization, Long workspace, String digitalServiceVersionUid) {
        // Check if EcoMindAi module is enabled or not
        authorizationUtils.checkEcomindAuthorization();
        return new ResponseEntity<>(inAiInfrastructureMapper.toRest(inAiInfrastructureService.getDigitalServiceInputsAiInfraRest(digitalServiceVersionUid)), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> postDigitalServiceInputsAiInfraRest(String organization, Long workspace, String digitalServiceVersionUid, InAiInfrastructureRest aiInfraRest) {
        // Check if EcoMindAi module is enabled or not
        authorizationUtils.checkEcomindAuthorization();
        return new ResponseEntity<>(inAiInfrastructureService.postDigitalServiceInputsAiInfra(digitalServiceVersionUid, aiInfraRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> updateDigitalServiceInputsAiInfraRest(String organization, Long workspace, String digitalServiceUid, InAiInfrastructureRest aiInfraRest) {
        // Check if EcoMindAi module is enabled or not
        authorizationUtils.checkEcomindAuthorization();
        return new ResponseEntity<>(inAiInfrastructureService.updateDigitalServiceInputsAiInfraRest(digitalServiceUid, aiInfraRest), HttpStatus.OK);
    }
}
