/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, forkJoin } from "rxjs";
import { Constants } from "src/constants";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "../../interfaces/footprint.interface";
import { transformEquipmentType } from "../mapper/array";

const endpoint = Constants.ENDPOINTS.inventories;

@Injectable({
    providedIn: "root",
})
export class FootprintDataService {
    constructor(private readonly http: HttpClient) {}

    getFootprint(inventoryId: number): Observable<Criterias> {
        return this.http.get<Criterias>(
            `${endpoint}/${inventoryId}/indicators/equipments`,
        );
    }

    getApplicationFootprint(inventoryId: number): Observable<ApplicationFootprint[]> {
        return this.http.get<ApplicationFootprint[]>(
            `${endpoint}/${inventoryId}/indicators/applications`,
        );
    }

    transformApplicationEquipmentType(
        applications: ApplicationFootprint[],
        currentWorkspaceName: string,
    ): ApplicationFootprint[] {
        return applications.map((app) => {
            return {
                ...app,
                impacts: app?.impacts?.map((impact) => {
                    return {
                        ...impact,
                        equipmentType: transformEquipmentType(
                            impact?.equipmentType,
                            currentWorkspaceName,
                        ),
                    };
                }),
            };
        });
    }
    getApplicationCriteriaFootprint(
        inventoryId: number,
        app: string,
        criteria: string,
    ): Observable<ApplicationCriteriaFootprint[]> {
        return this.http.get<ApplicationCriteriaFootprint[]>(
            `${endpoint}/${inventoryId}/indicators/applications/${app}/${criteria}`,
        );
    }

    getDatacenters(inventoryId: number) {
        return this.http.get<Datacenter[]>(
            `${endpoint}/${inventoryId}/indicators/datacenters`,
        );
    }

    getPhysicalEquipments(inventoryId: number) {
        const averageAge$ = this.http.get<PhysicalEquipmentAvgAge[]>(
            `${endpoint}/${inventoryId}/indicators/physicalEquipmentsAvgAge`,
        );
        const lowImpact$ = this.http.get<PhysicalEquipmentLowImpact[]>(
            `${endpoint}/${inventoryId}/indicators/physicalEquipmentsLowImpact`,
        );
        const elecConsumption$ = this.http.get<PhysicalEquipmentsElecConsumption[]>(
            `${endpoint}/${inventoryId}/indicators/physicalEquipmentsElecConsumption`,
        );

        return forkJoin([averageAge$, lowImpact$, elecConsumption$]);
    }

    deleteIndicators(inventoryId: number) {
        return this.http.delete<any>(`${endpoint}/${inventoryId}/indicators`);
    }

    downloadExportResultsFile(inventoryId: number): Observable<any> {
        return this.http.get(`${endpoint}/${inventoryId}/indicators/export/download`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }
}
