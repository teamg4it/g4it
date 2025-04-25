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
    OrganizationCriteriaRest,
    OrganizationUpsertRest,
    Subscriber,
    SubscriberCriteriaRest,
} from "../../interfaces/administration.interfaces";

const endpoint = Constants.ENDPOINTS.subscribers;
const endpointForOrg = Constants.ENDPOINTS.organizations;
const endpointForUser = Constants.ENDPOINTS.users;

@Injectable({
    providedIn: "root",
})
export class AdministrationDataService {
    constructor(private http: HttpClient) {}

    getOrganizations(): Observable<Subscriber[]> {
        return this.http.get<Subscriber[]>(`${endpoint}`);
    }

    getUsers(): Observable<Subscriber> {
        return this.http.get<Subscriber>(`${endpointForOrg}`);
    }

    postOrganization(body: OrganizationUpsertRest): Observable<OrganizationUpsertRest> {
        return this.http.post<OrganizationUpsertRest>(`${endpointForOrg}`, body);
    }

    updateOrganization(
        organizationId: number,
        body: OrganizationUpsertRest,
    ): Observable<OrganizationUpsertRest> {
        return this.http.put<OrganizationUpsertRest>(
            `${endpointForOrg}?organizationId=${organizationId}`,
            body,
        );
    }

    updateOrganizationCriteria(
        organizationId: number,
        body: OrganizationCriteriaRest,
    ): Observable<OrganizationCriteriaRest> {
        return this.http.put<OrganizationCriteriaRest>(
            `${endpointForOrg}?organizationId=${organizationId}`,
            body,
        );
    }

    updateSubscriberCriteria(
        subscriberId: number,
        criteria: SubscriberCriteriaRest,
    ): Observable<SubscriberCriteriaRest> {
        return this.http.put<SubscriberCriteriaRest>(
            `${endpoint}?subscriberId=${subscriberId}`,
            criteria,
        );
    }

    getUserDetails(organizationId: number): Observable<any> {
        return this.http.get<any>(
            `${endpointForOrg}/users?organizationId=${organizationId}`,
        );
    }

    getSearchDetails(
        searchName: string,
        subscriberId: number,
        organizationId: number,
    ): Observable<any> {
        return this.http.get<any>(
            `${endpoint}/${endpointForUser}?searchedName=${searchName}&subscriberId=${subscriberId}&organizationId=${organizationId}`,
        );
    }

    postUserToOrganizationAndAddRoles(body: any): Observable<any> {
        return this.http.post<any>(`${endpointForOrg}/users`, body);
    }

    deleteUserDetails(body: any): Observable<any> {
        return this.http.delete<any>(`${endpointForOrg}/users`, {
            body,
        });
    }
}
