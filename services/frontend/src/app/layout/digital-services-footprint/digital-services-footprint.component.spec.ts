import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import {
    ChangeDetectorRef
} from "@angular/core";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
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
        },
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
    };

    const mockDigitalServiceStoreService = {
        setDigitalService: jasmine.createSpy(),
        setEnableCalcul: jasmine.createSpy(),
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

    const mockCdr = { detectChanges: jasmine.createSpy() };

    const MockScrollPanel = { refresh: jasmine.createSpy('refresh') };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesFootprintComponent],
            imports: [TranslateModule.forRoot()],
            providers: [
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
                { provide: InDatacentersService, useValue: mockInDatacentersService }
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

    it("should call updateHeights in updateEnableCalculation", (done) => {
        const spy = spyOn(component, "updateHeights");
        component.updateEnableCalculation(true);
        setTimeout(() => {
            expect(spy).toHaveBeenCalled();
            done();
        }, 10);
    });


});
