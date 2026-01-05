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
import { BusinessHours } from "../../interfaces/business-hours.interface";
import { VersionRest } from "../../interfaces/version.interfaces";

const endpoint = Constants.ENDPOINTS.sharedDs;
@Injectable({
    providedIn: "root",
})
export class ShareUsefulInformationDataService {
    constructor(private readonly http: HttpClient) {}

    getVersion(sharedToken: string): Observable<VersionRest> {
        return this.http.get<VersionRest>(
            `${endpoint}/${sharedToken}/${Constants.ENDPOINTS.version}`,
        );
    }

    getBusinessHours(sharedToken: string): Observable<BusinessHours[]> {
        return this.http.get<BusinessHours[]>(
            `${endpoint}/${sharedToken}/${Constants.ENDPOINTS.businessHours}`,
        );
    }
}
