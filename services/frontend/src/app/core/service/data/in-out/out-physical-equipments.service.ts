/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { OutPhysicalEquipmentRest } from "src/app/core/interfaces/output.interface";
import { Constants } from "src/constants";

const endpoint = Constants.ENDPOINTS.digitalServices;

@Injectable({
    providedIn: "root",
})
export class OutPhysicalEquipmentsService {
    private readonly API = "physical-equipments";

    constructor(private readonly http: HttpClient) {}

    get(digitalServiceUid: string): Observable<OutPhysicalEquipmentRest[]> {
        return this.http.get<OutPhysicalEquipmentRest[]>(
            `${endpoint}/${digitalServiceUid}/outputs/${this.API}`,
        );
    }
}
