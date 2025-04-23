/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.mapper;

import com.soprasteria.g4it.backend.apibatchexport.modeldb.ExportReport;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryExportReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
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
    @Mapping(target = "organization", source = "organization.name")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationStatus", source = "organization.status")
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
     * @param organization    the organization.
     * @param inventoryCreate the inventoryCreate rest object.
     * @return entity.
     */
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "name", source = "inventoryCreate.name")
    @Mapping(target = "type", expression = "java(inventoryCreate.getType().name())")
    @Mapping(target = "isNewArch", source = "inventoryCreate.isNewArch")
    @Mapping(target = "doExport", source = "inventoryCreate.doExport")
    @Mapping(target = "doExportVerbose", source = "inventoryCreate.doExportVerbose")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "isMigrated", ignore = true)
    Inventory toEntity(final Organization organization, final InventoryCreateRest inventoryCreate);

    @Mapping(target = "organization", ignore = true)
    Inventory toEntity(InventoryBO inventoryBO);

    /**
     * Map to business object.
     *
     * @param source entity list.
     * @return the business object.
     */
    @Named("createInventoryMapping")
    @Mapping(target = "organization", source = "organization.name")
    @Mapping(target = "dataCenterCount", ignore = true)
    @Mapping(target = "physicalEquipmentCount", ignore = true)
    @Mapping(target = "virtualEquipmentCount", ignore = true)
    @Mapping(target = "applicationCount", ignore = true)
    @Mapping(target = "integrationReports", ignore = true)
    @Mapping(target = "evaluationReports", ignore = true)
    @Mapping(target = "exportReport", ignore = true)
    InventoryBO toCreateBusinessObject(final Inventory source);

    @Mapping(target = "batchStatusCode", source = "statusCode")
    @Mapping(target = "resultFileSize", source = "exportFileSize")
    @Mapping(target = "resultFileUrl", source = "exportFilename")
    @Mapping(target = "createTime", source = "batchCreateTime")
    @Mapping(target = "endTime", source = "batchEndTime")
    InventoryExportReportBO toBusinessObject(ExportReport exportReport);

    TaskBO toBusinessObject(Task task);
}
