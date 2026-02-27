/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentElecConsumptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InVirtualEquipmentElecConsumptionViewRepository
        extends JpaRepository<InVirtualEquipmentElecConsumptionView, Long> {

    @Query(nativeQuery = true)
    List<InVirtualEquipmentElecConsumptionView>
    findVirtualEquipmentElecConsumptionIndicators(
            @Param("taskId") Long taskId);
}

