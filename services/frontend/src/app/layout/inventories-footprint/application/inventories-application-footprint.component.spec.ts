import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { UserService } from "src/app/core/service/business/user.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
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
    currentOrganization$: of({ name: "Org1" }),
};

const mockFootprintService = {
    initApplicationFootprint: jasmine.createSpy().and.returnValue(of([])),
    getUniqueValues: jasmine.createSpy().and.returnValue({}),
};

const mockFootprintStore = {
    setApplicationCriteria: jasmine.createSpy(),
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
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [InventoriesApplicationFootprintComponent],
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: UserService, useValue: mockUserService },
                { provide: FootprintService, useValue: mockFootprintService },
                { provide: FootprintStoreService, useValue: mockFootprintStore },
                { provide: GlobalStoreService, useValue: mockGlobalStore },
            ],
        }).compileComponents();
    });

    it("should create the component", () => {
        const fixture = TestBed.createComponent(InventoriesApplicationFootprintComponent);
        const component = fixture.componentInstance;
        expect(component).toBeTruthy();
    });
});
