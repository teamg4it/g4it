import { signal } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { ActivatedRoute, convertToParamMap, Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { OutAiServicesService } from "src/app/core/service/data/in-out/out-ai-services.service";
import { OutVirtualEquipmentsService } from "src/app/core/service/data/in-out/out-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import * as en from "../../../assets/i18n/en.json";
import * as fr from "../../../assets/i18n/fr.json";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";

describe("InventoriesFootprintComponent evaluation indicators", () => {
    let component: InventoriesFootprintComponent;
    let translate: TranslateService;
    let filters: ReturnType<typeof signal<any>>;

    beforeEach(() => {
        filters = signal({});
        TestBed.configureTestingModule({
            imports: [InventoriesFootprintComponent, TranslateModule.forRoot()],
            providers: [
                InventoryUtilService,
                IntegerPipe,
                DecimalsPipe,
                {
                    provide: ActivatedRoute,
                    useValue: { snapshot: { paramMap: convertToParamMap({}) } },
                },
                { provide: Router, useValue: {} },
                { provide: FootprintStoreService, useValue: { filters } },
                { provide: GlobalStoreService, useValue: { criteriaList: () => ({}) } },
                {
                    provide: DigitalServiceStoreService,
                    useValue: { countryMap: () => ({ France: "France" }) },
                },
                ...[
                    OutVirtualEquipmentsService,
                    OutAiServicesService,
                    InVirtualEquipmentsService,
                    UserService,
                    InventoryService,
                    FilterService,
                    EvaluationDataService,
                    FootprintDataService,
                    FootprintService,
                    DigitalServiceBusinessService,
                ].map((provide) => ({ provide, useValue: {} })),
            ],
        });
        translate = TestBed.inject(TranslateService);
        translate.setTranslation("en", en);
        translate.setTranslation("fr", fr);
        translate.use("en");
        const util = TestBed.inject(InventoryUtilService);
        // Isolate asynchronous legacy aggregates, not the real usage-card mapping.
        spyOn(util, "computeEquipmentStats").and.resolveTo([]);
        spyOn(util, "computeCloudStats").and.resolveTo([]);
        spyOn(util, "computeDataCenterStats").and.resolveTo([]);
        // No lifecycle/network polling: exercise the actual injected container/computed.
        component = TestBed.createComponent(
            InventoriesFootprintComponent,
        ).componentInstance;
    });

    function usageCards() {
        return component.statGroups()[0].items.slice(-2);
    }

    it("appends persisted unrounded shares to existing infrastructure cards, not energy", () => {
        const equipment = { title: "Equipment", label: "10", description: "" };
        const lifespan = { title: "Lifespan", label: "5", description: "" };
        const cloud = { title: "Cloud", label: "3", description: "" };
        component.equipmentStats.set([equipment, lifespan] as any);
        component.cloudStats.set([cloud] as any);
        component.allUnmodifiedFootprint.set({
            climate: {
                impacts: [],
                byodDevicesPercentage: 57.4,
                copeDevicesPercentage: 28.6,
            },
        } as any);

        const groups = component.statGroups();
        expect(groups[0].items.slice(0, 3)).toEqual([equipment, lifespan, cloud]);
        expect(usageCards()).toEqual([
            jasmine.objectContaining({ title: "BYOD Devices (%)", label: "57", value: 57.4, unit: "%" }),
            jasmine.objectContaining({ title: "COPE Devices (%)", label: "29", value: 28.6, unit: "%" }),
        ]);
        expect(groups[1].items).toHaveSize(3);
    });

    it("refreshes labels on language change without replacing the evaluation snapshot", () => {
        component.allUnmodifiedFootprint.set({
            climate: { impacts: [], byodDevicesPercentage: 0, copeDevicesPercentage: 0.4 },
        } as any);
        const snapshot = component.allUnmodifiedFootprint();
        expect(usageCards()[0].title).toBe("BYOD Devices (%)");

        translate.use("fr");

        expect(component.allUnmodifiedFootprint()).toBe(snapshot);
        expect(usageCards()).toEqual([
            jasmine.objectContaining({ title: "Appareils BYOD (%)", value: 0, label: "0" }),
            jasmine.objectContaining({ title: "Appareils COPE (%)", value: 0.4, label: "< 1" }),
        ]);
        translate.use("en");
        expect(usageCards()[1].title).toBe("COPE Devices (%)");
    });

    it("keeps inventory-wide shares when filters exclude all impacts and updates on reevaluation", () => {
        component.allUnmodifiedFootprint.set({
            climate: {
                impacts: [{ country: "France", quantity: 0.57 }],
                byodDevicesPercentage: 30,
                copeDevicesPercentage: 30,
            },
        } as any);
        expect(usageCards().map((card) => card.value)).toEqual([30, 30]);
        filters.set({ country: ["Canada"] });
        expect(usageCards().map((card) => card.value)).toEqual([30, 30]);

        component.allUnmodifiedFootprint.set({
            climate: { impacts: [], byodDevicesPercentage: 10, copeDevicesPercentage: 90 },
        } as any);
        expect(usageCards().map((card) => card.value)).toEqual([10, 90]);
    });

    it("preserves independent absent/null no-data values, including an empty response", () => {
        for (const footprint of [
            {},
            { climate: { impacts: [] } },
            { climate: { impacts: [], byodDevicesPercentage: null, copeDevicesPercentage: null } },
        ]) {
            component.allUnmodifiedFootprint.set(footprint as any);
            expect(usageCards().map((card) => [card.value, card.label])).toEqual([
                [undefined, ""], [undefined, ""],
            ]);
        }
        component.allUnmodifiedFootprint.set({
            climate: { impacts: [], byodDevicesPercentage: 0, copeDevicesPercentage: null },
        } as any);
        expect(usageCards().map((card) => card.value)).toEqual([0, undefined]);
    });

    it("retains external provider metadata for cloud and AI category grouping", () => {
        const source = {
            infrastructureType: "CLOUD_SERVICES", provider: "aws", location: "France",
            criterion: "climate-change", lifecycleStep: "USING", unitImpact: 4,
            peopleEqImpact: 2, commonFilters: ["Entity"], quantity: 3,
        };
        const cloud = component.transformOutVirtualEquipment([
            source, { ...source, infrastructureType: "OTHER" },
        ] as any);
        expect(cloud).toHaveSize(1);
        expect(cloud[0]).toEqual(jasmine.objectContaining({
            itemCategory: "EXTERNAL", sunburstServiceType: "cloud",
            sunburstProvider: "aws", equipment: "Cloud AWS", impact: 4, sip: 2,
        }));
        const ai = component.transformOutAiServices([source] as any);
        expect(ai[0]).toEqual(jasmine.objectContaining({
            sunburstServiceType: "ai", sunburstProvider: "aws", impact: 4, sip: 2,
        }));
        expect(source.quantity).toBe(3);
        expect(Constants.EQUIPMENT_DIMENSIONS.slice(0, 2)).toEqual(["acvStep", "equipment"]);
    });
});
