/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentLowImpactView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InVirtualEquipmentLowImpactViewRepository
        extends JpaRepository<InVirtualEquipmentLowImpactView, Long> {

    @Query(
            name = "InVirtualEquipmentLowImpactView.findVirtualEquipmentLowImpactIndicatorsByInventoryId",
            nativeQuery = true
    )
    List<InVirtualEquipmentLowImpactView> findByInventoryId(Long inventoryId);
}

