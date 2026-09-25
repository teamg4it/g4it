package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import org.mapstruct.Mapper;

import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;

@Mapper(componentModel = "spring")
public interface AiServiceToCsvRecord {

    default List<String> toCsv(final InAiService aiService) {
        return List.of(
                aiService.getServiceName(),
                aiService.getProvider(),
                aiService.getModel(),
                aiService.getOutputTokens().toString(),
                print(aiService.getLocation())
        );
    }
}