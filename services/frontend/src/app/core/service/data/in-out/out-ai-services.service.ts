import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { OutAiServiceRest } from "src/app/core/interfaces/output.interface";
import { Constants } from "src/constants";

@Injectable({
    providedIn: "root",
})
export class OutAiServicesService {
    private readonly API = "ai-services";

    constructor(private readonly http: HttpClient) {}

    getByInventory(inventoryId: number): Observable<OutAiServiceRest[]> {
        return this.http.get<OutAiServiceRest[]>(
            `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/${this.API}`,
        );
    }
}
