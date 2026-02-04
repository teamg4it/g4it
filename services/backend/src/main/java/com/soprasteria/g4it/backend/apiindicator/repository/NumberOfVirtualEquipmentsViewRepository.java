/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.NumberOfVirtualEquipmentsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberOfVirtualEquipmentsViewRepository
        extends JpaRepository<NumberOfVirtualEquipmentsView, Long> {

    @Query(
            name = "NumberOfVirtualEquipmentsView.findByInventoryId",
            nativeQuery = true
    )
    NumberOfVirtualEquipmentsView findByInventoryId(Long inventoryId);
}

