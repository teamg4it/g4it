import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { DigitalServiceVersionResponse } from "../../interfaces/digital-service-version.interface";

const endpoint = Constants.ENDPOINTS.digitalServicesVersions;

@Injectable({
    providedIn: "root",
})
export class DigitalServiceVersionDataService {
    constructor(private readonly http: HttpClient) {}

    getVersions(dsvUid: string): Observable<DigitalServiceVersionResponse[]> {
        return this.http.get<DigitalServiceVersionResponse[]>(
            `${endpoint}/${dsvUid}/manage-versions`,
        );
    }
}
