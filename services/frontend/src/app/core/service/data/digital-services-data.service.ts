/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, ReplaySubject, tap } from "rxjs";
import { Constants } from "src/constants";
import {
    DigitalService,
    DSCriteriaRest,
    Host,
    NetworkType,
    TerminalsType,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";

const endpoint = Constants.ENDPOINTS.digitalServices;

@Injectable({
    providedIn: "root",
})
export class DigitalServicesDataService {
    private readonly HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });
    constructor(private readonly http: HttpClient) {}
    private readonly digitalServiceSubject = new ReplaySubject<DigitalService>(1);
    digitalService$ = this.digitalServiceSubject.asObservable();

    list(): Observable<DigitalService[]> {
        return this.http.get<DigitalService[]>(`${endpoint}`);
    }

    create(): Observable<DigitalService> {
        return this.http.post<DigitalService>(
            `${endpoint}`,
            {},
            { headers: this.HEADERS },
        );
    }

    update(digitalService: DigitalService): Observable<DigitalService> {
        return this.http
            .put<DigitalService>(`${endpoint}/${digitalService.uid}`, digitalService, {
                headers: this.HEADERS,
            })
            .pipe(
                tap((res: DigitalService) => {
                    this.digitalServiceSubject.next(res);
                }),
            );
    }

    get(uid: DigitalService["uid"]): Observable<DigitalService> {
        return this.http
            .get<DigitalService>(`${endpoint}/${uid}`)
            .pipe(tap((res: DigitalService) => this.digitalServiceSubject.next(res)));
    }

    delete(uid: DigitalService["uid"]): Observable<string> {
        return this.http.delete<string>(`${endpoint}/${uid}`);
    }

    getDeviceReferential(): Observable<TerminalsType[]> {
        return this.http.get<TerminalsType[]>(`${endpoint}/device-type`);
    }

    getCountryReferential(): Observable<string[]> {
        return this.http.get<string[]>(`${endpoint}/country`);
    }

    getNetworkReferential(): Observable<NetworkType[]> {
        return this.http.get<NetworkType[]>(`${endpoint}/network-type`);
    }

    getHostServerReferential(type: string): Observable<Host[]> {
        return this.http.get<Host[]>(`${endpoint}/server-host?type=${type}`);
    }

    getBoaviztapiCountryMap(): Observable<MapString> {
        return this.http.get<MapString>(`referential/boaviztapi/countries`);
    }
    getBoaviztapiCloudProviders(): Observable<string[]> {
        return this.http.get<string[]>(`referential/boaviztapi/cloud/providers`);
    }
    getBoaviztapiInstanceTypes(providerName: string): Observable<string[]> {
        return this.http.get<string[]>(
            `referential/boaviztapi/cloud/providers/instances?provider=${providerName}`,
        );
    }

    launchEvaluating(uid: DigitalService["uid"]): Observable<string> {
        return this.http.post<string>(`${endpoint}/${uid}/evaluating`, {});
    }

    downloadFile(uid: DigitalService["uid"]): Observable<any> {
        return this.http.get(`${endpoint}/${uid}/export`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }

    updateDsCriteria(
        digitalServiceUid: string,
        DSCriteria: DSCriteriaRest,
    ): Observable<DSCriteriaRest> {
        return this.http.put<DSCriteriaRest>(
            `${endpoint}/${digitalServiceUid}`,
            DSCriteria,
        );
    }
}
