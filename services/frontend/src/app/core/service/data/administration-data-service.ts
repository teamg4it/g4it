/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

import { Constants } from "src/constants";
import {
    Organization,
    OrganizationCriteriaRest,
    WorkspaceCriteriaRest,
    WorkspaceUpsertRest,
} from "../../interfaces/administration.interfaces";

const endpoint = Constants.ENDPOINTS.organizations;
const endpointById = Constants.ENDPOINTS.organizationById;
const endpointForWorkspace = Constants.ENDPOINTS.workspaces;
const endpointForUser = Constants.ENDPOINTS.users;

@Injectable({
    providedIn: "root",
})
export class AdministrationDataService {
    constructor(private readonly http: HttpClient) {}

    getOrganizations(): Observable<Organization[]> {
        return this.http.get<Organization[]>(`${endpoint}`);
    }

    getOrganizationById(organizationId: number): Observable<Organization> {
        return this.http.get<Organization>(
            `${endpointById}?organizationId=${organizationId}`,
        );
    }

    getUsers(): Observable<Organization> {
        return this.http.get<Organization>(`${endpointForWorkspace}`);
    }

    postWorkspace(body: WorkspaceUpsertRest): Observable<WorkspaceUpsertRest> {
        return this.http.post<WorkspaceUpsertRest>(`${endpointForWorkspace}`, body);
    }

    updateWorkspace(
        workspaceId: number,
        body: WorkspaceUpsertRest,
    ): Observable<WorkspaceUpsertRest> {
        return this.http.put<WorkspaceUpsertRest>(
            `${endpointForWorkspace}?workspaceId=${workspaceId}`,
            body,
        );
    }

    updateWorkspaceCriteria(
        workspaceId: number,
        body: WorkspaceCriteriaRest,
    ): Observable<WorkspaceCriteriaRest> {
        return this.http.put<WorkspaceCriteriaRest>(
            `${endpointForWorkspace}?workspaceId=${workspaceId}`,
            body,
        );
    }

    updateOrganizationCriteria(
        organizationId: number,
        criteria: OrganizationCriteriaRest,
    ): Observable<OrganizationCriteriaRest> {
        return this.http.put<OrganizationCriteriaRest>(
            `${endpoint}?organizationId=${organizationId}`,
            criteria,
        );
    }

    getUserDetails(workspaceId: number): Observable<any> {
        return this.http.get<any>(
            `${endpointForWorkspace}/users?workspaceId=${workspaceId}`,
        );
    }

    getSearchDetails(
        searchName: string,
        organizationId: number,
        workspaceId: number,
    ): Observable<any> {
        return this.http.get<any>(
            `${endpoint}/${endpointForUser}?searchedName=${searchName}&organizationId=${organizationId}&workspaceId=${workspaceId}`,
        );
    }

    postUserToWorkspaceAndAddRoles(body: any): Observable<any> {
        return this.http.post<any>(`${endpointForWorkspace}/users`, body);
    }

    deleteUserDetails(body: any): Observable<any> {
        return this.http.delete<any>(`${endpointForWorkspace}/users`, {
            body,
        });
    }
}
