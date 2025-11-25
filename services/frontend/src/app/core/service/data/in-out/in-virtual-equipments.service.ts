/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { InVirtualEquipmentRest } from "../../../interfaces/input.interface";

const endpoint = Constants.ENDPOINTS.digitalServicesVersions;

@Injectable({
    providedIn: "root",
})
export class InVirtualEquipmentsService {
    private readonly HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });

    private readonly API = "virtual-equipments";

    constructor(private readonly http: HttpClient) {}

    getByDigitalService(digitalServiceUid: string): Observable<InVirtualEquipmentRest[]> {
        return this.http.get<InVirtualEquipmentRest[]>(
            `${endpoint}/${digitalServiceUid}/inputs/${this.API}`,
        );
    }

    getByInventory(inventoryId: number): Observable<InVirtualEquipmentRest[]> {
        return this.http.get<InVirtualEquipmentRest[]>(
            `${Constants.ENDPOINTS.inventories}/${inventoryId}/inputs/${this.API}`,
        );
    }

    update(equipment: InVirtualEquipmentRest): Observable<InVirtualEquipmentRest> {
        return this.http.put<InVirtualEquipmentRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}/${equipment.id}`,
            equipment,
            {
                headers: this.HEADERS,
            },
        );
    }

    updateAllVms(
        equipment: InVirtualEquipmentRest[],
        digitalServiceVersionId: string,
        physicalEquipmentId: number,
    ): Observable<InVirtualEquipmentRest> {
        const params = new HttpParams().set("physicalEqpId", physicalEquipmentId);
        return this.http.put<InVirtualEquipmentRest>(
            `${endpoint}/${digitalServiceVersionId}/inputs/${this.API}`,
            equipment,
            {
                headers: this.HEADERS,
                params,
            },
        );
    }

    create(equipment: InVirtualEquipmentRest): Observable<InVirtualEquipmentRest> {
        return this.http.post<InVirtualEquipmentRest>(
            `${endpoint}/${equipment.digitalServiceVersionUid!}/inputs/${this.API}`,
            equipment,
            { headers: this.HEADERS },
        );
    }

    delete(
        id: number,
        digitalServiceVersionUid: string,
    ): Observable<InVirtualEquipmentRest> {
        return this.http.delete<InVirtualEquipmentRest>(
            `${endpoint}/${digitalServiceVersionUid}/inputs/${this.API}/${id}`,
        );
    }
}
