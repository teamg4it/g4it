/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.VirtualEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VirtualEquipmentIndicatorService {

    private final InVirtualEquipmentLowImpactViewRepository repository;
    private final VirtualEquipmentIndicatorMapper mapper;
    private final InventoryService inventoryService;

    public List<VirtualEquipmentLowImpactBO> getVirtualEquipmentsLowImpact(
            final String organization,
            final Long workspaceId,
            final Long inventoryId
    ) {
        InventoryBO inventory =
                inventoryService.getInventory(organization, workspaceId, inventoryId);

        List<VirtualEquipmentLowImpactBO> bos =
                mapper.toLowImpactBO(repository.findByInventoryId(inventoryId));

        return bos;
    }
}

