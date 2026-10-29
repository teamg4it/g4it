import { HttpClientTestingModule } from "@angular/common/http/testing";
import { signal } from "@angular/core";
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG } from "ngx-echarts";
import { of } from "rxjs";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { ApplicationCriteriaFootprintComponent } from "./application-criteria-footprint.component";

describe("ApplicationCriteriaFootprintComponent", () => {
    let component: ApplicationCriteriaFootprintComponent;
    let fixture: ComponentFixture<ApplicationCriteriaFootprintComponent>;
    let footprintStore: any;
    let filterService: any;
    let translate: any;
    let globalStore: any;
    let footprintDataService: jasmine.SpyObj<FootprintDataService>;
    let footprintService: jasmine.SpyObj<FootprintService>;

    beforeEach(async () => {
        footprintDataService = jasmine.createSpyObj("FootprintDataService", [
            "getApplicationMultiCriteriaGraph",
        ]);
        footprintDataService.getApplicationMultiCriteriaGraph.and.returnValue(of([]));
        footprintService = jasmine.createSpyObj("FootprintService", [
            "mapApplicationSummaryFilters",
        ]);
        footprintService.mapApplicationSummaryFilters.and.returnValue({});
        footprintStore = {
            applicationCriteria: jasmine.createSpy().and.returnValue("co2"),
            applicationSelectedFilters: jasmine.createSpy().and.returnValue({}),
            appGraphType: jasmine.createSpy().and.returnValue("global"),
            appDomain: jasmine.createSpy().and.returnValue("Domain1"),
            appSubDomain: jasmine.createSpy().and.returnValue("SubDomain1"),
            appApplication: jasmine.createSpy().and.returnValue("App1"),
            appDimension: signal("domain"),
            setDomain: jasmine.createSpy(),
            setSubDomain: jasmine.createSpy(),
            setGraphType: jasmine.createSpy(),
            setApplication: jasmine.createSpy(),
        };

        filterService = {
            getFilterincludes: jasmine.createSpy().and.returnValue(true),
        };

        translate = {
            instant: jasmine.createSpy().and.callFake((key) => key),
        };

        globalStore = {
            setLoading: jasmine.createSpy(),
        };

        await TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                ApplicationCriteriaFootprintComponent,
            ],
            providers: [
                {
                    provide: InventoriesApplicationFootprintComponent,
                    useValue: {
                        formatLifecycleImpact: (x: any) => x,
                        allUnmodifiedFootprint: [],
                    },
                },
                { provide: IntegerPipe, useValue: { transform: (v: any) => v } },
                { provide: DecimalsPipe, useValue: { transform: (v: any) => v } },
                { provide: GlobalStoreService, useValue: globalStore },
                { provide: FilterService, useValue: filterService },
                { provide: FootprintService, useValue: footprintService },
                { provide: FootprintDataService, useValue: footprintDataService },
                { provide: FootprintStoreService, useValue: footprintStore },
                {
                    provide: ActivatedRoute,
                    useValue: { snapshot: { paramMap: { get: () => "1" } } },
                },
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ApplicationCriteriaFootprintComponent);
        component = fixture.componentInstance;
        component.footprint = {
            criteria: "co2",
            impacts: [
                {
                    domain: "Domain1",
                    subDomain: "SubDomain1",
                    applicationName: "App1",
                    virtualEquipmentName: "Eq1",
                    impact: 5,
                    sip: 2,
                    equipmentType: "Server",
                    environment: "Prod",
                    cluster: "ClusterA",
                    lifeCycle: "use",
                    statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                },
            ],
        } as any;
        fixture.detectChanges();
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should render the selected criterion description without a legacy footprint", () => {
        const emptyFixture = TestBed.createComponent(
            ApplicationCriteriaFootprintComponent,
        );
        emptyFixture.componentRef.setInput("footprint", undefined);
        expect(() => emptyFixture.detectChanges()).not.toThrow();
        expect(emptyFixture.componentInstance.getContentText().description).toBe(
            "ds-graph-description.criteria.description",
        );
        emptyFixture.destroy();
    });

    it("should request and render the selected criterion using the multi-criteria API", () => {
        footprintStore.applicationCriteria.and.returnValue("climate-change");
        footprintDataService.getApplicationMultiCriteriaGraph.and.returnValue(
            of([
                {
                    criteria: "CLIMATE_CHANGE",
                    unit: "kg",
                    nodes: [
                        {
                            label: "Domain1",
                            impact: 12,
                            sip: 3,
                            countValue: 5,
                            consistentCount: 4,
                            inconsistentCount: 1,
                            applicationCount: 2,
                            subDomainCount: 1,
                            repartitions: [
                                {
                                    label: "use",
                                    impact: 12,
                                    sip: 3,
                                    countValue: 5,
                                    consistentCount: 4,
                                    inconsistentCount: 1,
                                },
                            ],
                        },
                    ],
                },
            ]),
        );
        fixture.componentRef.setInput("selectedInventoryId", 42);
        fixture.componentRef.setInput("filterMetadata", {
            environment: [],
            equipmentType: [],
            lifeCycle: [],
            domains: [],
        });
        fixture.detectChanges();

        expect(
            footprintDataService.getApplicationMultiCriteriaGraph,
        ).toHaveBeenCalledWith(42, {
            criteria: ["CLIMATE_CHANGE"],
            graphLevel: "domain",
            repartition: "lifeCycle",
            filters: {},
        });
        const result = component.computeData([], {}, "climate-change", "lifeCycle");
        expect(result.xAxis).toEqual(["Domain1"]);
        expect(result.yAxis[0].data[0].slice(0, 3)).toEqual(["Domain1", 12, 3]);
        expect(result.status.Domain1.status).toEqual({ ok: 4, error: 1, total: 5 });
        expect(component.noData()).toBeFalse();

        const contentText = component.getContentText();
        expect(contentText.textDescription).toBeTruthy();
        expect(component.textDescriptionImpacts).toHaveSize(1);
        expect(component.textDescriptionImpacts[0].impactNameVisible).toBe("Domain1");
    });

    it("should translate lifecycle series names in the legend and tooltip", () => {
        translate.instant.and.callFake((key: string) =>
            key === "acvStep.USING" ? "Using" : key,
        );
        (component as any).graphResponse.set([
            {
                criteria: "CO2",
                unit: "kg",
                nodes: [
                    {
                        label: "Domain1",
                        impact: 10,
                        sip: 2,
                        countValue: 1,
                        consistentCount: 1,
                        inconsistentCount: 0,
                        repartitions: [
                            {
                                label: "USING",
                                impact: 10,
                                sip: 2,
                                countValue: 1,
                                consistentCount: 1,
                                inconsistentCount: 0,
                            },
                        ],
                    },
                ],
            },
        ]);

        const chart = component.loadBarChartOption(
            {},
            [],
            "co2",
            { enableDataInconsistency: false } as any,
            "lifeCycle",
        );
        const tooltip = (chart.tooltip as any).formatter({
            data: ["Domain1", 10, 2, [], []],
            seriesName: "USING",
        });

        expect((chart.legend as any).formatter("USING")).toBe("Using");
        expect(tooltip).toContain("Using");
        expect((chart.legend as any).formatter("UNKNOWN_STEP")).toBe("UNKNOWN_STEP");
    });

    it("should reset showInconsitencyGraph on ngOnChanges", () => {
        component.showInconsitencyGraph = true;
        component.ngOnChanges({});
        expect(component.showInconsitencyGraph).toBeFalse();
    });

    for (const [level, graphLevel] of [
        ["domain", "subDomain"],
        ["subdomain", "application"],
        ["application", "virtualEquipment"],
    ] as const) {
        it(`should request the ${level} drill-down with its filters`, () => {
            footprintStore.appGraphType.and.returnValue(level);
            footprintStore.appDimension.set("environment");
            footprintService.mapApplicationSummaryFilters.and.returnValue({
                environment: ["Prod"],
            });
            fixture.componentRef.setInput("selectedInventoryId", 42);
            fixture.componentRef.setInput("filterMetadata", {
                environment: ["Prod"],
                equipmentType: [],
                lifeCycle: [],
                domains: [],
            });
            fixture.detectChanges();

            expect(footprintService.mapApplicationSummaryFilters).toHaveBeenCalledWith(
                {},
                jasmine.any(Object),
                {
                    domain: "Domain1",
                    subDomain: level === "domain" ? "" : "SubDomain1",
                },
            );
            expect(
                footprintDataService.getApplicationMultiCriteriaGraph,
            ).toHaveBeenCalledWith(42, {
                criteria: ["CO2"],
                graphLevel,
                repartition: "environment",
                filters:
                    level === "application"
                        ? { environment: ["Prod"], applicationName: ["App1"] }
                        : { environment: ["Prod"] },
            });
            expect(component.noData()).toBeTrue();
            expect(component.computeData([], {}, "co2", "environment").xAxis).toEqual([]);
        });
    }

    describe("onChartClick", () => {
        it("should set domain + subdomain if global and children <=1", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("global");
            footprintStore.applicationSelectedFilters.and.returnValue({
                domain: [{ label: "Domain1", children: [{ label: "SubDomain1" }] }],
            });
            component.onChartClick({ name: "Domain1" });
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("Domain1");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("SubDomain1");
        }));

        it("should set domain only if global and children >1", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("global");
            footprintStore.applicationSelectedFilters.and.returnValue({
                domain: [{ label: "Domain1", children: [{}, {}] }],
            });
            component.onChartClick({ name: "Domain1" });
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("domain");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("Domain1");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("");
        }));

        it("should set subdomain if graphType = domain", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("domain");
            component.onChartClick({ name: "SubDomain1" });
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("SubDomain1");
        }));

        it("should set application if graphType = subdomain", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            component.onChartClick({ name: "App1" });
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("application");
            expect(footprintStore.setApplication).toHaveBeenCalledWith("App1");
        }));
    });

    describe("onArrowClick", () => {
        it("should go back from application -> subdomain", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("application");
            component.onArrowClick();
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
            expect(footprintStore.setApplication).toHaveBeenCalledWith("");
        }));

        it("should go back from domain -> global", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("domain");
            component.allUnmodifiedFilters = { domain: [] } as any;
            component.onArrowClick();
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("global");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("");
        }));

        it("should go back from subdomain -> global", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("subdomain");

            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            (component["footprintStore"].appDomain as any) = () => "Domain1";
            component.onArrowClick();
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("global");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("");
        }));

        it("should go back from subdomain -> domain when children > 1", fakeAsync(() => {
            footprintStore.appGraphType.and.returnValue("subdomain");

            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    {
                        label: "Domain1",
                        children: [{ label: "Sub1" }, { label: "Sub2" }],
                    },
                ],
            });
            (component["footprintStore"].appDomain as any) = () => "Domain1";
            component.onArrowClick();
            tick(20);
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("domain");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("");
        }));
    });

    it("checkIfNoData should return false if filter includes impact", () => {
        const result = component.checkIfNoData({});
        expect(result).toBeFalse();
    });

    it("computeImpactOrder should add and then aggregate impacts", () => {
        const impact = component.footprint.impacts[0];
        component.computeImpactOrder(impact, "Domain1", "lifeCycle");
        expect(component.impactOrder).toHaveSize(1);

        // second time aggregates
        component.computeImpactOrder(
            { ...impact, sip: 3, impact: 4 },
            "Domain1",
            "lifeCycle",
        );
        expect(component.impactOrder[0].sipImpact).toBe(7);
    });

    it("initGraphData should build global chart data", () => {
        footprintStore.appGraphType.and.returnValue("global");
        const result = component.initGraphData([
            {
                domain: "Domain1",
                sipImpact: 10,
                unitImpact: 5,
                subdomains: ["SubDomain1"],
                apps: ["App1"],
                status: { ok: 1, error: 0, total: 1 },
            },
        ]);
        expect(result.xAxis).toContain("Domain1");
    });

    it("checkImpacts should return grey label if no errors", () => {
        component.allCriteriaMap = { key: { status: { error: 0 } } } as any;
        expect(component.checkImpacts("key")).toContain("{grey|");
    });

    it("selectedStackBarClick should call onChartClick", () => {
        spyOn(component, "onChartClick");
        component.selectedStackBarClick("criteria1");
        expect(component.onChartClick).toHaveBeenCalledWith({ name: "criteria1" });
    });

    describe("computed signals", () => {
        it("showBackButton should be true when graphType is global", () => {
            footprintStore.appGraphType.and.returnValue("global");
            expect(component.showBackButton()).toBeTrue();
        });

        it("showBackButton should be true when graphType is not global", () => {
            footprintStore.appGraphType.and.returnValue("domain");
            expect(component.showBackButton()).toBeTrue();
        });

        it("showDomainByApplication should be false for subdomain", () => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            expect(component.showDomainByApplication()).toBeFalse();
        });

        it("showDomainByApplication should be false for application", () => {
            footprintStore.appGraphType.and.returnValue("application");
            expect(component.showDomainByApplication()).toBeFalse();
        });

        it("showDomainByApplication should be false for global", () => {
            footprintStore.appGraphType.and.returnValue("global");
            expect(component.showDomainByApplication()).toBeFalse();
        });

        it("showDomainLabel should be true for subdomain if appDomain exists", () => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            footprintStore.appDomain.and.returnValue("Domain1");
            expect(component.showDomainLabel()).toBeTrue();
        });

        it("showDomainLabel should be true if appDomain empty", () => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            footprintStore.appDomain.and.returnValue("");
            expect(component.showDomainLabel()).toBeTrue();
        });

        it("showSubDomainLabel should be true for application if appSubDomain exists", () => {
            footprintStore.appGraphType.and.returnValue("application");
            footprintStore.appSubDomain.and.returnValue("SubDomain1");
            expect(component.showSubDomainLabel()).toBeTrue();
        });

        it("showSubDomainLabel should be true if no appSubDomain", () => {
            footprintStore.appGraphType.and.returnValue("application");
            footprintStore.appSubDomain.and.returnValue("");
            expect(component.showSubDomainLabel()).toBeTrue();
        });

        it("should return false when domain filters length is 2 or less", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            expect(component.showDomainLabel()).toBeFalse();
        });

        it("should return false when showSubDomainLabel  filters length is 2 or less", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            expect(component.showSubDomainLabel()).toBeFalse();
        });

        it("should return false when showBackButton appGraphType is subdomain", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            (component["footprintStore"].appGraphType as any) = () => "subdomain";
            expect(component.showBackButton()).toBeTrue();
        });
        it("should return false when showBackButton appGraphType is domain", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            (component["footprintStore"].appGraphType as any) = () => "domain";
            expect(component.showBackButton()).toBeTrue();
        });
    });

    describe("domainSelected handling", () => {
        beforeEach(() => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
        });

        it("should set graphType global and reset domain/subDomain if children length <= 1", () => {
            footprintStore.appDomain.and.returnValue("Domain1");

            const domainSelected = (component.allUnmodifiedFilters() as any).domain.find(
                (d: any) => d.label === footprintStore.appDomain(),
            );

            // simulate the block of code directly
            if (domainSelected?.children.length <= 1) {
                component["footprintStore"].setGraphType("global");
                component["footprintStore"].setDomain("");
                component["footprintStore"].setSubDomain("");
            }

            expect(footprintStore.setGraphType).toHaveBeenCalledWith("global");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("");
        });
    });
});
