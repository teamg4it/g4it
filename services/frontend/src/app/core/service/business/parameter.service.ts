import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { DigitalServiceParameterIa } from "../../interfaces/digital-service.interfaces";

@Injectable({ providedIn: "root" })
export class ParameterService {
    private readonly baseUrl = "/api/parameter";

    constructor(private readonly http: HttpClient) {}

    getModels(): Observable<string[]> {
        return this.http.get<string[]>(`${this.baseUrl}/models`);
    }

    getParameters(): Observable<string[]> {
        return this.http.get<string[]>(`${this.baseUrl}/parameters`);
    }

    getFrameworks(): Observable<string[]> {
        return this.http.get<string[]>(`${this.baseUrl}/frameworks`);
    }

    getQuantizations(): Observable<string[]> {
        return this.http.get<string[]>(`${this.baseUrl}/quantizations`);
    }

    submitForm(data: DigitalServiceParameterIa): Observable<any> {
        return this.http.post(`${this.baseUrl}/submit`, data);
    }
}
