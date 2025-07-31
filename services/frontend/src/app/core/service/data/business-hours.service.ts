import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { BusinessHours } from "../../interfaces/business-hours.interface";

@Injectable({
    providedIn: "root",
})
export class BusinessHoursService {
    constructor(private readonly http: HttpClient) {}

    getBusinessHours(): Observable<BusinessHours[]> {
        return this.http.get<BusinessHours[]>(Constants.ENDPOINTS.businessHours);
    }
}
