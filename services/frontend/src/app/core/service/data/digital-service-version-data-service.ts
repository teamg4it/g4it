import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import {
    CompareVersion,
    DigitalServicePromoteResponse,
    DigitalServiceVersionResponse,
} from "../../interfaces/digital-service-version.interface";
import { DigitalService } from "../../interfaces/digital-service.interfaces";

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

    duplicateVersion(dsvUid: string): Observable<DigitalService> {
        return this.http.post<DigitalService>(`${endpoint}/${dsvUid}/duplicate`, null);
    }

    compareVersions(v1Uid: string, v2Uid: string): Observable<CompareVersion[]> {
        const params = new HttpParams()
            .set("digitalServiceVersionUid1", v1Uid)
            .set("digitalServiceVersionUid2", v2Uid);

        return this.http.get<CompareVersion[]>(`${endpoint}/compare-versions`, {
            params,
        });
    }

    promoteVersion(dsvUid: string): Observable<DigitalServicePromoteResponse> {
        return this.http.get<DigitalServicePromoteResponse>(
            `${endpoint}/${dsvUid}/promote-version`,
        );
    }
}
