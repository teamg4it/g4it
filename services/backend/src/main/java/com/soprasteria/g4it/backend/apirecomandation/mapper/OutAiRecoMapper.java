package com.soprasteria.g4it.backend.apirecomandation.mapper;

import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OutAiRecoMapper {

    OutAiRecommendationRest toDto(OutAiReco entity);

    OutAiReco toEntity(OutAiRecommendationRest dto);

    List<OutAiRecommendationRest> toDtoList(List<OutAiReco> entities);
}