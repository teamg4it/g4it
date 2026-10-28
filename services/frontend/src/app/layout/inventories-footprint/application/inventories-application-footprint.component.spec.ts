import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA, signal } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of, Subject } from "rxjs";
import { Filter } from "src/app/core/interfaces/filter.interface";
import { ApplicationMultiCriteriaImpact } from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { InventoriesApplicationFootprintComponent } from "./inventories-application-footprint.component";

const mockTranslateService = {
    currentLang: "en",
    instant: jasmine.createSpy("instant").and.callFake((key: string) => key),
    get: jasmine.createSpy("get").and.callFake((key: string) => of(key)),
    getParsedResult: jasmine
        .createSpy("getParsedResult")
        .and.callFake(
            (translations: Record<string, string>, key: string) =>
                translations?.[key] ?? key,
        ),
    onTranslationChange: of({ lang: "en", translations: {} }),
    onLangChange: of({ lang: "en", translations: {} }),
    onDefaultLangChange: of({ lang: "en", translations: {} }),
    translations: {
        en: {
            criteria: {
                someCriteria: { title: "Some Title", unite: "unit" },
            },
        },
    },
};

const mockActivatedRoute = {
    snapshot: {
        paramMap: {
            get: jasmine.createSpy().and.callFake((key: string) => {
                if (key === "inventoryId") return "1";
                if (key === "criteria") return "someCriteria";
                return null;
            }),
        },
    },
    paramMap: {
        subscribe: jasmine.createSpy("subscribe"),
    },
};

const mockUserService = {
    currentWorkspace$: of({ name: "Org1" }),
    currentOrganization$: of({ criteria: [] }),
};

const mockInventoryService = {
    getInventories: jasmine.createSpy().and.returnValue(
        Promise.resolve([
            {
                id: 1,
                name: "Test Inventory",
                criteria: [],
                enableDataInconsistency: false,
            },
        ]),
    ),
};

const mockFootprintDataService = {
    getApplicationMultiCriteriaImpacts: jasmine.createSpy().and.returnValue(of([])),
    getApplicationMultiCriteriaGraph: jasmine.createSpy().and.returnValue(of([])),
    getApplicationKeyIndicators: jasmine.createSpy().and.returnValue(of([[], [], []])),
    getApplicationFilters: jasmine
        .createSpy()
        .and.returnValue(
            of({ environment: [], equipmentType: [], lifeCycle: [], domains: [] }),
        ),
};

const mockFootprintService = {
    mapApplicationSummaryFilters: jasmine.createSpy().and.returnValue({}),
    initApplicationFootprint: jasmine.createSpy().and.returnValue(of([])),
    getUniqueValues: jasmine.createSpy().and.returnValue({
        domain: [],
        lifeCycle: [],
        equipmentType: [],
        environment: [],
        location: [],
    }),
};

const mockFootprintStore = {
    applicationSelectedFilters: jasmine.createSpy().and.returnValue({}),
    setApplicationSelectedFilters: jasmine.createSpy(),
    setApplicationCriteria: jasmine.createSpy(),
    setDomain: jasmine.createSpy(),
    setSubDomain: jasmine.createSpy(),
    setGraphType: jasmine.createSpy(),
    appGraphType: jasmine.createSpy().and.returnValue("global"),
    appDomain: jasmine.createSpy().and.returnValue(""),
    appSubDomain: jasmine.createSpy().and.returnValue(""),
    appApplication: jasmine.createSpy().and.returnValue(""),
    applicationCriteria: jasmine.createSpy().and.returnValue("someCriteria"),
    appDimension: jasmine.createSpy().and.returnValue("lifeCycle"),
};

const mockGlobalStore = {
    criteriaList: jasmine.createSpy().and.returnValue({}),
    setLoading: jasmine.createSpy(),
};
describe("Inventory Application footprint", () => {
    let component: InventoriesApplicationFootprintComponent;
    let fixture: ComponentFixture<InventoriesApplicationFootprintComponent>;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                InventoriesApplicationFootprintComponent,
            ],
            schemas: [NO_ERRORS_SCHEMA],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: UserService, useValue: mockUserService },
                { provide: InventoryService, useValue: mockInventoryService },
                { provide: FootprintDataService, useValue: mockFootprintDataService },
                { provide: FootprintService, useValue: mockFootprintService },
                { provide: FootprintStoreService, useValue: mockFootprintStore },
                { provide: GlobalStoreService, useValue: mockGlobalStore },
                DecimalsPipe,
                IntegerPipe,
            ],
        }).compileComponents();
        fixture = TestBed.createComponent(InventoriesApplicationFootprintComponent);
        component = fixture.componentInstance;
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should translate canonical and legacy lifecycle filter values", () => {
        mockTranslateService.instant.and.callFake((key: string) =>
            key.startsWith("acvStep.") ? key.replace("acvStep.", "Translated ") : key,
        );
        const lifecycleMap = new Map([
            ["FABRICATION", "MANUFACTURING"],
            ["DISTRIBUTION", "TRANSPORTATION"],
            ["UTILISATION", "USING"],
            ["FIN_DE_VIE", "END_OF_LIFE"],
        ]);
        const canonicalValues = [
            "END_OF_LIFE",
            "MANUFACTURING",
            "TRANSPORTATION",
            "USING",
        ];

        for (const value of canonicalValues) {
            expect(component["mapLifecycle"](value, lifecycleMap)).toBe(
                `Translated ${value}`,
            );
            expect(mockTranslateService.instant).toHaveBeenCalledWith(`acvStep.${value}`);
        }

        expect(component["mapLifecycle"]("FABRICATION", lifecycleMap)).toBe(
            "Translated MANUFACTURING",
        );
        mockTranslateService.instant.and.callFake((key: string) => key);
    });

    describe("sidebar summaries", () => {
        const metadata = {
            environment: ["PRODUCTION"],
            equipmentType: [],
            lifeCycle: [],
            domains: [],
        };
        const impacts: ApplicationMultiCriteriaImpact[] = [
            {
                criteria: "someCriteria",
                unit: "kg",
                impact: 42,
                sip: 7,
                countValue: 3,
            },
        ];
        let response: Subject<ApplicationMultiCriteriaImpact[]>;

        beforeEach(() => {
            response = new Subject<ApplicationMultiCriteriaImpact[]>();
            mockFootprintDataService.getApplicationMultiCriteriaImpacts.calls.reset();
            mockFootprintDataService.getApplicationMultiCriteriaImpacts.and.returnValue(
                response,
            );
            mockFootprintService.mapApplicationSummaryFilters.and.returnValue({
                environment: ["PRODUCTION"],
                equipmentType: [],
                lifeCycle: [],
                domain: [],
                subDomain: [],
            });
        });

        afterEach(() => {
            fixture.destroy();
            mockFootprintDataService.getApplicationMultiCriteriaImpacts.and.returnValue(
                of([]),
            );
            mockFootprintService.mapApplicationSummaryFilters.and.returnValue({});
        });

        function loadSummary() {
            component["filterMetadata"].set({ ...metadata });
            TestBed.tick();
        }

        it("uses returned totals directly", () => {
            loadSummary();
            expect(
                mockFootprintDataService.getApplicationMultiCriteriaImpacts,
            ).toHaveBeenCalledWith(1, {});
            response.next(impacts);
            response.complete();
            expect(component.impacts()).toEqual([
                {
                    name: "someCriteria",
                    title: "criteria.someCriteria.title",
                    unite: "kg",
                    raw: 42,
                    peopleeq: 7,
                },
            ]);
        });

        it("shows the loader while the summary request is pending", () => {
            mockGlobalStore.setLoading.calls.reset();
            loadSummary();
            expect(mockGlobalStore.setLoading).toHaveBeenCalledWith(true);
            response.next(impacts);
            response.complete();
            expect(mockGlobalStore.setLoading.calls.mostRecent().args).toEqual([false]);
        });

        it("hides the loader when the summary request fails", () => {
            loadSummary();
            mockGlobalStore.setLoading.calls.reset();
            response.error(new Error("failed request"));
            expect(mockGlobalStore.setLoading).toHaveBeenCalledOnceWith(false);
        });

        it("keeps the loader while a newer summary request replaces a pending one", () => {
            loadSummary();
            mockFootprintDataService.getApplicationMultiCriteriaImpacts.and.returnValue(
                new Subject<ApplicationMultiCriteriaImpact[]>(),
            );
            mockGlobalStore.setLoading.calls.reset();
            loadSummary();
            expect(mockGlobalStore.setLoading).toHaveBeenCalledOnceWith(true);
        });

        it("waits for the selected filters before requesting the summary", () => {
            const selectedFilters = signal<Filter>({});
            mockFootprintStore.applicationSelectedFilters.and.callFake(() =>
                selectedFilters(),
            );
            component.allUnmodifiedFilters.set({ environment: ["All", "PRODUCTION"] });

            loadSummary();
            expect(
                mockFootprintDataService.getApplicationMultiCriteriaImpacts,
            ).not.toHaveBeenCalled();

            selectedFilters.set({ environment: ["All", "PRODUCTION"] });
            TestBed.tick();
            expect(
                mockFootprintDataService.getApplicationMultiCriteriaImpacts,
            ).toHaveBeenCalledTimes(1);
            expect(
                mockFootprintDataService.getApplicationMultiCriteriaImpacts,
            ).toHaveBeenCalledWith(1, {});
            mockFootprintStore.applicationSelectedFilters.and.returnValue({});
        });

        it("sends the full mapped filter object after its initial request", () => {
            loadSummary();
            loadSummary();
            expect(
                mockFootprintDataService.getApplicationMultiCriteriaImpacts,
            ).toHaveBeenCalledTimes(2);
            expect(
                mockFootprintDataService.getApplicationMultiCriteriaImpacts,
            ).toHaveBeenCalledWith(1, {
                environment: ["PRODUCTION"],
                equipmentType: [],
                lifeCycle: [],
                domain: [],
                subDomain: [],
            });
        });

        it("preserves successful totals during replacement and on failure", () => {
            component.summaryImpacts.set(impacts);
            loadSummary();
            expect(component.summaryImpacts()).toEqual(impacts);
            response.error(new Error("failed request"));
            expect(component.summaryImpacts()).toEqual(impacts);
            expect(component.summaryError()).toBeTrue();
        });

        it("cancels stale requests when metadata refreshes", () => {
            loadSummary();
            const newerResponse = new Subject<ApplicationMultiCriteriaImpact[]>();
            mockFootprintDataService.getApplicationMultiCriteriaImpacts.and.returnValue(
                newerResponse,
            );
            loadSummary();
            newerResponse.next(impacts);
            response.next([{ ...impacts[0], impact: 999 }]);
            expect(component.summaryImpacts()).toEqual(impacts);
            expect(
                mockFootprintDataService.getApplicationMultiCriteriaImpacts,
            ).toHaveBeenCalledTimes(2);
        });

        it("clears previous totals on a successful empty response", () => {
            component.summaryImpacts.set(impacts);
            loadSummary();
            response.next([]);
            response.complete();
            expect(component.impacts()).toEqual([]);
            expect(component.summaryError()).toBeFalse();
        });
    });

    it('should set graph type to "subdomain" if domain has one child', async () => {
        const testDomainFilter = [
            Constants.ALL,
            {
                label: "Domain1",
                checked: true,
                visible: true,
                children: [{ label: "SubDomain1", checked: true, visible: true }],
            },
        ];
        mockFootprintService.initApplicationFootprint.and.returnValue(of([]));
        mockFootprintDataService.getApplicationFilters.and.returnValue(
            of({
                environment: [],
                equipmentType: [],
                lifeCycle: [],
                domains: [{ domain: "Domain1", subDomains: ["SubDomain1"] }],
            }),
        );

        await component["initializeOnInit"]();

        expect(mockFootprintDataService.getApplicationFilters).toHaveBeenCalledWith(1);
        expect(mockFootprintStore.setDomain).toHaveBeenCalledWith("Domain1");
        expect(mockFootprintStore.setSubDomain).toHaveBeenCalledWith("SubDomain1");
        expect(mockFootprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
    });

    it("should set graphType global when domain length >2", async () => {
        spyOn(component.allUnmodifiedFilters, "set").and.callFake(() => {});
        spyOn(component, "allUnmodifiedFilters").and.returnValue({
            domain: [{ label: "ALL" }, { label: "Domain1" }, { label: "Domain2" }],
        });
        mockFootprintDataService.getApplicationFilters.and.returnValue(
            of({
                environment: [],
                equipmentType: [],
                lifeCycle: [],
                domains: [
                    { domain: "Domain1", subDomains: ["Child1", "Child2"] },
                    { domain: "Domain2", subDomains: [] },
                ],
            }),
        );
        await component["initializeOnInit"]();

        expect(mockFootprintStore.setGraphType).toHaveBeenCalledWith("global");
    });

    it("should set domain when domain length <=2 and children length >1", async () => {
        spyOn(component.allUnmodifiedFilters, "set").and.callFake(() => {});
        spyOn(component, "allUnmodifiedFilters").and.returnValue({
            domain: [
                { label: "ALL" },
                {
                    label: "Domain1",
                    children: [{ label: "Child1" }, { label: "Child2" }],
                },
            ],
        });
        mockFootprintDataService.getApplicationFilters.and.returnValue(
            of({
                environment: [],
                equipmentType: [],
                lifeCycle: [],
                domains: [{ domain: "Domain1", subDomains: ["Child1", "Child2"] }],
            }),
        );
        await component["initializeOnInit"]();

        expect(mockFootprintStore.setDomain).toHaveBeenCalledWith("Domain1");
        expect(mockFootprintStore.setSubDomain).toHaveBeenCalledWith("");
        expect(mockFootprintStore.setGraphType).toHaveBeenCalledWith("domain");
    });
});
