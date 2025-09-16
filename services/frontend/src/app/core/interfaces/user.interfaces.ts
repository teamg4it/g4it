/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Role } from "./roles.interfaces";

export interface User {
    email: string;
    firstName: string;
    lastName: string;
    id: number;
    organizations: Organization[];
    isSuperAdmin: boolean;
}

export interface UserInfo {
    firstName: string;
    lastName: string;
    email: string;
}

export interface Organization {
    id: number;
    name: string;
    defaultFlag: boolean;
    workspaces: Workspace[];
    roles: Role[];
    criteria?: string[];
    authorizedDomains?: string[];
    ecomindai: boolean;
}

export interface Workspace {
    id: number;
    name: string;
    defaultFlag: boolean;
    status: string;
    roles: Role[];
    criteriaIs?: string[];
    criteriaDs?: string[];
    dataRetentionDays?: number | null;
    organizationId?: number;
}

export interface OrganizationData {
    id: number;
    name: string;
    workspace?: Workspace;
    organization?: Organization;
    color: string;
}

export interface UserDetails {
    email: string;
    firstName: string;
    id: number;
    lastName: string;
    roles: Role[];
}
