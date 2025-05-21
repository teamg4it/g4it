package com.soprasteria.g4it.backend.apiparameterai.business;


import com.soprasteria.g4it.backend.apiparameterai.mapper.AiParameterMapper;
import com.soprasteria.g4it.backend.apiparameterai.model.AiParameterBO;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import com.soprasteria.g4it.backend.apiparameterai.repository.AiParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AiParameterService {
    @Autowired
    private  AiParameterRepository aiParameterRepository;
    @Autowired
    private  AiParameterMapper aiParameterMapper;

    public AiParameterBO createAiParameter(AiParameterBO aiParameterBO) {

        final AiParameter entityToSave = aiParameterMapper.toEntity(aiParameterBO);

        final AiParameter savedEntity = aiParameterRepository.save(entityToSave);

        return aiParameterMapper.toBusinessObject(savedEntity);
    }

}