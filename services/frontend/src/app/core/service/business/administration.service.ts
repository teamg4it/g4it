/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import {
    OrganizationCriteriaRest,
    OrganizationUpsertRest,
    Subscriber,
    SubscriberCriteriaRest,
} from "../../interfaces/administration.interfaces";
import { AdministrationDataService } from "../data/administration-data-service";

@Injectable({
    providedIn: "root",
})
export class AdministrationService {
    constructor(private administrationDataService: AdministrationDataService) {}

    getOrganizations(): Observable<Subscriber[]> {
        return this.administrationDataService.getOrganizations();
    }

    getUsers(): Observable<Subscriber> {
        return this.administrationDataService.getUsers();
    }

    updateOrganization(
        organizationId: number,
        body: OrganizationUpsertRest,
    ): Observable<any> {
        return this.administrationDataService.updateOrganization(organizationId, body);
    }

    updateOrganizationCriteria(
        organizationId: number,
        body: OrganizationCriteriaRest,
    ): Observable<OrganizationCriteriaRest> {
        return this.administrationDataService.updateOrganizationCriteria(
            organizationId,
            body,
        );
    }

    updateSubscriberCriteria(
        subscriberId: number,
        criteria: SubscriberCriteriaRest,
    ): Observable<SubscriberCriteriaRest> {
        return this.administrationDataService.updateSubscriberCriteria(
            subscriberId,
            criteria,
        );
    }

    getUserDetails(organizationId: number): Observable<any> {
        return this.administrationDataService.getUserDetails(organizationId);
    }

    getSearchDetails(
        searchName: string,
        subscriberId: number,
        organizationId: number,
    ): Observable<any> {
        return this.administrationDataService.getSearchDetails(
            searchName,
            subscriberId,
            organizationId,
        );
    }

    postUserToOrganizationAndAddRoles(body: any): Observable<any> {
        return this.administrationDataService.postUserToOrganizationAndAddRoles(body);
    }

    deleteUserDetails(body: any): Observable<any> {
        return this.administrationDataService.deleteUserDetails(body);
    }

    postOrganization(body: any): Observable<any> {
        return this.administrationDataService.postOrganization(body);
    }
}
