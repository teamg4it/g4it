/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.controller;

import com.soprasteria.g4it.backend.apireferential.business.WorkspaceReferentialExportService;
import com.soprasteria.g4it.backend.apireferential.business.WorkspaceReferentialImportService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.auditevent.business.AuditEventService;
import com.soprasteria.g4it.backend.auditevent.model.AuditContext;
import com.soprasteria.g4it.backend.auditevent.model.AuditEventType;
import com.soprasteria.g4it.backend.auditevent.modeldb.AuditEvent;
import com.soprasteria.g4it.backend.auditevent.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.WorkspaceReferentialImportExportApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.ImportReportRest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class WorkspaceReferentialImportExportController implements WorkspaceReferentialImportExportApiDelegate {

    @Autowired
    private WorkspaceReferentialExportService workspaceReferentialExportService;

    @Autowired
    private WorkspaceReferentialImportService workspaceReferentialImportService;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private AuthService authService;

    @Autowired
    private HttpServletRequest request;

    /**
     * {@inheritDoc}
     */

    @Override
    public ResponseEntity<Resource> exportWorkspaceReferentialZip(String organization, Long workspace) {

        UserBO user = authService.getUser();

        Resource resource = auditEventService.execute(
                AuditContext.builder()
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .organization(organization)
                        .workspaceId(workspace)
                        .action(AuditEventType.EXPORT_WORKSPACE_REFERENTIAL)
                        .endpoint(Constants.GET_REFERENTIAL_WORKSPACE_ZIP_FILES)
                        .build(),
                () -> {
                    InputStream zipStream = workspaceReferentialExportService
                            .exportReferentialZip(organization, workspace);

                    return new InputStreamResource(zipStream);
                }
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=workspace-referential.zip")
                .header("Content-Type", "application/zip")
                .body(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ImportReportRest> importWorkspaceReferentialCSV(
            String organization,
            Long workspace,
            String type,
            MultipartFile file
    ) {

        UserBO user = authService.getUser();

        ImportReportRest report = auditEventService.execute(
                AuditContext.builder()
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .organization(organization)
                        .workspaceId(workspace)
                        .action(AuditEventType.IMPORT_WORKSPACE_REFERENTIAL)
                        .endpoint(resolveEndpoint(type))
                        .build(),
                () -> workspaceReferentialImportService.importReferentialCSV(
                        organization, workspace, type, file
                )
        );

        return ResponseEntity.ok(report);
    }


    private String resolveEndpoint(String type) {
        return switch (type) {
            case "itemType" -> Constants.POST_REFERENTIAL_WORKSPACE_ITEM_TYPE;
            case "itemImpact" -> Constants.POST_REFERENTIAL_WORKSPACE_ITEM_IMPACT;
            case "matchingItem" -> Constants.POST_REFERENTIAL_WORKSPACE_MATCHING_ITEMS;
            default -> "unknown";
        };
    }
}