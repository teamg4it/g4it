import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
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
    getApplicationKeyIndicators: jasmine.createSpy().and.returnValue(of([[], [], []])),
};

const mockFootprintService = {
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
    setApplicationCriteria: jasmine.createSpy(),
    setDomain: jasmine.createSpy(),
    setSubDomain: jasmine.createSpy(),
    setGraphType: jasmine.createSpy(),
    appGraphType: jasmine.createSpy().and.returnValue("global"),
    appDomain: jasmine.createSpy().and.returnValue(""),
    appSubDomain: jasmine.createSpy().and.returnValue(""),
    applicationCriteria: jasmine.createSpy().and.returnValue("someCriteria"),
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
            declarations: [InventoriesApplicationFootprintComponent],
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
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
        mockFootprintService.getUniqueValues.and.returnValue({
            domain: testDomainFilter.slice(1),
            lifeCycle: [],
            equipmentType: [],
            environment: [],
            location: [],
        });

        await component["initializeOnInit"]();

        expect(mockFootprintStore.setDomain).toHaveBeenCalledWith("Domain1");
        expect(mockFootprintStore.setSubDomain).toHaveBeenCalledWith("SubDomain1");
        expect(mockFootprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
    });

    it("should set graphType global when domain length >2", async () => {
        spyOn(component.allUnmodifiedFilters, "set").and.callFake(() => {});
        spyOn(component, "allUnmodifiedFilters").and.returnValue({
            domain: [{ label: "ALL" }, { label: "Domain1" }, { label: "Domain2" }],
        });
        mockFootprintService.getUniqueValues.and.returnValue({
            domain: [],
            lifeCycle: [],
            equipmentType: [],
            environment: [],
            location: [],
        });
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
        mockFootprintService.getUniqueValues.and.returnValue({
            domain: [],
            lifeCycle: [],
            equipmentType: [],
            environment: [],
            location: [],
        });
        await component["initializeOnInit"]();

        expect(mockFootprintStore.setDomain).toHaveBeenCalledWith("Domain1");
        expect(mockFootprintStore.setSubDomain).toHaveBeenCalledWith("");
        expect(mockFootprintStore.setGraphType).toHaveBeenCalledWith("domain");
    });
});
