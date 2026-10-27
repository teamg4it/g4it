/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, forkJoin, of } from "rxjs";
import { Constants } from "src/constants";
import { ApplicationFiltersRest } from "../../interfaces/filter.interface";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
    ApplicationIndicatorsPage,
    ApplicationIndicatorsPageRequest,
    ApplicationMultiCriteriaImpact,
    ApplicationMultiCriteriaImpactsRequest,
    ApplicationMultiCriteriaRequest,
    ApplicationMultiCriteriaResponse,
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
    VirtualEquipmentElectricityConsumption,
    VirtualEquipmentLowImpact,
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
        // return this.http.get<ApplicationFootprint[]>(
        //     `${endpoint}/${inventoryId}/indicators/applications`,
        // );
        return of([]);
    }

    getApplicationIndicatorsPage(
        inventoryId: number,
        request: ApplicationIndicatorsPageRequest,
        page: number,
        size: number,
        sortField: string,
        sortDirection: "asc" | "desc",
    ): Observable<ApplicationIndicatorsPage> {
        const params = new HttpParams()
            .set("page", page)
            .set("size", size)
            .set("sort", `${sortField},${sortDirection}`);
        return this.http.post<ApplicationIndicatorsPage>(
            `${endpoint}/${inventoryId}/indicators/applications`,
            request,
            { params },
        );
    }

    getApplicationFilters(inventoryId: number): Observable<ApplicationFiltersRest> {
        return this.http.get<ApplicationFiltersRest>(
            `${endpoint}/${inventoryId}/indicators/applications/filters`,
        );
    }

    getApplicationMultiCriteriaImpacts(
        inventoryId: number,
        filters: ApplicationMultiCriteriaImpactsRequest,
    ): Observable<ApplicationMultiCriteriaImpact[]> {
        return this.http.post<ApplicationMultiCriteriaImpact[]>(
            `${endpoint}/${inventoryId}/indicators/applications/multi-criteria-impacts`,
            filters,
        );
    }

    getApplicationMultiCriteriaGraph(
        inventoryId: number,
        request: ApplicationMultiCriteriaRequest,
    ): Observable<ApplicationMultiCriteriaResponse[]> {
        return this.http.post<ApplicationMultiCriteriaResponse[]>(
            `${endpoint}/${inventoryId}/indicators/applications/multi-criteria`,
            request,
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

    getApplicationKeyIndicators(inventoryId: number) {
        const lowImpact$ = this.http.get<VirtualEquipmentLowImpact[]>(
            `${endpoint}/${inventoryId}/indicators/virtualEquipmentsLowImpact`,
        );
        const elecConsumption$ = this.http.get<VirtualEquipmentElectricityConsumption[]>(
            `${endpoint}/${inventoryId}/indicators/virtualEquipmentsElecConsumption`,
        );

        return forkJoin([lowImpact$, elecConsumption$]);
    }

    getSourceList(inventoryId: number) {
        return this.http.get<string[]>(`${endpoint}/${inventoryId}/sources`);
    }
}
