/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.DataCenter;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractValidationBaseEntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataCenterRepository extends AbstractValidationBaseEntityRepository<DataCenter>,
        JpaRepository<DataCenter, Long> {

    /**
     * Find datacenter by the functionally unique fields
     *
     * @param inventoryId        the inventory identifier
     * @param nomCourtDatacenter unique name for the datacenter
     */
    Optional<DataCenter> findByInventoryIdAndNomCourtDatacenter(long inventoryId,
                                                                String nomCourtDatacenter);

    List<DataCenter> findByInventoryId(long inventoryId);

}
