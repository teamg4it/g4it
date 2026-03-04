/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.mapper;

import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.common.task.model.TaskBO;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryCreateRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Inventory mapper.
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    /**
     * Map to business object.
     *
     * @param source entity.
     * @return the business object.
     */
    @Mapping(target = "workspace", source = "workspace.name")
    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "workspaceStatus", source = "workspace.status")
    @Mapping(target = "enableDataInconsistency", source = "enableDataInconsistency")
    InventoryBO toBusinessObject(final Inventory source);

    /**
     * Map to business object list.
     *
     * @param source entity list.
     * @return the business object list.
     */
    List<InventoryBO> toBusinessObject(final List<Inventory> source);

    /**
     * Map to entity.
     *
     * @param workspace       the organization.
     * @param inventoryCreate the inventoryCreate rest object.
     * @return entity.
     */
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "createdBy", source = "user")
    @Mapping(target = "name", source = "inventoryCreate.name")
    @Mapping(target = "type", expression = "java(inventoryCreate.getType().name())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "isMigrated", ignore = true)
    Inventory toEntity(final Workspace workspace, final InventoryCreateRest inventoryCreate, final User user);

    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Inventory toEntity(InventoryBO inventoryBO);

    /**
     * Map to business object.
     *
     * @param source entity list.
     * @return the business object.
     */
    @Named("createInventoryMapping")
    @Mapping(target = "workspace", source = "workspace.name")
    @Mapping(target = "dataCenterCount", ignore = true)
    @Mapping(target = "physicalEquipmentCount", ignore = true)
    @Mapping(target = "virtualEquipmentCount", ignore = true)
    @Mapping(target = "applicationCount", ignore = true)
    InventoryBO toCreateBusinessObject(final Inventory source);

    TaskBO toBusinessObject(Task task);
}
