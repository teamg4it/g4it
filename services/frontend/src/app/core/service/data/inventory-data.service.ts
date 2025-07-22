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
}
