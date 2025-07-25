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
import { OutApplicationsRest } from "src/app/core/interfaces/output.interface";
import { Constants } from "src/constants";

const endpoint = Constants.ENDPOINTS.inventories;

@Injectable({
    providedIn: "root",
})
export class OutApplicationsService {
    private readonly API = "applications";

    constructor(private readonly http: HttpClient) {}

    get(inventoryId: number): Observable<OutApplicationsRest[]> {
        return this.http.get<OutApplicationsRest[]>(
            `${endpoint}/${inventoryId}/outputs/${this.API}`,
        );
    }
}
