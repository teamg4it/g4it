package com.soprasteria.g4it.backend.apiadministratoractions.controller;

import com.soprasteria.g4it.backend.apiadministratoractions.business.AdministratorActionsService;
import com.soprasteria.g4it.backend.apiadministratoractions.business.DsMigrationService;
import com.soprasteria.g4it.backend.server.gen.api.AdministratorActionsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AllEvaluationStatusRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdministratorActionsRestController implements AdministratorActionsApiDelegate {
    @Autowired
    AdministratorActionsService administratorActionsService;
    @Autowired
    DsMigrationService dsMigrationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AllEvaluationStatusRest> doAdminActions() {

        // Migration of digital services from DEMO workspace to new workspaces
        return ResponseEntity.ok(dsMigrationService.migrateDemoDs());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AllEvaluationStatusRest> removeWriteAccess() {

      //  Remove write access for digital services on demo organizations
        return ResponseEntity.ok(administratorActionsService.executeWriteRoleCleanupOnDemoOrg());
    }
}