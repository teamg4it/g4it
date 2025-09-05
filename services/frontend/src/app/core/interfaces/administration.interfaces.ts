import { Role } from "./roles.interfaces";

export interface Organization {
    name: string;
    defaultFlag: boolean;
    id: number;
    workspaces: Workspace[];
    roles: Role[];
    criteria?: string[];
    authorizedDomains?: string[];
    ecomindai: boolean;
}

export interface Workspace {
    id: number;
    name: string;
    status: string;
    deletionDate: string;
    dataRetentionDay: number | null;
    defaultFlag: boolean;
    roles: Role[];
    uiStatus?: string;
    criteriaIs?: string[];
    criteriaDs?: string[];
}

export interface WorkspaceUpsertRest {
    organizationId: number;
    name: string;
    status: string | null;
    dataRetentionDay?: number | null;
}

export interface WorkspaceWithOrganization {
    organizationName: string;
    organizationId: number;
    workspaceName: string;
    workspaceId: number;
    status: string;
    dataRetentionDays: number;
    displayLabel: string;
    criteriaDs: string[];
    criteriaIs: string[];
    authorizedDomains?: string[];
}

export interface OrganizationCriteriaRest {
    criteria: string[];
}

export interface WorkspaceCriteriaRest {
    organizationId: number;
    name: string;
    status: string;
    dataRetentionDays: number;
    criteriaIs: string[];
    criteriaDs: string[];
}

export interface WorkspaceOrganization {
    id: number;
    name: string;
    status: string;
}

export interface DomainOrganizations {
    id: number;
    name: string;
    workspaces: WorkspaceOrganization[];
}

export interface WorkspaceNameObj {
    id: number;
    name: string;
    status: string;
    defaultFlag: boolean;
}
