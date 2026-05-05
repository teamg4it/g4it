/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.auditevent.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditContext {

    private Long userId;
    private String userEmail;

    private String organization;
    private Long workspaceId;
    private AuditEventType action;
    private String endpoint;

}
