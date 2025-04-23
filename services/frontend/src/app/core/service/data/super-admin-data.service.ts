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
const endpoint = Constants.ENDPOINTS.actions;

@Injectable({
    providedIn: "root",
})
export class SuperAdminDataService {
    constructor(private http: HttpClient) {}

    migrateDataToNewFormat(): Observable<void> {
        return this.http.post<void>(`${endpoint}/migrate-data-to-new-format`, {});
    }
}
