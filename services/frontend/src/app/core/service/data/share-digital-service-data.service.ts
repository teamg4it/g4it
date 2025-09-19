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
import { Constants } from "src/constants";
import { DigitalService } from "../../interfaces/digital-service.interfaces";
import {
    InDatacenterRest,
    InPhysicalEquipmentRest,
    InVirtualEquipmentRest,
} from "../../interfaces/input.interface";
import {
    OutPhysicalEquipmentRest,
    OutVirtualEquipmentRest,
} from "../../interfaces/output.interface";

const endpoint = Constants.ENDPOINTS.sharedDs;

@Injectable({
    providedIn: "root",
})
export class ShareDigitalServiceDataService {
    constructor(private readonly http: HttpClient) {}

    getSharedPhysicalEquipments(
        uid: string | null,
    ): Observable<InPhysicalEquipmentRest[]> {
        return this.http.get<InPhysicalEquipmentRest[]>(
            `${endpoint}/${uid}/inputs/physical-equipments`,
        );
    }

    getSharedVirtualEquipments(uid: string | null): Observable<InVirtualEquipmentRest[]> {
        return this.http.get<InVirtualEquipmentRest[]>(
            `${endpoint}/${uid}/inputs/virtual-equipments`,
        );
    }
    getReferentialData(): Observable<unknown> {
        return this.http.get(`${endpoint}/referential-data`);
    }

    getInSharedDataCenters(uid: DigitalService["uid"]): Observable<InDatacenterRest[]> {
        return this.http.get<InDatacenterRest[]>(
            `${endpoint}/${uid}/inputs/data-centers`,
        );
    }

    getOutSharedPhysicalEquipments(
        uid: DigitalService["uid"],
    ): Observable<OutPhysicalEquipmentRest[]> {
        return this.http.get<OutPhysicalEquipmentRest[]>(
            `${endpoint}/${uid}/outputs/physical-equipments`,
        );
    }

    getOutSharedVirtualEquipments(
        uid: DigitalService["uid"],
    ): Observable<OutVirtualEquipmentRest[]> {
        return this.http.get<OutVirtualEquipmentRest[]>(
            `${endpoint}/${uid}/outputs/virtual-equipments`,
        );
    }
}
