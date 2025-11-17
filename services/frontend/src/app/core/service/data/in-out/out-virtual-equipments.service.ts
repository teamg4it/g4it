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
import { OutVirtualEquipmentRest } from "src/app/core/interfaces/output.interface";

import { Constants } from "src/constants";

@Injectable({
    providedIn: "root",
})
export class OutVirtualEquipmentsService {
    private readonly API = "virtual-equipments";

    constructor(private readonly http: HttpClient) {}

    getByDigitalService(
        digitalServiceUid: string,
    ): Observable<OutVirtualEquipmentRest[]> {
        return this.http.get<OutVirtualEquipmentRest[]>(
            `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/${this.API}`,
        );
    }

    getByInventory(inventoryId: number): Observable<OutVirtualEquipmentRest[]> {
        return this.http.get<OutVirtualEquipmentRest[]>(
            `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/${this.API}`,
        );
    }
}
