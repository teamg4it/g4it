package com.soprasteria.g4it.backend.apirecomandation.controller;

import com.soprasteria.g4it.backend.apirecomandation.business.OutAiRecoService;
import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import com.soprasteria.g4it.backend.server.gen.api.AiRecommendationsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class OutAiRecoController implements AiRecommendationsApiDelegate {


    @Autowired
    private OutAiRecoService outAiRecoService;

    @Autowired
    private OutAiRecoMapper outAiRecoMapper;

    /**
     * GET /sub/orga/digitalservice/outputs/ai-recomandation
     */

    @Override
    public ResponseEntity<List<OutAiRecommendationRest>> getAiRecommendations(String subscriber, Long organization, String digitalServiceUid) {
        List<OutAiRecommendationRest> result = outAiRecoService.getAllRecommendations(digitalServiceUid);
        return ResponseEntity.ok(result);
    }

}
