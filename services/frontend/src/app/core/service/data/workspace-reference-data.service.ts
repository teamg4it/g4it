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

const endpoint = Constants.ENDPOINTS.referential;
const endpointWs = Constants.ENDPOINTS.adminWorkspaces;

export interface CsvImportEndpoint {
    name: string;
    url: string;
    label: string;
}

@Injectable({
    providedIn: "root",
})
export class WorkspaceReferenceDataService {
    constructor(private readonly http: HttpClient) {}

    private readonly csvEndpoints: CsvImportEndpoint[] = [
        {
            name: "itemType",
            url: `${endpoint}/itemType/csv`,
            label: "Item Type",
        },
        {
            name: "itemImpact",
            url: `${endpoint}/itemImpact/csv`,
            label: "Item Impact",
        },
        {
            name: "matchingItem",
            url: `${endpoint}/matchingItem/csv`,
            label: "Matching Item",
        },
    ];

    getWorkspaceCsvEndpoints(): CsvImportEndpoint[] {
        return this.csvEndpoints;
    }

    workspaceUploadCsvFile(
        endpointName: string,
        file: File,
        workspaceId: number,
    ): Observable<any> {
        const endpointType = this.csvEndpoints.find((ep) => ep.name === endpointName);
        if (!endpointType) {
            throw new Error(`Endpoint ${endpointName} not found`);
        }

        const formData = new FormData();
        formData.append("file", file);

        return this.http.post(
            `${endpointWs}/${workspaceId}/${endpointType.url}`,
            formData,
        );
    }

    workspaceDownloadZipFile(workspaceId: number): Observable<Blob> {
        return this.http.get(`${endpointWs}/${workspaceId}/${endpoint}/csv`, {
            responseType: "blob",
        });
    }
}
