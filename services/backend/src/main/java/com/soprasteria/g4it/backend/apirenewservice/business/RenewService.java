/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apirenewservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.error.ErrorConstants;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.ObjectUtils;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewResponseRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewUpdateRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class RenewService {

    private final InventoryRepository inventoryRepository;
    private final DigitalServiceRepository digitalServiceRepository;
    private final WorkspaceRepository workspaceRepository;
    private final DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Value("${g4it.data.retention.day}")
    private Integer dataRetentiondDay;

    public RenewService(InventoryRepository inventoryRepository,
                        DigitalServiceRepository digitalServiceRepository,
                        WorkspaceRepository workspaceRepository, DigitalServiceVersionRepository digitalServiceVersionRepository) {
        this.inventoryRepository = inventoryRepository;
        this.digitalServiceRepository = digitalServiceRepository;
        this.workspaceRepository = workspaceRepository;
        this.digitalServiceVersionRepository = digitalServiceVersionRepository;
    }

    public RenewRest getRenewDetailsInventory(Long workspace, Long inventoryId) {
        Workspace workspaceEntity = workspaceRepository.findById(workspace).orElseThrow(() ->
                new G4itRestException("400", String.format(ErrorConstants.WORKSPACE_NOT_FOUND, workspace)));
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() ->
                new G4itRestException("400", String.format(ErrorConstants.INVENTORY_NOT_FOUND, inventoryId)));
        final Integer retentionDay = Optional.ofNullable(workspaceEntity.getDataRetentionDay())
                .orElse(Optional.ofNullable(workspaceEntity.getOrganization().getDataRetentionDay())
                        .orElse(dataRetentiondDay));
        String expirationDate = ObjectUtils.getExpiryDate(inventory.getLastUpdateDate(), retentionDay);
        return RenewRest.builder().serviceName(inventory.getName()).serviceId(String.valueOf(inventory.getId()))
                .expiryDate(expirationDate).retentionDays(retentionDay).build();
    }

    public RenewRest getRenewDetailsDigitalService(Long workspace, String digitalServiceVersionUid) {
        Workspace workspaceEntity = workspaceRepository.findById(workspace).orElseThrow(() ->
                new G4itRestException("400", String.format(ErrorConstants.WORKSPACE_NOT_FOUND, workspace)));
        final Integer retentionDay = Optional.ofNullable(workspaceEntity.getDataRetentionDay())
                .orElse(Optional.ofNullable(workspaceEntity.getOrganization().getDataRetentionDay())
                        .orElse(dataRetentiondDay));
        Optional<DigitalServiceVersion> digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid);
        if(digitalServiceVersion.isPresent()) {
            DigitalService digitalService = digitalServiceVersion.get().getDigitalService();
            String expirationDate = ObjectUtils.getExpiryDate(digitalService.getLastUpdateDate(), retentionDay);
            return RenewRest.builder().serviceName(digitalService.getName()).serviceId(digitalService.getUid())
                    .expiryDate(expirationDate).retentionDays(retentionDay).build();
        }else{
            throw new G4itRestException("400", String.format(ErrorConstants.DIGITAL_SERVICE_NOT_FOUND, digitalServiceVersionUid));
        }
    }

    public RenewResponseRest renewDigitalService(Long workspace, RenewUpdateRest renewUpdateRest) {
        if (renewUpdateRest.getAction() != null && renewUpdateRest.getAction().equals("renew")) {
            Workspace workspaceEntity = workspaceRepository.findById(workspace).orElseThrow(() ->
                    new G4itRestException("400", String.format(ErrorConstants.WORKSPACE_NOT_FOUND, workspace)));
            DigitalService digitalService = digitalServiceRepository.findByWorkspaceAndUid(workspaceEntity, renewUpdateRest.getServiceId()).orElseThrow(() ->
                    new G4itRestException("400", String.format(ErrorConstants.DIGITAL_SERVICE_NOT_FOUND, renewUpdateRest.getServiceId())));
            digitalService.setLastUpdateDate(LocalDateTime.now());
            digitalServiceRepository.save(digitalService);
            return RenewResponseRest.builder().serviceId(digitalService.getUid()).isRenewed(true).responseMessage(Constants.RENEWAL_SUCCESS_MESSAGE).build();
        } else {
            throw new G4itRestException("400", ErrorConstants.INVALID_RENEW_ACTION);
        }
    }

    public RenewResponseRest renewInventoryService(Long workspace, Long inventoryId, RenewUpdateRest renewUpdateRest) {
        if (renewUpdateRest.getAction() != null && renewUpdateRest.getAction().equals("renew")) {
            workspaceRepository.findById(workspace).orElseThrow(() ->
                    new G4itRestException("400", String.format(ErrorConstants.WORKSPACE_NOT_FOUND, workspace)));
            Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() ->
                    new G4itRestException("400", String.format(ErrorConstants.INVENTORY_NOT_FOUND, inventoryId)));
            inventory.setLastUpdateDate(LocalDateTime.now());
            inventoryRepository.save(inventory);
            return RenewResponseRest.builder().serviceId(String.valueOf(inventory.getId())).isRenewed(true).responseMessage(Constants.RENEWAL_SUCCESS_MESSAGE).build();
        } else {
            throw new G4itRestException("400", ErrorConstants.INVALID_RENEW_ACTION);
        }
    }

}
