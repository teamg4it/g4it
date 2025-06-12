/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { InventoryUtilService } from "./inventory-util.service";

import { Filter } from "../../interfaces/filter.interface";
import {
    Criterias,
    Datacenter,
    Impact,
    PhysicalEquipment,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "../../interfaces/footprint.interface";
import { InventoryFilterSet } from "../../interfaces/inventory.interfaces";
import { DecimalsPipe } from "../../pipes/decimal.pipe";
import { IntegerPipe } from "../../pipes/integer.pipe";
describe("InventoryUtilService", () => {
    let httpMock: HttpTestingController;
    let service: InventoryUtilService;
    let decimalsPipe: DecimalsPipe;
    let integerPipe: IntegerPipe;
    let translate: TranslateService;

    const footprint = {
        criteria1: {
            impacts: [{ acvStep: "step1" }, { acvStep: "step2" }, { acvStep: "step1" }],
        },
        criteria2: {
            impacts: [
                { acvStep: "step1" },
                { acvStep: "step2" },
                { acvStep: "step2" },
                { acvStep: "step2" },
            ],
        },
        criteria3: {
            impacts: [{ acvStep: "step1" }, { acvStep: "step1" }, { acvStep: "step1" }],
        },
    } as unknown as Criterias;
    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [
                InventoryUtilService,
                DecimalsPipe,
                IntegerPipe,
                TranslateService,
            ],
        });

        service = TestBed.inject(InventoryUtilService);

        decimalsPipe = TestBed.inject(DecimalsPipe);
        integerPipe = TestBed.inject(IntegerPipe);
        translate = TestBed.inject(TranslateService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("getDatacenterStats", () => {
        it("should return an array with count and avgPue", () => {
            const count = 5;
            const avgPue = 1.5;

            const result = service.getDatacenterStats(count, avgPue);

            expect(result).toEqual([
                {
                    label: service["decimalsPipe"].transform(count),
                    value: count,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.nb-dc",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.datacenters",
                    ),
                },
                {
                    label: service["decimalsPipe"].transform(avgPue),
                    value: avgPue,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.ave-pue",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.ave-pue",
                    ),
                },
            ]);
        });

        it("should return an array with undefined values if count and avgPue are NaN", () => {
            const count = NaN;
            const avgPue = NaN;

            const result = service.getDatacenterStats(count, avgPue);

            expect(result).toEqual([
                {
                    label: service["decimalsPipe"].transform(count),
                    value: undefined,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.nb-dc",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.datacenters",
                    ),
                },
                {
                    label: service["decimalsPipe"].transform(avgPue),
                    value: undefined,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.ave-pue",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.ave-pue",
                    ),
                },
            ]);
        });
    });
    describe("valueImpact", () => {
        it("should return the country value", () => {
            const impact = {
                country: "France",
                entity: "Entity",
                equipment: "Equipment",
                status: "Status",
            } as Impact;
            const field = "country";

            const result = service.valueImpact(impact, field);

            expect(result).toEqual("France");
        });

        it("should return the entity value", () => {
            const impact = {
                country: "France",
                entity: "Entity",
                equipment: "Equipment",
                status: "Status",
            } as Impact;
            const field = "entity";

            const result = service.valueImpact(impact, field);

            expect(result).toEqual("Entity");
        });

        it("should return the equipment value", () => {
            const impact = {
                country: "France",
                entity: "Entity",
                equipment: "Equipment",
                status: "Status",
            } as Impact;
            const field = "equipment";

            const result = service.valueImpact(impact, field);

            expect(result).toEqual("Equipment");
        });

        it("should return the status value", () => {
            const impact = {
                country: "France",
                entity: "Entity",
                equipment: "Equipment",
                status: "Status",
            } as Impact;
            const field = "status";

            const result = service.valueImpact(impact, field);

            expect(result).toEqual("Status");
        });

        it("should return null for an invalid field", () => {
            const impact = {
                country: "France",
                entity: "Entity",
                equipment: "Equipment",
                status: "Status",
            } as Impact;
            const field = "invalidField";

            const result = service.valueImpact(impact, field);

            expect(result).toBeNull();
        });
    });
    describe("valueEquipment", () => {
        it("should return the country value", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
            } as PhysicalEquipment;
            const field = "country";

            const result = service.valueEquipment(equipment, field);

            expect(result).toEqual("France");
        });

        it("should return the entity value", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
            } as PhysicalEquipment;
            const field = "entity";

            const result = service.valueEquipment(equipment, field);

            expect(result).toEqual("Entity");
        });

        it("should return the equipment value", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
            } as PhysicalEquipment;
            const field = "equipment";

            const result = service.valueEquipment(equipment, field);

            expect(result).toEqual("Equipment");
        });

        it("should return the status value", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
            } as PhysicalEquipment;
            const field = "status";

            const result = service.valueEquipment(equipment, field);

            expect(result).toEqual("Status");
        });

        it("should return null for an invalid field", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
            } as PhysicalEquipment;
            const field = "invalidField";

            const result = service.valueEquipment(equipment, field);

            expect(result).toBeNull();
        });
    });

    describe("isEquipmentPresent", () => {
        it("should return true if equipment matches all filters", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
            } as unknown as PhysicalEquipment;

            const filtersSet: InventoryFilterSet = {
                country: new Set(["France"]),
                entity: new Set(["Entity"]),
                equipment: new Set(["Equipment"]),
                status: new Set(["Status"]),
            };

            const result = service.isEquipmentPresent(equipment, filtersSet, true);

            expect(result).toBe(true);
        });

        it("should return false if equipment does not match any filter", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
                lowImpact: true,
            } as unknown as PhysicalEquipment;

            const filtersSet: InventoryFilterSet = {
                country: new Set(["Germany"]),
                entity: new Set(["Entity"]),
                equipment: new Set(["Equipment"]),
                status: new Set(["Status"]),
                lowImpact: new Set(["true"]),
            };

            const result = service.isEquipmentPresent(equipment, filtersSet, true);

            expect(result).toBe(false);
        });

        it("should return false if equipment matches some filters but not all", () => {
            const equipment = {
                country: "France",
                nomEntite: "Entity",
                type: "Equipment",
                statut: "Status",
                lowImpact: true,
            } as unknown as PhysicalEquipment;

            const filtersSet: InventoryFilterSet = {
                country: new Set(["France"]),
                entity: new Set(["Entity"]),
                equipment: new Set(["Equipment"]),
                status: new Set(["Status"]),
                lowImpact: new Set(["false"]),
            };

            const result = service.isEquipmentPresent(equipment, filtersSet, true);

            expect(result).toBe(false);
        });
    });

    describe("maxCriteriaAndStep", () => {
        it("should return the criteria and step with the maximum number of impacts", () => {
            // const service = new InventoryUtilService();
            const result = service.maxCriteriaAndStep(footprint);

            expect(result).toEqual(["criteria2", "step2"]);
        });

        it("should return the first criteria and step if multiple have the same maximum number of impacts", () => {
            //const service = new InventoryUtilService();
            const result = service.maxCriteriaAndStep(footprint);

            expect(result).toEqual(["criteria2", "step2"]);
        });
    });

    describe("computeDataCenterStats", () => {
        it("should return an array with datacenter count and average PUE", async () => {
            const filters: Filter<string> = {
                country: ["France"],
                entity: ["Entity"],
                equipment: ["Equipment"],
                status: ["Status"],
            };

            const filterFields = ["country", "entity", "equipment", "status"];

            const datacenters = [
                {
                    dataCenterName: "Datacenter 1",
                    physicalEquipmentCount: 10,
                    pue: 1.5,
                    country: "France",
                    entity: "Entity",
                    equipment: "Equipment",
                    status: "Status",
                },
                {
                    dataCenterName: "Datacenter 2",
                    physicalEquipmentCount: 5,
                    pue: 1.2,
                    country: "France",
                    entity: "Entity",
                    equipment: "Equipment",
                    status: "Status",
                },
            ] as Datacenter[];

            const result = await service.computeDataCenterStats(
                filters,
                filterFields,
                datacenters,
            );

            expect(result).toEqual([
                {
                    label: service["decimalsPipe"].transform(2),
                    value: 2,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.nb-dc",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.datacenters",
                    ),
                },
                {
                    label: service["decimalsPipe"].transform(1.4),
                    value: 1.4,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.ave-pue",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.ave-pue",
                    ),
                },
            ]);
        });
    });

    describe("computeEquipmentStats", () => {
        it("should compute stats with all filters applied", async () => {
            const equipments: [
                PhysicalEquipmentAvgAge[],
                PhysicalEquipmentLowImpact[],
                PhysicalEquipmentsElecConsumption[],
            ] = [
                [{ poids: 10, ageMoyen: 5 }] as PhysicalEquipmentAvgAge[],
                [{ quantite: 20, lowImpact: true }] as PhysicalEquipmentLowImpact[],
                [{ elecConsumption: 100 }] as PhysicalEquipmentsElecConsumption[],
            ];

            const filters: Filter<string> = { country: ["ALL"] };
            const filterFields = ["country"];
            const footprint: Criterias = {
                "climate-change": {
                    impacts: [
                        {
                            acvStep: "UTILISATION",
                            status: "ACTIVE",
                            countValue: 50,
                        } as Impact,
                    ],
                } as any,
            };

            const stats = await service.computeEquipmentStats(
                equipments,
                filters,
                filterFields,
                footprint,
            );

            expect(stats.length).toEqual(4);
        });

        it("should return an array with count and avgPue", () => {
            const count = 5;
            const avgPue = 1.5;

            const result = service.getDatacenterStats(count, avgPue);

            expect(result).toEqual([
                {
                    label: service["decimalsPipe"].transform(count),
                    value: count,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.nb-dc",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.datacenters",
                    ),
                },
                {
                    label: service["decimalsPipe"].transform(avgPue),
                    value: avgPue,
                    description: service["translate"].instant(
                        "inventories-footprint.global.tooltip.ave-pue",
                    ),
                    title: service["translate"].instant(
                        "inventories-footprint.global.ave-pue",
                    ),
                },
            ]);
        });
    });
});
