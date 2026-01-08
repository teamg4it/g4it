/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, ReplaySubject, tap } from "rxjs";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { DigitalServiceVersionRequestBody } from "../../interfaces/digital-service-version.interface";
import {
    AiModelConfig,
    DigitalService,
    DSCriteriaRest,
    DuplicateNames,
    Host,
    NetworkType,
    ShareLinkResp,
    TerminalsType,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";

const endpoint = Constants.ENDPOINTS.digitalServices;
const endpointDsVersions = Constants.ENDPOINTS.digitalServicesVersions;
const ecomindaiModelConfig = Constants.ENDPOINTS.ecomindaiModelConfig;

const endpointshared = Constants.ENDPOINTS.sharedDs;
const endpointDs = Constants.ENDPOINTS.dsv;

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

    list(isAi?: boolean): Observable<DigitalService[]> {
        let params = new HttpParams();
        if (isAi !== undefined) {
            params = params.set("isAi", isAi);
        }
        return this.http.get<DigitalService[]>(`${endpoint}`, { params });
    }

    create(requestBody: DigitalServiceVersionRequestBody): Observable<DigitalService> {
        return this.http.post<DigitalService>(`${endpointDsVersions}`, requestBody);
    }

    update(digitalService: DigitalService): Observable<DigitalService> {
        return this.http
            .put<DigitalService>(
                `${endpointDsVersions}/${digitalService.uid}`,
                digitalService,
                {
                    headers: this.HEADERS,
                },
            )
            .pipe(
                tap((res: DigitalService) => {
                    this.digitalServiceSubject.next(res);
                }),
            );
    }

    get(uid: DigitalService["uid"]): Observable<DigitalService> {
        return this.http
            .get<DigitalService>(`${endpointDsVersions}/${uid}`)
            .pipe(tap((res: DigitalService) => this.digitalServiceSubject.next(res)));
    }

    getDsTasks(uid: DigitalService["uid"]): Observable<DigitalService> {
        return this.http.get<DigitalService>(`${endpointDsVersions}/${uid}`);
    }

    delete(uid: DigitalService["uid"]): Observable<string> {
        return this.http.delete<string>(`${endpoint}/${uid}`);
    }

    deleteVersion(uid: DigitalService["uid"]): Observable<string> {
        return this.http.delete<string>(`${endpointDsVersions}/${uid}`);
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

    getModels(model: string): Observable<AiModelConfig[]> {
        return this.http.get<AiModelConfig[]>(`${ecomindaiModelConfig}/${model}`);
    }

    launchEvaluating(uid: DigitalService["uid"]): Observable<string> {
        return this.http.post<string>(`${endpointDsVersions}/${uid}/evaluating`, {});
    }

    downloadFile(uid: DigitalService["uid"]): Observable<any> {
        return this.http.get(`${endpointDsVersions}/${uid}/export`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }

    updateDsCriteria(
        digitalServiceUid: string,
        DSCriteria: DSCriteriaRest,
    ): Observable<DSCriteriaRest> {
        return this.http.put<DSCriteriaRest>(
            `${endpointDsVersions}/${digitalServiceUid}`,
            DSCriteria,
        );
    }

    copyUrl(
        uid: DigitalService["uid"],
        digitalService: DigitalService,
        extendLink: boolean,
    ): Observable<ShareLinkResp> {
        let params = new HttpParams();
        if (extendLink) {
            params = params.set("extendLink", extendLink);
        }
        return this.http
            .get<ShareLinkResp>(`${endpointDsVersions}/${digitalService.uid}/share`, {
                params,
            })
            .pipe(
                map((response) => {
                    return {
                        ...response,
                        url:
                            environment.frontEndUrl +
                            response.url +
                            "/footprint" +
                            (digitalService?.lastCalculationDate ? "/dashboard" : ""),
                    };
                }),
            );
    }

    validateShareToken(id: DigitalService["uid"], token: string): Observable<boolean> {
        return this.http
            .get<boolean>(`${endpointshared}/${token}/${endpointDs}/${id}/validate`)
            .pipe(map((res) => res));
    }

    getDs(dsId: string, token: string): Observable<DigitalService> {
        return this.http
            .get<DigitalService>(`${endpointshared}/${token}/${endpointDs}/${dsId}`)
            .pipe(tap((res: DigitalService) => this.digitalServiceSubject.next(res)));
    }

    getDuplicateDigitalServiceAndVersionName(dsvId: string): Observable<DuplicateNames> {
        return this.http.get<DuplicateNames>(
            `${endpointDsVersions}/${dsvId}/validate-duplicate-names`,
        );
    }
}
