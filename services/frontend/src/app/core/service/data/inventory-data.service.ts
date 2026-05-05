/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import {
    CreateInventory,
    Inventory,
    InventoryCriteriaRest,
    InventoryUpdateRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { Constants } from "src/constants";
import {
    RenewServiceResp,
    RenewServiceUpdateResp,
} from "../../interfaces/digital-service.interfaces";

const endpoint = Constants.ENDPOINTS.inventories;

@Injectable({
    providedIn: "root",
})
export class InventoryDataService {
    constructor(private readonly http: HttpClient) {}

    createInventory(creationObj: CreateInventory): Observable<Inventory> {
        return this.http.post<Inventory>(`${endpoint}`, creationObj);
    }

    getInventories(inventoryId?: number): Observable<Inventory[]> {
        if (inventoryId === undefined) {
            return this.http.get<Inventory[]>(`${endpoint}`);
        }

        let params = new HttpParams().append("inventoryId", inventoryId);

        return this.http.get<Inventory[]>(`${endpoint}`, {
            params,
        });
    }

    updateInventory(inventory: InventoryUpdateRest): Observable<InventoryUpdateRest> {
        return this.http.put<InventoryUpdateRest>(`${endpoint}`, inventory);
    }

    deleteInventory(id: number): Observable<Inventory[]> {
        return this.http.delete<any>(`${endpoint}/${id}`);
    }

    updateInventoryCriteria(
        inventoryCriteria: InventoryCriteriaRest,
    ): Observable<Inventory> {
        return this.http.put<Inventory>(`${endpoint}`, inventoryCriteria);
    }

    getServiceRenewalDetails(inventoryId: string | number): Observable<RenewServiceResp> {
        return this.http.get<RenewServiceResp>(`${endpoint}/${inventoryId}/renew`);
    }

    renewService(
        payload: {
            retentionDays: number;
            action: string;
            serviceId: string;
        },
        inventoryId: string | number,
    ): Observable<RenewServiceUpdateResp> {
        return this.http.post<RenewServiceUpdateResp>(
            `${endpoint}/${inventoryId}/renew`,
            {
                retentionDays: payload.retentionDays,
                action: payload.action,
                serviceId: payload.serviceId,
            },
        );
    }

    downloadWorkspaceSettingsZip(): Observable<Blob> {
        return this.http.get(`${endpoint}/workspace-referential-csv`, {
            responseType: "blob",
        });
    }
}
