/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * The database cache configuration.
 */
@Slf4j
@Configuration
@Profile("!test")
public class DatabaseCacheConfiguration {

    /**
     * Clear all cache configuration.
     */
    @CacheEvict(value = {
            "Workspace",
            "getBusinessHours",
            "ref_getAllLifecycleSteps",
            "ref_getAllCriteria",
            "ref_getHypotheses",
            "ref_getItemTypes",
            "ref_getMatchingItem",
            "ref_getItemImpacts",
            "ref_getCountries",
            "boaviztaGetCountryMap",
            "boaviztaGetProviderList",
            "boaviztaGetInstanceList",
            "getNewElectricityMixQuartiles",
    }, allEntries = true)
    @Scheduled(fixedRateString = "${g4it.cache.database.ttl}")
    public void emptyDatabaseCaches() {
        // evict caches, code executed from Annotations
    }

    /**
     * Clear all cache configuration with short ttl.
     */
    @CacheEvict(value = {
            "getVersion",
            "getJwtToken",
            "existsByUidAndUserId",
            "RoleRepository_findByName",
            "getUserByName",
            "listTemplatesFiles",
            "getElectricityMixQuartiles",
            "getCountryList",
            "isLowImpact",
            "inventoryExists"
    }, allEntries = true)
    @Scheduled(fixedRateString = "${g4it.cache.short.ttl}")
    public void emptyShortTtlCaches() {
        // evict caches, code executed from Annotations
    }
}
