/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiversion.business;


import com.soprasteria.g4it.backend.server.gen.api.dto.VersionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Version Service
 */
@Service
@Slf4j
public class VersionService {

    @Value("${version}")
    private String version;

    @Value("${boaviztapi.version}")
    private String boaviztaVersion;
    @Value("${ecomindaimodule.version}")
    private String ecomindai;

    /**
     * Get the NumEcoEval version
     *
     * @return the numEcoEval version
     */
    @Cacheable("getVersion")
    public VersionRest getVersion() {
        return VersionRest.builder()
                .boaviztapi(boaviztaVersion)
                .g4it(version)
                .ecomindai(ecomindai)
                .build();
    }
}
