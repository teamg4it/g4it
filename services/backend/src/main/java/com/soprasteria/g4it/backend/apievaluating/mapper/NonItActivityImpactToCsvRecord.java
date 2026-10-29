package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.InNonItActivity;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutNonItActivity;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;

@Mapper(componentModel = "spring")
public interface NonItActivityImpactToCsvRecord {

    default List<String> toCsv(final Context context, final Long taskId, final String inventoryName,
                               final InNonItActivity inNonItActivity, final OutNonItActivity outNonItActivity) {
        final LocalDateTime now = context.getDatetime();

        return List.of(
                inventoryName,
                now.format(Constants.LOCAL_DATE_TIME_FORMATTER_MS),
                now.toLocalDate().toString(),
                taskId.toString(),
                CriteriaUtils.transformCriteriaKeyToCriteriaName(StringUtils.snakeToKebabCase(outNonItActivity.getCriterion())),
                print(outNonItActivity.getCategory()),
                print(outNonItActivity.getSubcategory()),
                print(outNonItActivity.getReference()),
                inNonItActivity.getActivityName(),
                print(inNonItActivity.getEntity()),
                print(inNonItActivity.getSource()),
                outNonItActivity.getStatusIndicator(),
                outNonItActivity.getQuantity() == null ? "" : outNonItActivity.getQuantity().toString(),
                print(outNonItActivity.getUnitImpact()),
                print(outNonItActivity.getUnit()),
                print(outNonItActivity.getPeopleEqImpact()),
                outNonItActivity.getErrors() == null ? "" : String.join(", ", outNonItActivity.getErrors())
        );
    }
}
