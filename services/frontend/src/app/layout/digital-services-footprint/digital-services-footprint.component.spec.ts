import { ChangeDetectorRef } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, convertToParamMap } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServicesFootprintComponent } from "./digital-services-footprint.component";

describe("DigitalServicesFootprintComponent", () => {
    let component: DigitalServicesFootprintComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintComponent>;

    const mockRoute = {
        snapshot: {
            paramMap: {
                get: (key: string) => "test-uid",
            },
            queryParamMap: {
                get: (key: string) => null,
            },
        },
        paramMap: of(
            convertToParamMap({
                digitalServiceVersionId: "ABC-123",
            }),
        ),
    };

    const mockDigitalService = {
        uid: "test-uid",
        isAi: false,
        lastCalculationDate: new Date(),
    };

    const mockDigitalServicesDataService = {
        get: () => of(mockDigitalService),
        getNetworkReferential: () => of([]),
        getDeviceReferential: () => of([]),
        getHostServerReferential: () => of([]),
        update: () => of(mockDigitalService),
        digitalService$: of(mockDigitalService),
        getServiceRenewalDetails: () => of(null),
        getDuplicateDigitalServiceAndVersionName: () =>
            of({ dsNames: [], versionNames: [] }),
    };

    const mockDigitalServiceStoreService = {
        setDigitalService: jasmine.createSpy(),
        setEnableCalcul: jasmine.createSpy(),
        setEcoMindEnableCalcul: jasmine.createSpy(),
        initInPhysicalEquipments: jasmine.createSpy(),
        initInVirtualEquipments: jasmine.createSpy(),
        setInDatacenters: jasmine.createSpy(),
        setNetworkTypes: jasmine.createSpy(),
        setTerminalDeviceTypes: jasmine.createSpy(),
        setServerTypes: jasmine.createSpy(),
    };

    const mockDigitalServiceBusinessService = {
        initCountryMap: jasmine.createSpy(),
    };

    const mockGlobal = {
        setLoading: jasmine.createSpy(),
    };

    const mockInDatacentersService = {
        get: () => of([]),
        create: () => of({}),
    };

    const mockUserService = {
        currentOrganization$: of({
            name: "Test Org",
            id: "test-org-id",
            ecomindai: false,
        }),
        currentWorkspace$: of({ name: "Test Workspace" }),
        user$: of({ email: "test@example.com" }),
    };

    const mockInventoryDataService = {
        getServiceRenewalDetails: () => of(null),
    };

    const mockAIFormsStore = {
        setParameterChange: jasmine.createSpy(),
        setInfrastructureChange: jasmine.createSpy(),
        clearForms: jasmine.createSpy(),
    };

    const mockCdr = { detectChanges: jasmine.createSpy() };

    const MockScrollPanel = { refresh: jasmine.createSpy("refresh") };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), DigitalServicesFootprintComponent],
            providers: [
                MessageService,
                { provide: ActivatedRoute, useValue: mockRoute },
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesDataService,
                },
                {
                    provide: DigitalServiceStoreService,
                    useValue: mockDigitalServiceStoreService,
                },
                {
                    provide: DigitalServiceBusinessService,
                    useValue: mockDigitalServiceBusinessService,
                },
                { provide: ChangeDetectorRef, useValue: mockCdr },
                { provide: GlobalStoreService, useValue: mockGlobal },
                { provide: InDatacentersService, useValue: mockInDatacentersService },
                { provide: UserService, useValue: mockUserService },
                { provide: InventoryDataService, useValue: mockInventoryDataService },
                { provide: AIFormsStore, useValue: mockAIFormsStore },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesFootprintComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should set tabItems for EcoMind AI", () => {
        component.isEcoMindAi = true;
        component.digitalService = { lastCalculationDate: new Date() } as any;
        component.updateTabItems();
        expect(component.tabItems?.length).toBe(2);
    });

    it("should set tabItems for normal service", () => {
        component.isEcoMindAi = false;
        component.digitalService = { lastCalculationDate: new Date() } as any;
        component.updateTabItems();
        expect(component.tabItems?.length).toBe(2);
        expect(component.tabItems?.find((item) => item.id === "resources")).toBeDefined();
    });

    it("should call asyncInit() with the UID from route params", () => {
        const asyncInitSpy = spyOn(component as any, "asyncInit").and.returnValue(
            Promise.resolve(),
        );

        component.ngOnInit();

        expect(asyncInitSpy).toHaveBeenCalledWith("ABC-123");
    });
});
