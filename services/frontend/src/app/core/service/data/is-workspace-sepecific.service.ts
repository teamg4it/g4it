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

const endpoint = Constants.ENDPOINTS.isWorkspaceSpecific;

@Injectable({
    providedIn: "root",
})
export class IsWorkspaceSpecificService {
    constructor(private readonly http: HttpClient) {}

    getIsWorkspaceSpecific(): Observable<boolean> {
        return this.http.get<boolean>(`${endpoint}`);
    }
}
