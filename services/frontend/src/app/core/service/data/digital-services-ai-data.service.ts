import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import {
  DigitalServiceParameterIa,
  DigitalServicesAiInfrastructure,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";

const endpoint = Constants.ENDPOINTS.digitalServices;

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
    infrastructureData: any
  ): Observable<DigitalServicesAiInfrastructure> {
    return this.http.post<DigitalServicesAiInfrastructure>(
      `${endpoint}/${digitalServiceUid}/ai-infra-input`,
      infrastructureData,
      { headers: this.HEADERS }
    );
  }

  saveAiParameters(
    digitalServiceUid: string,
    parametersData: any
  ): Observable<DigitalServiceParameterIa> {
    return this.http.post<DigitalServiceParameterIa>(
      `${endpoint}/${digitalServiceUid}/ai-parameter-input`,
      parametersData,
      { headers: this.HEADERS }
    );
  }

  getBoaviztapiCountryMap(): Observable<MapString> {
    return this.http.get<MapString>(`referential/boaviztapi/countries`);
  }
}
