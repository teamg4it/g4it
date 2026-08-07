/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "g4it.file.equipment-limit")
@Data
public class FileEquipmentLimitConfiguration {
    
    private Integer application;
    private Integer physicalEquipment;
    private Integer virtualEquipment;
    private Integer datacenter;
    
}
