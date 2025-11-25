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
const endpointDs = Constants.ENDPOINTS.dsv;
@Injectable({
    providedIn: "root",
})
export class ShareDigitalServiceDataService {
    constructor(private readonly http: HttpClient) {}

    getSharedPhysicalEquipments(
        uid: string | null,
        shareToken: string,
    ): Observable<InPhysicalEquipmentRest[]> {
        return this.http.get<InPhysicalEquipmentRest[]>(
            `${endpoint}/${shareToken}/${endpointDs}/${uid}/inputs/physical-equipments`,
        );
    }

    getSharedVirtualEquipments(
        uid: string | null,
        shareToken: string,
    ): Observable<InVirtualEquipmentRest[]> {
        return this.http.get<InVirtualEquipmentRest[]>(
            `${endpoint}/${shareToken}/${endpointDs}/${uid}/inputs/virtual-equipments`,
        );
    }
    getReferentialData(uid: string, shareToken: string): Observable<unknown> {
        return this.http.get(
            `${endpoint}/${shareToken}/${endpointDs}/${uid}/referential-data`,
        );
    }

    getInSharedDataCenters(
        uid: DigitalService["uid"],
        shareToken: string,
    ): Observable<InDatacenterRest[]> {
        return this.http.get<InDatacenterRest[]>(
            `${endpoint}/${shareToken}/${endpointDs}/${uid}/inputs/datacenters`,
        );
    }

    getOutSharedPhysicalEquipments(
        uid: DigitalService["uid"],
        shareToken: string,
    ): Observable<OutPhysicalEquipmentRest[]> {
        return this.http.get<OutPhysicalEquipmentRest[]>(
            `${endpoint}/${shareToken}/${endpointDs}/${uid}/outputs/physical-equipments`,
        );
    }

    getOutSharedVirtualEquipments(
        uid: DigitalService["uid"],
        shareToken: string,
    ): Observable<OutVirtualEquipmentRest[]> {
        return this.http.get<OutVirtualEquipmentRest[]>(
            `${endpoint}/${shareToken}/${endpointDs}/${uid}/outputs/virtual-equipments`,
        );
    }
}
