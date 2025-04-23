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

const endpoint = Constants.ENDPOINTS.digitalServices;

@Injectable({
    providedIn: "root",
})
export class InVirtualEquipmentsService {
    private HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });

    private API = "virtual-equipments";

    constructor(private http: HttpClient) {}

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
        digitalServiceUid: string,
        physicalEquipmentId: number,
    ): Observable<InVirtualEquipmentRest> {
        const params = new HttpParams().set("physicalEqpId", physicalEquipmentId);
        return this.http.put<InVirtualEquipmentRest>(
            `${endpoint}/${digitalServiceUid}/inputs/${this.API}`,
            equipment,
            {
                headers: this.HEADERS,
                params,
            },
        );
    }

    create(equipment: InVirtualEquipmentRest): Observable<InVirtualEquipmentRest> {
        return this.http.post<InVirtualEquipmentRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}`,
            equipment,
            { headers: this.HEADERS },
        );
    }

    delete(id: number, digitalServiceUid: string): Observable<InVirtualEquipmentRest> {
        return this.http.delete<InVirtualEquipmentRest>(
            `${endpoint}/${digitalServiceUid}/inputs/${this.API}/${id}`,
        );
    }
}
