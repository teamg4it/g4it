/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.model;

import com.soprasteria.g4it.backend.common.model.NoteBO;
import com.soprasteria.g4it.backend.common.task.model.TaskBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventoryBO {

    private Long id;

    private String name;

    private String type;

    private LocalDateTime creationDate;

    private LocalDateTime lastUpdateDate;

    private String workspace;

    private Long workspaceId;

    private String workspaceStatus;

    private Long dataCenterCount;

    private Long physicalEquipmentCount;

    private Long virtualEquipmentCount;

    private Long applicationCount;

    private List<String> criteria;

    private List<TaskBO> tasks;

    private NoteBO note;

}
