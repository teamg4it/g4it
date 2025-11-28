import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, Observable, of, switchMap, throwError } from "rxjs";
import { Constants } from "src/constants";
import {
    DigitalServiceParameterIa,
    DigitalServicesAiInfrastructure,
    EcomindType,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";

const endpointDs = Constants.ENDPOINTS.digitalServices;
const endpoint = Constants.ENDPOINTS.digitalServicesVersions;

@Injectable({
    providedIn: "root",
})
export class DigitalServicesAiDataService {
    private readonly HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });

    constructor(private readonly http: HttpClient) {}

    saveAiInfrastructure(
        digitalServiceUid: string,
        infrastructureData: any,
    ): Observable<DigitalServicesAiInfrastructure> {
        return this.getAiInfrastructure(digitalServiceUid).pipe(
            catchError((error) => {
                if (error.status === 404) {
                    // The infrastructure doesn't exist, so we make a POST
                    return of(null);
                }
                return throwError(() => error);
            }),
            switchMap((existingInfra) => {
                if (existingInfra) {
                    // The infra exists, we make a PUT
                    return this.http.put<DigitalServicesAiInfrastructure>(
                        `${endpoint}/${digitalServiceUid}/ai-infra-input`,
                        infrastructureData,
                        { headers: this.HEADERS },
                    );
                } else {
                    return this.http.post<DigitalServicesAiInfrastructure>(
                        `${endpoint}/${digitalServiceUid}/ai-infra-input`,
                        infrastructureData,
                        { headers: this.HEADERS },
                    );
                }
            }),
        );
    }

    saveAiParameters(
        digitalServiceUid: string,
        parametersData: any,
    ): Observable<DigitalServiceParameterIa> {
        return this.getAiParameter(digitalServiceUid).pipe(
            catchError((error) => {
                if (error.status === 404) {
                    // The parameter doesn't exist, so we make a POST
                    return of(null);
                }
                return throwError(() => error);
            }),
            switchMap((existingParams) => {
                if (existingParams) {
                    // The parameter exists, we make a PUT
                    return this.http.put<DigitalServiceParameterIa>(
                        `${endpoint}/${digitalServiceUid}/ai-parameter-input`,
                        parametersData,
                        { headers: this.HEADERS },
                    );
                } else {
                    return this.http.post<DigitalServiceParameterIa>(
                        `${endpoint}/${digitalServiceUid}/ai-parameter-input`,
                        parametersData,
                        { headers: this.HEADERS },
                    );
                }
            }),
        );
    }

    getBoaviztapiCountryMap(): Observable<MapString> {
        return this.http.get<MapString>(`referential/boaviztapi/countries`);
    }

    getAiRecommendations(digitalServiceUid: string): Observable<any> {
        return this.http.get<any>(
            `${endpoint}/${digitalServiceUid}/outputs/ai-recomandation`,
        );
    }

    getAiInfrastructure(digitalServiceUid: string): Observable<any> {
        return this.http.get<any>(`${endpoint}/${digitalServiceUid}/ai-infra-input`);
    }

    getAiParameter(digitalServiceUid: string): Observable<any> {
        return this.http.get<any>(`${endpoint}/${digitalServiceUid}/ai-parameter-input`);
    }

    getEcomindReferential(): Observable<EcomindType[]> {
        return this.http.get<EcomindType[]>(`${endpointDs}/ecomind-type`);
    }
}
