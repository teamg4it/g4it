package com.soprasteria.g4it.backend.apirecomandation.business;

import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutAiRecoService {

    @Autowired
    private OutAiRecoRepository outAiRecoRepository;

    @Autowired
    private OutAiRecoMapper outAiRecoMapper;

    /**
     * Retrieve all AI recommendations
     */
    public OutAiRecommendationRest getRecommendations(String digitalServiceUid) {
        OutAiReco outAiReco = outAiRecoRepository.findByDigitalServiceUid(digitalServiceUid);
        return outAiRecoMapper.toDto(outAiReco);
    }
}
