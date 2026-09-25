/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class EvaluateReportBO {

    private boolean export;
    private boolean verbose;
    private boolean isDigitalService;
    private int nbPhysicalEquipmentLines;
    private int nbVirtualEquipmentLines;
    private int nbApplicationLines;
    private int nbAiServiceLines;
    private Long taskId;
    private String name;
}
