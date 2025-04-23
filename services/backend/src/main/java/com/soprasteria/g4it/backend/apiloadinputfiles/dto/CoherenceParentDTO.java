/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.dto;

public interface CoherenceParentDTO {
    String getEquipmentName();
    String getParentEquipmentName();
    String getFilename();
    Integer getLineNb();
}
