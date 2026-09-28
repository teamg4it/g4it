package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutAiService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;

@Mapper(componentModel = "spring")
public interface AiServiceImpactToCsvRecord {

    default List<String> toCsv(final Context context, final Long taskId, final String inventoryName,
                               final InAiService inAiService, final OutAiService outAiService) {
        final LocalDateTime now = context.getDatetime();

        return List.of(
                inventoryName,
                now.format(Constants.LOCAL_DATE_TIME_FORMATTER_MS),
                now.toLocalDate().toString(),
                taskId.toString(),
                outAiService.getLifecycleStep(),
                CriteriaUtils.transformCriteriaKeyToCriteriaName(StringUtils.snakeToKebabCase(outAiService.getCriterion())),
                outAiService.getProvider(),
                outAiService.getModel(),
                print(outAiService.getLocation()),
                inAiService.getServiceName(),
                outAiService.getStatusIndicator(),
                print(outAiService.getUnitImpact()),
                print(outAiService.getUnit()),
                print(outAiService.getPeopleEqImpact()),
                outAiService.getErrors() == null ? "" : String.join(", ", outAiService.getErrors())
        );
    }
}