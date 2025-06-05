package com.soprasteria.g4it.backend.apirecomandation.business;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import java.util.List;

@Service
public class OutAiRecoService {

    @Autowired
    private OutAiRecoRepository outAiRecoRepository;

    @Autowired
    private OutAiRecoMapper outAiRecoMapper;

    /**
     * Retrieve all AI recommendations
     */
    public List<OutAiRecommendationRest> getAllRecommendations() {
        List<OutAiReco> entities = outAiRecoRepository.findAll();
        return outAiRecoMapper.toDtoList(entities);
    }
}
