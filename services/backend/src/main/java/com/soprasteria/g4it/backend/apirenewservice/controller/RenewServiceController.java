/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apirenewservice.controller;



import com.soprasteria.g4it.backend.apirenewservice.business.RenewService;
import com.soprasteria.g4it.backend.server.gen.api.RenewApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewResponseRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewUpdateRest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@NoArgsConstructor
public class RenewServiceController implements RenewApiDelegate {

    @Autowired
    private RenewService renewService;

    @Override
    public ResponseEntity<RenewRest> getRenewDetailsInventory(final String organization, final Long workspace, final Long inventoryId) {
        return ResponseEntity.ok().body(renewService.getRenewDetailsInventory(workspace, inventoryId));
    }

    @Override
    public ResponseEntity<RenewRest> getRenewDetailsDigitalService(final String organization,final Long workspace,final String digitalServiceVersionUid) {
        return ResponseEntity.ok().body(renewService.getRenewDetailsDigitalService(workspace, digitalServiceVersionUid));
    }

    @Override
    public ResponseEntity<RenewResponseRest> renewDigitalService(final String organization, final Long workspace, final String digitalServiceVersionUid, final RenewUpdateRest renewUpdateRest) {
        return ResponseEntity.ok().body(renewService.renewDigitalService(workspace,renewUpdateRest));
    }

    @Override
    public ResponseEntity<RenewResponseRest> renewInventoryService(final String organization,final Long workspace,final Long inventoryId,final RenewUpdateRest renewUpdateRest) {
        return ResponseEntity.ok().body(renewService.renewInventoryService(workspace, inventoryId,renewUpdateRest));
    }

}
