import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

import { Constants } from "src/constants";
import { AiModelConfig, DigitalServiceParameterIa } from "../../interfaces/digital-service/parameter.interfaces"; // renommer si besoin

const ecomindaiModelConfig = Constants.ENDPOINTS.ecomindaiModelConfig;

@Injectable({ providedIn: "root" })
export class EcomindaiParameterDataService {
    constructor(private http: HttpClient) {}

    getModels(model:string): Observable<AiModelConfig[]> {
        return this.http.get<AiModelConfig[]>(`${ecomindaiModelConfig}/${model}`);
    }
}
