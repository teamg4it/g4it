/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";

import { Observable } from "rxjs";
import { Constants } from "src/constants";

const endpoint = Constants.ENDPOINTS.downloadReject;

@Injectable({
    providedIn: "root",
})
export class FileSystemDataService {
    private readonly http = inject(HttpClient);

    downloadResultsFile(taskId: string): Observable<any> {
        return this.http.get(`${endpoint}/${taskId}`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }
}
