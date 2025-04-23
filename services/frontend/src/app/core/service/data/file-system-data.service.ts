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

const endpoint = Constants.ENDPOINTS.inventories;

@Injectable({
    providedIn: "root",
})
export class FileSystemDataService {
    constructor(private http: HttpClient) {}

    postFileSystemUploadFile(inventoryId: number, formData: FormData): Observable<any> {
        return this.http.post(`${endpoint}/${inventoryId}/files`, formData);
    }

    downloadResultsFile(inventoryId: number, batchName: string): Observable<any> {
        return this.http.get(`${endpoint}/${inventoryId}/output/${batchName}`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }
}
