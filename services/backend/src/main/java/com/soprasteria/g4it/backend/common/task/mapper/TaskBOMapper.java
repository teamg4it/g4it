/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.mapper;


import com.soprasteria.g4it.backend.common.task.model.TaskBO;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskBOMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "progressPercentage", source = "progressPercentage")
    @Mapping(target = "details", source = "details")
    @Mapping(target = "errors", source = "errors")
    @Mapping(target = "criteria", source = "criteria")
    @Mapping(target = "creationDate", source = "creationDate")
    @Mapping(target = "lastUpdateDate", source = "lastUpdateDate")
    TaskBO toBO(Task task);

    List<TaskBO> toBOList(List<Task> tasks);
}
