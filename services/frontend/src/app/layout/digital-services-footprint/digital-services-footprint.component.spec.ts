import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
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

    const mockTranslate = {
        instant: (key: string) => key,
    };

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
                { provide: GlobalStoreService, useValue: mockGlobal },
                { provide: InDatacentersService, useValue: mockInDatacentersService },
                { provide: TranslateService, useValue: mockTranslate },
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

    it("should update header and footer heights", () => {
        const mockHeader = { nativeElement: { offsetHeight: 100 } };
        const mockFooter = { nativeElement: { offsetHeight: 50 } };
        component.headerRef = mockHeader as any;
        component.footerRef = mockFooter as any;

        component.updateHeights();
        expect(component.headerHeight).toBe(100);
        expect(component.footerHeight).toBe(50);
    });

    it("should update header and footer heights", () => {
        const mockHeader = { nativeElement: { offsetHeight: 100 } };
        const mockFooter = { nativeElement: { offsetHeight: 50 } };
        component.headerRef = mockHeader as any;
        component.footerRef = mockFooter as any;

        component.updateHeights();
        expect(component.headerHeight).toBe(100);
        expect(component.footerHeight).toBe(50);
    });

    it("should call updateHeights in updateEnableCalculation", (done) => {
        const spy = spyOn(component, "updateHeights");
        component.updateEnableCalculation(true);
        setTimeout(() => {
            expect(spy).toHaveBeenCalled();
            done();
        }, 10);
    });

    it("should attach and detach resize event listener", () => {
        const addSpy = spyOn(window, "addEventListener");
        const removeSpy = spyOn(window, "removeEventListener");

        component.ngAfterViewInit();
        expect(addSpy).toHaveBeenCalledWith("resize", jasmine.any(Function));

        component.ngOnDestroy();
        expect(removeSpy).toHaveBeenCalledWith("resize", jasmine.any(Function));
    });
});
