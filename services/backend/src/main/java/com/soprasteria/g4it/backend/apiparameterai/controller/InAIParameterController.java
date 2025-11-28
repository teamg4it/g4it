package com.soprasteria.g4it.backend.apiparameterai.controller;


import com.soprasteria.g4it.backend.apiparameterai.business.InAiParameterService;
import com.soprasteria.g4it.backend.common.utils.AuthorizationUtils;
import com.soprasteria.g4it.backend.server.gen.api.AiParameterApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Digital Service endpoints.
 */
@AllArgsConstructor
@Validated
@Slf4j
@Service
public class InAIParameterController implements AiParameterApiDelegate {

    @Autowired
    private InAiParameterService inAiParameterService;

    @Autowired
    private AuthorizationUtils authorizationUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AiParameterRest> createAiParameter(String organization, Long workspace, String digitalServiceVersionUid, AiParameterRest aiParameterRest) {
        // Check if EcoMindAi module is enabled or not
        authorizationUtils.checkEcomindAuthorization();
        AiParameterRest created = inAiParameterService.createAiParameter(digitalServiceVersionUid, aiParameterRest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AiParameterRest> getAiParameter(String organization, Long workspace, String digitalServiceVersionUid) {
        // Check if EcoMindAi module is enabled or not
        authorizationUtils.checkEcomindAuthorization();
        return new ResponseEntity<>(inAiParameterService.getAiParameter(digitalServiceVersionUid), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AiParameterRest> updateAiParameter(String organization, Long workspace, String digitalServiceVersionUid, AiParameterRest aiParameterRest) {
        // Check if EcoMindAi module is enabled or not
        authorizationUtils.checkEcomindAuthorization();
        return new ResponseEntity<>(inAiParameterService.updateAiParameter(digitalServiceVersionUid, aiParameterRest), HttpStatus.OK);
    }
}


