/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Injectable, signal } from "@angular/core";
import { Observable } from "rxjs";
import {
    Organization,
    OrganizationCriteriaRest,
    WorkspaceCriteriaRest,
    WorkspaceUpsertRest,
} from "../../interfaces/administration.interfaces";
import { AdministrationDataService } from "../data/administration-data-service";

@Injectable({
    providedIn: "root",
})
export class AdministrationService {
    constructor(private readonly administrationDataService: AdministrationDataService) {}

    getUsersTriggered = signal<boolean>(false);

    getOrganizations(): Observable<Organization[]> {
        return this.administrationDataService.getOrganizations();
    }

    getOrganizationById(organizationById: number): Observable<Organization> {
        return this.administrationDataService.getOrganizationById(organizationById);
    }

    getUsers(): Observable<Organization> {
        return this.administrationDataService.getUsers();
    }

    updateWorkspace(workspaceId: number, body: WorkspaceUpsertRest): Observable<any> {
        return this.administrationDataService.updateWorkspace(workspaceId, body);
    }

    updateWorkspaceCriteria(
        organizationId: number,
        body: WorkspaceCriteriaRest,
    ): Observable<WorkspaceCriteriaRest> {
        return this.administrationDataService.updateWorkspaceCriteria(
            organizationId,
            body,
        );
    }

    updateOrganizationCriteria(
        organizationId: number,
        criteria: OrganizationCriteriaRest,
    ): Observable<OrganizationCriteriaRest> {
        return this.administrationDataService.updateOrganizationCriteria(
            organizationId,
            criteria,
        );
    }

    getUserDetails(organizationId: number): Observable<any> {
        return this.administrationDataService.getUserDetails(organizationId);
    }

    getSearchDetails(
        searchName: string,
        organizationId: number,
        workspaceId: number,
    ): Observable<any> {
        return this.administrationDataService.getSearchDetails(
            searchName,
            organizationId,
            workspaceId,
        );
    }

    postUserToWorkspaceAndAddRoles(body: any): Observable<any> {
        return this.administrationDataService.postUserToWorkspaceAndAddRoles(body);
    }

    deleteUserDetails(body: any): Observable<any> {
        return this.administrationDataService.deleteUserDetails(body);
    }

    postWorkspace(body: any): Observable<any> {
        return this.administrationDataService.postWorkspace(body);
    }

    refreshGetUsers() {
        this.getUsersTriggered.set(true);
        setTimeout(() => {
            this.getUsersTriggered.set(false);
        });
    }
}
