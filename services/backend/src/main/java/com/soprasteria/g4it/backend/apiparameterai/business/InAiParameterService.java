package com.soprasteria.g4it.backend.apiparameterai.business;


import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiparameterai.mapper.InAiParameterMapper;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.InAiParameterRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class InAiParameterService {
    @Autowired
    private InAiParameterRepository inAiParameterRepository;
    @Autowired
    private InAiParameterMapper inAiParameterMapper;
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;

    public AiParameterRest createAiParameter(final String digitalServiceUid,AiParameterRest aiParameterRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }

        final InAiParameter entityToSave = inAiParameterMapper.toEntity(aiParameterRest);
        final LocalDateTime now = LocalDateTime.now();
        entityToSave.setDigitalServiceUid(digitalServiceUid);
        entityToSave.setCreationDate(now);
        entityToSave.setLastUpdateDate(now);
        return inAiParameterMapper.toBusinessObject(inAiParameterRepository.save(entityToSave));
    }

    public AiParameterRest getAiParameter(final String digitalServiceUid) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }

        return inAiParameterMapper.toBusinessObject(inAiParameterRepository.findByDigitalServiceUid(digitalServiceUid));
    }

    public AiParameterRest updateAiParameter(final String digitalServiceUid, AiParameterRest aiParameterRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }
        InAiParameter inAiParameter = inAiParameterRepository.findByDigitalServiceUid(digitalServiceUid);
        inAiParameterMapper.updateEntityFromDto(aiParameterRest, inAiParameter);
        inAiParameterRepository.save(inAiParameter);
        return inAiParameterMapper.toBusinessObject(inAiParameter);
    }

}

