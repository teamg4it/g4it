import { TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DataCenterEquipmentStatsComponent } from "./datacenter-equipment-stats.component";

describe("DataCenterEquipmentStatsComponent", () => {
    let component: DataCenterEquipmentStatsComponent;
    let inventoryUtilService: jasmine.SpyObj<InventoryUtilService>;
    let footprintStoreService: jasmine.SpyObj<FootprintStoreService>;

    beforeEach(() => {
        const inventoryUtilServiceSpy = jasmine.createSpyObj("InventoryUtilService", [
            "computeEquipmentStats",
            "computeCloudStats",
            "computeDataCenterStats",
        ]);
        const footprintStoreServiceSpy = jasmine.createSpyObj("FootprintStoreService", [
            "filters",
        ]);

        TestBed.configureTestingModule({
            providers: [
                DataCenterEquipmentStatsComponent,
                { provide: TranslateService, useValue: {} },
                { provide: IntegerPipe, useValue: {} },
                { provide: DecimalsPipe, useValue: {} },
                { provide: GlobalStoreService, useValue: {} },
                { provide: InventoryUtilService, useValue: inventoryUtilServiceSpy },
                { provide: FootprintStoreService, useValue: footprintStoreServiceSpy },
            ],
        });

        component = TestBed.inject(DataCenterEquipmentStatsComponent);
        inventoryUtilService = TestBed.inject(
            InventoryUtilService,
        ) as jasmine.SpyObj<InventoryUtilService>;
        footprintStoreService = TestBed.inject(
            FootprintStoreService,
        ) as jasmine.SpyObj<FootprintStoreService>;
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });
});
