/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Injectable, inject, signal } from "@angular/core";
import { Observable, map } from "rxjs";
import { Constants } from "src/constants";
import {
    Organization,
    OrganizationCriteriaRest,
    WorkspaceCriteriaRest,
    WorkspaceUpsertRest,
    WorkspaceWithOrganization,
} from "../../interfaces/administration.interfaces";
import { Role } from "../../interfaces/roles.interfaces";
import { AdministrationDataService } from "../data/administration-data-service";
import { UserService } from "./user.service";

@Injectable({
    providedIn: "root",
})
export class AdministrationService {
    private readonly userService = inject(UserService);

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

    /**
     * Gets list of workspaces where the current user is an admin (Organization or Workspace Admin)
     * Returns an observable of WorkspaceWithOrganization array with filtered active workspaces
     */
    getAdminWorkspaceList(): Observable<WorkspaceWithOrganization[]> {
        return this.getUsers().pipe(
            map((organizationsDetails: any) => {
                const list: WorkspaceWithOrganization[] = [];
                for (const organization of organizationsDetails) {
                    for (const workspace of organization.workspaces) {
                        const roles = this.userService.getRoles(organization, workspace);
                        if (
                            workspace.status === Constants.WORKSPACE_STATUSES.ACTIVE &&
                            (roles.includes(Role.OrganizationAdmin) ||
                                roles.includes(Role.WorkspaceAdmin))
                        ) {
                            list.push({
                                organizationName: organization.name,
                                organizationId: organization.id,
                                workspaceName: workspace.name,
                                workspaceId: workspace.id,
                                status: workspace.status,
                                dataRetentionDays: workspace.dataRetentionDays!,
                                displayLabel: `${workspace.name} - (${organization.name})`,
                                criteriaDs: workspace.criteriaDs!,
                                criteriaIs: workspace.criteriaIs!,
                                authorizedDomains: organization.authorizedDomains,
                            });
                        }
                    }
                }
                return list;
            })
        );
    }

    refreshGetUsers() {
        this.getUsersTriggered.set(true);
        setTimeout(() => {
            this.getUsersTriggered.set(false);
        });
    }
}
