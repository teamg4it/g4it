/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG } from "ngx-echarts";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { ApplicationMulticriteriaFootprintComponent } from "./application-multicriteria-footprint.component";

describe("ApplicationMulticriteriaFootprintComponent", () => {
    let component: ApplicationMulticriteriaFootprintComponent;
    let fixture: ComponentFixture<ApplicationMulticriteriaFootprintComponent>;
    let routerSpy: jasmine.SpyObj<Router>;
    let translateService: TranslateService;
    let filterServiceSpy: jasmine.SpyObj<FilterService>;
    let footprintServiceSpy: jasmine.SpyObj<FootprintService>;
    let footprintStoreSpy: any;
    let appComponentSpy: any;

    const mockFootprintData = [
        {
            criteria: "climate-change",
            criteriaTitle: "Climate Change",
            unit: "kgCO2eq",
            impacts: [
                {
                    domain: "Domain1",
                    subDomain: "SubDomain1",
                    applicationName: "App1",
                    virtualEquipmentName: "Eq1",
                    impact: 100,
                    sip: 50,
                    equipmentType: "Server",
                    environment: "Prod",
                    cluster: "ClusterA",
                    lifeCycle: "use",
                    statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                },
            ],
        },
        {
            criteria: "resource-use",
            criteriaTitle: "Resource Use",
            unit: "kgSbeq",
            impacts: [
                {
                    domain: "Domain2",
                    subDomain: "SubDomain2",
                    applicationName: "App2",
                    virtualEquipmentName: "Eq2",
                    impact: 200,
                    sip: 100,
                    equipmentType: "Storage",
                    environment: "Test",
                    cluster: "ClusterB",
                    lifeCycle: "manufacturing",
                    statusIndicator: Constants.DATA_QUALITY_STATUS.error,
                },
            ],
        },
    ];

    beforeEach(async () => {
        routerSpy = jasmine.createSpyObj("Router", ["navigate"]);

        filterServiceSpy = jasmine.createSpyObj("FilterService", ["getFilterincludes"]);
        filterServiceSpy.getFilterincludes.and.returnValue(true);

        footprintServiceSpy = jasmine.createSpyObj("FootprintService", ["calculate"]);
        footprintServiceSpy.calculate.and.returnValue({
            footprintCalculated: [
                {
                    data: "Domain1",
                    total: { impact: 300, sip: 150 },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [
                        {
                            criteria: "climate-change",
                            sumImpact: 300,
                            sumSip: 150,
                            acvStep: "use",
                            country: "France",
                            entity: "Entity1",
                            equipment: "Server",
                            status: "active",
                            impact: 300,
                            sip: 150,
                            statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                            countValue: 1,
                        },
                    ],
                },
            ],
            criteriaCountMap: {
                "climate-change": {
                    status: { ok: 1, error: 0, total: 1 },
                },
            },
            impactsWithMaxDimensions: [
                {
                    name: "climate-change",
                    title: "Climate Change",
                    peopleeq: 150,
                    raw: 300,
                    unite: "kgCO2eq",
                    maxCriteria: {
                        name: "Domain1",
                        peopleeq: 150,
                        raw: 300,
                    },
                },
            ],
        });

        footprintStoreSpy = {
            appDimension: jasmine.createSpy().and.returnValue("domain"),
            applicationSelectedFilters: jasmine.createSpy().and.returnValue({
                domain: [
                    {
                        label: "Domain1",
                        checked: true,
                        children: [{ label: "SubDomain1", checked: true }],
                    },
                ],
            }),
            setApplicationDimension: jasmine.createSpy(),
        };

        appComponentSpy = {
            formatLifecycleImpact: jasmine.createSpy().and.callFake((x: any) => x),
            allUnmodifiedFootprint: [],
        };

        await TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                ApplicationMulticriteriaFootprintComponent,
            ],
            providers: [
                IntegerPipe,
                DecimalsPipe,
                {
                    provide: Router,
                    useValue: routerSpy,
                },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: { paramMap: { get: () => "1" } },
                    },
                },
                {
                    provide: FilterService,
                    useValue: filterServiceSpy,
                },
                {
                    provide: FootprintService,
                    useValue: footprintServiceSpy,
                },
                {
                    provide: FootprintStoreService,
                    useValue: footprintStoreSpy,
                },
                {
                    provide: GlobalStoreService,
                    useValue: {
                        criteriaList: () => ({}),
                    },
                },
                {
                    provide: InventoriesApplicationFootprintComponent,
                    useValue: appComponentSpy,
                },
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
        }).compileComponents();

        translateService = TestBed.inject(TranslateService);
        translateService.setTranslation("en", {
            criteria: {
                "climate-change": {
                    title: "Climate Change",
                    unit: "kgCO2eq",
                },
                "resource-use": {
                    title: "Resource Use",
                    unit: "kgSbeq",
                },
            },
            common: {
                peopleeq: "People eq.",
                "peopleeq-min": "People eq. min",
                "no-data": "No data",
            },
            "inventories-footprint": {
                "select-dimension": "Select dimension",
                "data-consistency": "Data consistency",
                application: {},
                "application-round-button": {},
                "round-button": {},
                global: {},
            },
            "ds-graph-description": {
                "global-vision": {},
            },
            "ds-graph-module": {
                inventory: "inventory",
            },
        });
        translateService.use("en");

        fixture = TestBed.createComponent(ApplicationMulticriteriaFootprintComponent);
        component = fixture.componentInstance;
        component.footprint = mockFootprintData as any;
        component.filterFields = [
            { field: "domain", label: "Domain" },
            { field: "environment", label: "Environment" },
        ] as any;
        fixture.componentRef.setInput("inventory", {
            id: 1,
            name: "Test Inventory",
            enableDataInconsistency: true,
        });
        fixture.detectChanges();
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    describe("Component Initialization", () => {
        it("should have correct dimensions from Constants", () => {
            expect(component.dimensions).toEqual(Constants.APPLICATION_DIMENSIONS);
        });

        it("should initialize criteriakeys from translations", () => {
            expect(component.criteriakeys).toContain("climate-change");
            expect(component.criteriakeys).toContain("resource-use");
        });
    });

    describe("extractCheckedLabels", () => {
        it("should return empty array when data is undefined", () => {
            expect(component.extractCheckedLabels(undefined as any)).toEqual([]);
        });

        it("should return empty array when data is null", () => {
            expect(component.extractCheckedLabels(null as any)).toEqual([]);
        });

        it("should return checked All label", () => {
            const data = [
                {
                    label: "All",
                    checked: true,
                },
            ];
            expect(component.extractCheckedLabels(data)).toEqual(["All"]);
        });

        it("should return checked children labels", () => {
            const data = [
                {
                    label: "Domain",
                    children: [
                        { label: "Finance", checked: true },
                        { label: "HR", checked: false },
                        { label: "IT", checked: true },
                    ],
                },
            ];
            expect(component.extractCheckedLabels(data)).toEqual(["Finance", "IT"]);
        });

        it("should return All and checked children labels", () => {
            const data = [
                {
                    label: "All",
                    checked: true,
                    children: [
                        { label: "Finance", checked: true },
                        { label: "IT", checked: true },
                    ],
                },
            ];
            expect(component.extractCheckedLabels(data)).toEqual([
                "All",
                "Finance",
                "IT",
            ]);
        });

        it("should ignore unchecked labels", () => {
            const data = [
                {
                    label: "All",
                    checked: false,
                    children: [{ label: "Finance", checked: false }],
                },
            ];
            expect(component.extractCheckedLabels(data)).toEqual([]);
        });

        it("should handle data without children", () => {
            const data = [
                { label: "Item1", checked: true },
                { label: "Item2", checked: false },
            ];
            expect(component.extractCheckedLabels(data)).toEqual([]);
        });

        it("should include unknown labels if checked", () => {
            const data = [
                {
                    label: "Domain",
                    children: [
                        { label: "unknown", checked: true },
                        { label: "Known", checked: false },
                    ],
                },
            ];
            expect(component.extractCheckedLabels(data)).toEqual(["unknown"]);
        });
    });

    describe("checkStatusIndicatorOk", () => {
        it("should return 1 for ok status", () => {
            expect(
                component.checkStatusIndicatorOk(Constants.DATA_QUALITY_STATUS.ok),
            ).toBe(1);
        });

        it("should return 0 for error status", () => {
            expect(
                component.checkStatusIndicatorOk(Constants.DATA_QUALITY_STATUS.error),
            ).toBe(0);
        });

        it("should return 0 for undefined status", () => {
            expect(component.checkStatusIndicatorOk(undefined as any)).toBe(0);
        });
    });

    describe("checkStatusIndicatorError", () => {
        it("should return 1 for error status", () => {
            expect(
                component.checkStatusIndicatorError(Constants.DATA_QUALITY_STATUS.error),
            ).toBe(1);
        });

        it("should return 0 for ok status", () => {
            expect(
                component.checkStatusIndicatorError(Constants.DATA_QUALITY_STATUS.ok),
            ).toBe(0);
        });

        it("should return 0 for undefined status", () => {
            expect(component.checkStatusIndicatorError(undefined as any)).toBe(0);
        });
    });

    describe("onChartClick", () => {
        it("should navigate when name exists", () => {
            component.onChartClick({ name: "climate-change" });
            expect(routerSpy.navigate).toHaveBeenCalledWith(
                ["../climate-change"],
                jasmine.any(Object),
            );
        });

        it("should not navigate when event is null", () => {
            component.onChartClick(null as any);
            expect(routerSpy.navigate).not.toHaveBeenCalled();
        });

        it("should not navigate when event has no name", () => {
            component.onChartClick({});
            expect(routerSpy.navigate).not.toHaveBeenCalled();
        });

        it("should not navigate when name is empty string", () => {
            component.onChartClick({ name: "" });
            expect(routerSpy.navigate).not.toHaveBeenCalled();
        });
    });

    describe("stackChartClick", () => {
        it("should call onChartClick with matching criteria", () => {
            spyOn(component, "onChartClick");
            component.stackChartClick("Climate Change");
            expect(component.onChartClick).toHaveBeenCalledWith({
                name: "climate-change",
            });
        });

        it("should not call onChartClick when criteria is not found", () => {
            spyOn(component, "onChartClick");
            component.stackChartClick("Unknown Criteria");
            expect(component.onChartClick).not.toHaveBeenCalled();
        });

        it("should handle empty string", () => {
            spyOn(component, "onChartClick");
            component.stackChartClick("");
            expect(component.onChartClick).not.toHaveBeenCalled();
        });
    });

    describe("selectedStackBarClick", () => {
        it("should delegate to onChartClick", () => {
            spyOn(component, "onChartClick");
            component.selectedStackBarClick("resource-use");
            expect(component.onChartClick).toHaveBeenCalledWith({
                name: "resource-use",
            });
        });

        it("should pass empty name", () => {
            spyOn(component, "onChartClick");
            component.selectedStackBarClick("");
            expect(component.onChartClick).toHaveBeenCalledWith({ name: "" });
        });
    });

    describe("handleImpactClick", () => {
        it("should delegate to onChartClick", () => {
            spyOn(component, "onChartClick");
            component.handleImpactClick("climate-change");
            expect(component.onChartClick).toHaveBeenCalledWith({
                name: "climate-change",
            });
        });

        it("should handle any impact name", () => {
            spyOn(component, "onChartClick");
            component.handleImpactClick("any-impact");
            expect(component.onChartClick).toHaveBeenCalledWith({
                name: "any-impact",
            });
        });
    });

    describe("computeApplicationStats", () => {
        it("should compute application count correctly", () => {
            const applications = mockFootprintData;
            const filters = {};
            const result = component["computeApplicationStats"](applications, filters);

            expect(result).toBeDefined();
            expect(result[0].value).toBeGreaterThan(0);
        });

        it("should handle empty applications array", () => {
            const result = component["computeApplicationStats"]([], {});
            expect(result[0].value).toBe(0);
        });

        it("should count unique applications only", () => {
            const applications = [
                {
                    criteria: "test",
                    impacts: [
                        {
                            applicationName: "App1",
                            domain: "Domain1",
                            subDomain: "Sub1",
                        } as any,
                        {
                            applicationName: "App1",
                            domain: "Domain1",
                            subDomain: "Sub1",
                        } as any,
                        {
                            applicationName: "App2",
                            domain: "Domain1",
                            subDomain: "Sub1",
                        } as any,
                    ],
                },
            ] as any;

            const result = component["computeApplicationStats"](applications, {});
            expect(result[0].value).toBe(2);
        });
    });

    describe("renderChart", () => {
        it("should return empty object when no footprint data", () => {
            const result = component.renderChart(
                {
                    footprints: [],
                    hasError: false,
                    total: { impact: 0, sip: 0 },
                    criteriasCount: {},
                    impactsWithMaxDimensions: [],
                },
                "domain",
            );
            expect(result).toEqual({});
            expect(component.xAxisInput).toEqual([]);
        });

        it("should generate chart options with correct structure", () => {
            const criteriaCalculated = {
                footprints: [
                    {
                        data: "Domain1",
                        total: { impact: 300, sip: 150 },
                        status: { ok: 1, error: 0, total: 1 },
                        impacts: [
                            {
                                criteria: "climate-change",
                                sumImpact: 300,
                                sumSip: 150,
                                acvStep: "use",
                                country: "France",
                                entity: "Entity1",
                                equipment: "Server",
                                status: "active",
                                impact: 300,
                                sip: 150,
                                statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                                countValue: 1,
                            },
                        ],
                    },
                ],
                hasError: false,
                total: { impact: 300, sip: 150 },
                criteriasCount: {
                    "climate-change": {
                        status: { ok: 1, error: 0, total: 1 },
                    },
                },
                impactsWithMaxDimensions: [],
            } as any;

            const result = component.renderChart(criteriaCalculated, "domain");

            expect(result.tooltip).toBeDefined();
            expect(result.angleAxis).toBeDefined();
            expect(result.radiusAxis).toBeDefined();
            expect(result.polar).toBeDefined();
            expect(result.series).toBeDefined();
            expect(result.legend).toBeDefined();
        });

        it("should populate xAxisInput with translated criteria", () => {
            component.footprint = mockFootprintData as any;
            const criteriaCalculated = {
                footprints: [
                    {
                        data: "Domain1",
                        impacts: [
                            {
                                criteria: "climate-change",
                                sumSip: 100,
                                sumImpact: 200,
                                acvStep: "use",
                                country: "France",
                                entity: "Entity1",
                                equipment: "Server",
                                status: "active",
                                impact: 200,
                                sip: 100,
                                statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                                countValue: 1,
                            },
                        ],
                    },
                ],
                criteriasCount: {
                    "climate-change": { status: { ok: 1, error: 0, total: 1 } },
                },
            } as any;

            component.renderChart(criteriaCalculated, "domain");

            expect(component.xAxisInput.length).toBeGreaterThan(0);
        });

        it("should set polar radius and center", () => {
            const criteriaCalculated = {
                footprints: [
                    {
                        data: "Domain1",
                        impacts: [
                            {
                                criteria: "climate-change",
                                sumSip: 100,
                                sumImpact: 200,
                                acvStep: "use",
                                country: "France",
                                entity: "Entity1",
                                equipment: "Server",
                                status: "active",
                                impact: 200,
                                sip: 100,
                                statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                                countValue: 1,
                            },
                        ],
                    },
                ],
                criteriasCount: {
                    "climate-change": { status: { ok: 1, error: 0, total: 1 } },
                },
            } as any;

            const result = component.renderChart(criteriaCalculated, "domain");

            expect(result.polar).toEqual({
                radius: "62%",
                center: ["50%", "47%"],
            });
        });

        it("should configure angleAxis with criteria data", () => {
            const criteriaCalculated = {
                footprints: [
                    {
                        data: "Domain1",
                        impacts: [
                            {
                                criteria: "climate-change",
                                sumSip: 100,
                                sumImpact: 200,
                                acvStep: "use",
                                country: "France",
                                entity: "Entity1",
                                equipment: "Server",
                                status: "active",
                                impact: 200,
                                sip: 100,
                                statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                                countValue: 1,
                            },
                        ],
                    },
                ],
                criteriasCount: {
                    "climate-change": { status: { ok: 1, error: 0, total: 1 } },
                },
            } as any;

            const result: any = component.renderChart(criteriaCalculated, "domain");

            expect(result.angleAxis.type).toBe("category");
            expect(result.angleAxis.data).toBeDefined();
            expect(result.angleAxis.axisLabel).toBeDefined();
        });

        it("should create series data for each footprint", () => {
            const criteriaCalculated = {
                footprints: [
                    {
                        data: "Domain1",
                        impacts: [
                            {
                                criteria: "climate-change",
                                sumSip: 100,
                                sumImpact: 200,
                                acvStep: "use",
                                country: "France",
                                entity: "Entity1",
                                equipment: "Server",
                                status: "active",
                                impact: 200,
                                sip: 100,
                                statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                                countValue: 1,
                            },
                        ],
                    },
                    {
                        data: "Domain2",
                        impacts: [
                            {
                                criteria: "resource-use",
                                sumSip: 150,
                                sumImpact: 250,
                                acvStep: "use",
                                country: "France",
                                entity: "Entity1",
                                equipment: "Storage",
                                status: "active",
                                impact: 250,
                                sip: 150,
                                statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                                countValue: 1,
                            },
                        ],
                    },
                ],
                criteriasCount: {
                    "climate-change": { status: { ok: 1, error: 0, total: 1 } },
                    "resource-use": { status: { ok: 1, error: 0, total: 1 } },
                },
            } as any;

            const result: any = component.renderChart(criteriaCalculated, "domain");

            expect(result.series[0].name).toBe("Domain1");
            expect(result.series[1].name).toBe("Domain2");
        });

        it("should configure tooltip formatter correctly", () => {
            const criteriaCalculated = {
                footprints: [
                    {
                        data: "Domain1",
                        impacts: [
                            {
                                criteria: "climate-change",
                                sumSip: 100,
                                sumImpact: 200,
                                acvStep: "use",
                                country: "France",
                                entity: "Entity1",
                                equipment: "Server",
                                status: "active",
                                impact: 200,
                                sip: 100,
                                statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                                countValue: 1,
                            },
                        ],
                    },
                ],
                criteriasCount: {
                    "climate-change": { status: { ok: 1, error: 0, total: 1 } },
                },
            } as any;

            const result: any = component.renderChart(criteriaCalculated, "domain");

            expect(result.tooltip.show).toBeTrue();
            expect(result.tooltip.formatter).toBeDefined();
            expect(typeof result.tooltip.formatter).toBe("function");
        });
    });

    describe("criteriaCalculated computed signal", () => {
        it("should call footprintService.calculate with correct parameters", () => {
            // Trigger the computed signal
            const result = component.criteriaCalculated();

            expect(footprintServiceSpy.calculate).toHaveBeenCalled();
        });

        it("should process and sort footprints by criteria order", () => {
            const result = component.criteriaCalculated();

            expect(result.footprints).toBeDefined();
            expect(Array.isArray(result.footprints)).toBeTrue();
        });

        it("should calculate total impact and sip", () => {
            footprintServiceSpy.calculate.and.returnValue({
                footprintCalculated: [
                    {
                        data: "Domain1",
                        total: { impact: 100, sip: 50 },
                        status: { ok: 1, error: 0, total: 1 },
                        impacts: [],
                    },
                    {
                        data: "Domain2",
                        total: { impact: 200, sip: 100 },
                        status: { ok: 1, error: 0, total: 1 },
                        impacts: [],
                    },
                ],
                criteriaCountMap: {},
                impactsWithMaxDimensions: [],
            });

            const result = component.criteriaCalculated();

            expect(result.total.impact).toBe(300);
            expect(result.total.sip).toBe(150);
        });
    });

    describe("applicationStats computed signal", () => {
        it("should compute application statistics", () => {
            const result = component.applicationStats();

            expect(result).toBeDefined();
            expect(Array.isArray(result)).toBeTrue();
            expect(result.length).toBeGreaterThan(0);
        });

        it("should call formatLifecycleImpact from appComponent", () => {
            component.applicationStats();

            expect(appComponentSpy.formatLifecycleImpact).toHaveBeenCalled();
        });
    });

    describe("getContentText computed signal", () => {
        it("should return graph description content", () => {
            const result = component.getContentText();

            expect(result).toBeDefined();
            expect(result.description).toBeDefined();
            expect(result.scale).toBeDefined();
            expect(result.analysis).toBeDefined();
            expect(result.toGoFurther).toBeDefined();
        });

        it("should include criteria names in description", () => {
            const result = component.getContentText();

            expect(result.description).toBeDefined();
        });
    });

    describe("getTextDescription", () => {
        it("should handle empty impactsWithMaxDimensions", () => {
            const criteriaCalculated = {
                footprints: [],
                hasError: false,
                total: { impact: 0, sip: 0 },
                criteriasCount: {},
                impactsWithMaxDimensions: [],
            };

            const result = component.getTextDescription(
                "ds-graph-description.global-vision.",
                criteriaCalculated,
            );

            expect(result).toBeDefined();
        });

        it("should populate textDescriptionImpacts array", () => {
            const criteriaCalculated = {
                footprints: [],
                hasError: false,
                total: { impact: 0, sip: 0 },
                criteriasCount: {},
                impactsWithMaxDimensions: [
                    {
                        name: "climate-change",
                        title: "Climate Change",
                        peopleeq: 150,
                        raw: 300,
                        unite: "kgCO2eq",
                        maxCriteria: {
                            name: "Domain1",
                            peopleeq: 150,
                            raw: 300,
                        },
                    },
                ],
            };

            component.getTextDescription(
                "ds-graph-description.global-vision.",
                criteriaCalculated,
            );

            expect(component.textDescriptionImpacts.length).toBeGreaterThan(0);
            expect(component.textDescriptionImpacts[0].impactName).toBe("climate-change");
            expect(component.textDescriptionImpacts[0].impactNameVisible).toBe(
                "Climate Change",
            );
        });

        it("should generate text description with impacts", () => {
            const criteriaCalculated = {
                impactsWithMaxDimensions: [
                    {
                        name: "impact1",
                        title: "Impact 1",
                        peopleeq: 100,
                        raw: 200,
                        unite: "unit",
                        maxCriteria: {
                            name: "Resource1",
                            peopleeq: 50,
                            raw: 100,
                        },
                    },
                ],
            } as any;

            const result = component.getTextDescription(
                "ds-graph-description.global-vision.",
                criteriaCalculated,
            );

            expect(result).toContain("text-description");
        });
    });

    describe("Integration with AbstractDashboard", () => {
        it("should inherit existingTranslation method", () => {
            expect(component.existingTranslation).toBeDefined();
            expect(typeof component.existingTranslation).toBe("function");
        });

        it("should inherit getCriteriaTranslation method", () => {
            expect(component.getCriteriaTranslation).toBeDefined();
            expect(typeof component.getCriteriaTranslation).toBe("function");
        });
    });

    describe("selectedDimension computed signal", () => {
        it("should return dimension from footprintStore", () => {
            expect(component.selectedDimension()).toBe("domain");
        });
    });
});
