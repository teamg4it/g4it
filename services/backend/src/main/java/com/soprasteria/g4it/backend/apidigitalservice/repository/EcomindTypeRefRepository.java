package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.EcomindTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Device Type Referential repository.
 */
@Repository
public interface EcomindTypeRefRepository extends JpaRepository<EcomindTypeRef, Long> {
    /**
     * Find ref by code.
     *
     * @param code the ecomind type code.
     * @return the device type.
     */
    Optional<EcomindTypeRef> findByReference(final String code);
}
