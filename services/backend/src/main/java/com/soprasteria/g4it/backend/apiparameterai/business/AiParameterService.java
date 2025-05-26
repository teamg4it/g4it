package com.soprasteria.g4it.backend.apiparameterai.business;


import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiparameterai.mapper.AiParameterMapper;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.AiParameterRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class AiParameterService {
    @Autowired
    private  AiParameterRepository aiParameterRepository;
    @Autowired
    private  AiParameterMapper aiParameterMapper;
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;
    public AiParameterRest createAiParameter(final String digitalServiceUid,AiParameterRest aiParameterRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }
        final AiParameter entityToSave = aiParameterMapper.toEntity(aiParameterRest);
        final LocalDateTime now = LocalDateTime.now();
        entityToSave.setDigitalServiceUid(digitalServiceUid);
        entityToSave.setCreationDate(now);
        entityToSave.setLastUpdateDate(now);
        return aiParameterMapper.toBusinessObject(aiParameterRepository.save(entityToSave));
    }

}

