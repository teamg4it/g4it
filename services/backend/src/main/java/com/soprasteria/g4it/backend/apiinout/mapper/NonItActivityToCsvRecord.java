package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InNonItActivity;
import org.mapstruct.Mapper;

import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;

@Mapper(componentModel = "spring")
public interface NonItActivityToCsvRecord {

    default List<String> toCsv(final InNonItActivity nonItActivity) {
        return List.of(
                nonItActivity.getActivityName(),
                nonItActivity.getReference(),
                nonItActivity.getQuantity().toString(),
                print(nonItActivity.getEntity()),
                print(nonItActivity.getSource())
        );
    }
}
