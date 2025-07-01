/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Device Type Referential repository.
 */
@Repository
public interface DeviceTypeRefRepository extends JpaRepository<DeviceTypeRef, Long> {

    /**
     * Find ref by code.
     *
     * @param code the device type code.
     * @return the device type.
     */
    Optional<DeviceTypeRef> findByReference(final String code);

    List<DeviceTypeRef> findByCompatibleEcomind(final boolean compatibleEcomind);

    /**
     * Find ref by code and compatible_ecomind.
     *
     * @param code the device type code.
     * @return the device type.
     */
    Optional<DeviceTypeRef> findByReferenceAndCompatibleEcomind(final String code, final boolean compatibleEcomind);
}
