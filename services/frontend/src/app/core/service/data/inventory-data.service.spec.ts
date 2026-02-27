/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { Inventory } from "../../interfaces/inventory.interfaces";
import { InventoryDataService } from "./inventory-data.service";

describe("InventoryDataService", () => {
    let httpMock: HttpTestingController;
    let inventoryService: InventoryDataService;
    let inventoryDate: any = 2;
    let inventory: any = {
        name: "Mar 2024",
        type: "INFORMATION_SYSTEM",
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [InventoryDataService],
        });
        inventoryService = TestBed.inject(InventoryDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be create", () => {
        expect(inventoryService).toBeTruthy();
    });

    it("createInventory() should POST an Inventory", () => {
        inventoryService.createInventory(inventory).subscribe();

        const req = httpMock.expectOne(`inventories`);
        expect(req.request.method).toEqual("POST");

        httpMock.verify();
    });

    it("getInventories() should get inventories", () => {
        const inventories: Inventory[] = [
            {
                id: 2,
                type: "test",
                name: "test",
                date: new Date("12 April 2023 08:48 UTC"),
                creationDate: new Date("12 April 2023 08:48 UTC"),
                lastUpdateDate: new Date("18 April 2023 14:55 UTC"),
                workspace: "SSG",
                dataCenterCount: 4,
                physicalEquipmentCount: 17,
                virtualEquipmentCount: 21,
                applicationCount: 24,
                enableDataInconsistency: false,
                tasks: [],
            },
            {
                id: 2,
                type: "test",
                name: "test",
                date: new Date("12 April 2023 08:48 UTC"),
                creationDate: new Date("12 April 2023 08:48 UTC"),
                lastUpdateDate: new Date("18 April 2023 14:55 UTC"),
                workspace: "SSG",
                dataCenterCount: 4,
                physicalEquipmentCount: 17,
                virtualEquipmentCount: 21,
                applicationCount: 24,
                tasks: [],
                enableDataInconsistency: false,
            },
        ];

        inventoryService.getInventories().subscribe((res) => {
            expect(res).toEqual(inventories);
        });

        const req = httpMock.expectOne("inventories");

        expect(req.request.method).toEqual("GET");

        req.flush(inventories);

        httpMock.verify();
    });

    it("deleteInventory() should DELETE one inventory", () => {
        inventoryService.deleteInventory(inventoryDate).subscribe();

        const req = httpMock.expectOne("inventories/" + inventoryDate);

        expect(req.request.method).toEqual("DELETE");

        httpMock.verify();
    });
});
