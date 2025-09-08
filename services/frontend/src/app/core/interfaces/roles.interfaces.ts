/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
export enum Role {
    InventoryRead = "ROLE_INVENTORY_READ",
    DigitalServiceRead = "ROLE_DIGITAL_SERVICE_READ",
    InventoryWrite = "ROLE_INVENTORY_WRITE",
    DigitalServiceWrite = "ROLE_DIGITAL_SERVICE_WRITE",
    OrganizationAdmin = "ROLE_ORGANIZATION_ADMINISTRATOR ",
    WorkspaceAdmin = "ROLE_WORKSPACE_ADMINISTRATOR",
    EcoMindAiRead = "ROLE_ECO_MIND_AI_READ",
    EcoMindAiWrite = "ROLE_ECO_MIND_AI_WRITE",
}

export const BasicRoles = [
    Role.InventoryRead,
    Role.InventoryWrite,
    Role.DigitalServiceRead,
    Role.DigitalServiceWrite,
    Role.EcoMindAiRead,
    Role.EcoMindAiWrite,
];

export const RoleRightMap: any = {
    ROLE_INVENTORY_READ: "read",
    ROLE_DIGITAL_SERVICE_READ: "read",
    ROLE_INVENTORY_WRITE: "write",
    ROLE_DIGITAL_SERVICE_WRITE: "write",
    ROLE_ORGANIZATION_ADMINISTRATOR: "admin",
    ROLE_SUBSCRIBER_ADMINISTRATOR: "admin",
    ROLE_ECO_MIND_AI_READ: "read",
    ROLE_ECO_MIND_AI_WRITE: "write",
};

export interface RoleValue {
    value?: string;
    code: Role;
}
