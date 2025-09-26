import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { ShareDigitalServiceDataService } from "src/app/core/service/data/share-digital-service-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { ShareDigitalServiceComponent } from "./share-digital-service.component";

describe("ShareDigitalServiceComponent", () => {
    let component: ShareDigitalServiceComponent;
    let fixture: ComponentFixture<ShareDigitalServiceComponent>;

    const uid = "ds-123";
    const shareToken = "token-abc";

    const digitalServiceWithCalc: Partial<DigitalService> = {
        uid,
        lastCalculationDate: 1,
    };
    const digitalServiceWithoutCalc: Partial<DigitalService> = {
        uid,
        lastCalculationDate: undefined,
    };

    const mockActivatedRoute = {
        snapshot: {
            paramMap: {
                get: (key: string) => {
                    if (key === "id") return uid;
                    if (key === "share-token") return shareToken;
                    return null;
                },
            },
        },
    };

    const mockTranslate = {
        instant: (k: string) => k,
    };

    const mockDigitalServicesDataService = {
        getDs: jasmine.createSpy("getDs").and.callFake(() => of(digitalServiceWithCalc)),
    };

    const mockShareDigitalServiceDataService = {
        getSharedPhysicalEquipments: jasmine
            .createSpy("getSharedPhysicalEquipments")
            .and.returnValue(of([{ id: 1 }] as any)),
        getSharedVirtualEquipments: jasmine
            .createSpy("getSharedVirtualEquipments")
            .and.returnValue(of([{ id: 2 }] as any)),
        getInSharedDataCenters: jasmine
            .createSpy("getInSharedDataCenters")
            .and.returnValue(of([{ id: 3 }] as any)),
        getReferentialData: jasmine.createSpy("getReferentialData").and.returnValue(
            of({
                networkTypes: [{ code: "NT1" }],
                terminalTypes: [{ code: "TT1" }],
                computeServerTypes: [{ value: "C1" }],
                storageServerTypes: [{ value: "S1" }],
                countries: { FR: "France", US: "United States" },
            }),
        ),
    };

    const mockStore = {
        setInPhysicalEquipments: jasmine.createSpy("setInPhysicalEquipments"),
        setInVirtualEquipments: jasmine.createSpy("setInVirtualEquipments"),
        setInDatacenters: jasmine.createSpy("setInDatacenters"),
        setNetworkTypes: jasmine.createSpy("setNetworkTypes"),
        setTerminalDeviceTypes: jasmine.createSpy("setTerminalDeviceTypes"),
        setServerTypes: jasmine.createSpy("setServerTypes"),
        setCountryMap: jasmine.createSpy("setCountryMap"),
    };

    const mockRouter = {
        navigateByUrl: jasmine.createSpy("navigateByUrl"),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ShareDigitalServiceComponent],
            imports: [TranslateModule.forRoot()],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesDataService,
                },
                {
                    provide: ShareDigitalServiceDataService,
                    useValue: mockShareDigitalServiceDataService,
                },
                { provide: DigitalServiceStoreService, useValue: mockStore },
                { provide: TranslateService, useValue: mockTranslate },
                { provide: Router, useValue: mockRouter },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ShareDigitalServiceComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("initComponent should load digital service & related data and populate store", async () => {
        await component.initComponent();

        // Digital service set
        expect(component.digitalService.uid).toBe(uid);
        expect(mockDigitalServicesDataService.getDs).toHaveBeenCalledWith(
            uid,
            shareToken,
        );

        // Store population
        expect(
            mockShareDigitalServiceDataService.getSharedPhysicalEquipments,
        ).toHaveBeenCalledWith(uid, shareToken);
        expect(mockStore.setInPhysicalEquipments).toHaveBeenCalled();

        expect(
            mockShareDigitalServiceDataService.getSharedVirtualEquipments,
        ).toHaveBeenCalledWith(uid, shareToken);
        expect(mockStore.setInVirtualEquipments).toHaveBeenCalled();

        expect(
            mockShareDigitalServiceDataService.getInSharedDataCenters,
        ).toHaveBeenCalledWith(uid, shareToken);
        expect(mockStore.setInDatacenters).toHaveBeenCalled();

        expect(
            mockShareDigitalServiceDataService.getReferentialData,
        ).toHaveBeenCalledWith(uid, shareToken);
        expect(mockStore.setNetworkTypes).toHaveBeenCalled();
        expect(mockStore.setTerminalDeviceTypes).toHaveBeenCalled();
        expect(mockStore.setServerTypes).toHaveBeenCalled();
        expect(mockStore.setCountryMap).toHaveBeenCalled();

        // Tabs created
        expect(component.tabItems?.length).toBe(2);
        const visualizeTab = component.tabItems?.find((t) => t.id === "visualize");
        expect(visualizeTab?.visible).toBeTrue();
    });

    it("updateTabItems should hide dashboard tab when lastCalculationDate undefined", () => {
        component.digitalService = digitalServiceWithoutCalc as DigitalService;
        component.updateTabItems();
        const visualizeTab = component.tabItems?.find((t) => t.id === "visualize");
        expect(visualizeTab?.visible).toBeFalse();
    });

    it("onMenuTabChange should update selectedTab", () => {
        const event = { id: "resources" };
        component.onMenuTabChange(event);
        expect(component.selectedTab).toEqual(event);
    });

    it("should set visualize tab visible when lastCalculationDate present", () => {
        component.digitalService = digitalServiceWithCalc as DigitalService;
        component.updateTabItems();
        const visualizeTab = component.tabItems?.find((t) => t.id === "visualize");
        expect(visualizeTab?.visible).toBeTrue();
    });

    it("should call initComponent on ngOnInit", () => {
        const spy = spyOn(component as any, "initComponent");

        component.ngOnInit();

        expect(spy).toHaveBeenCalled();
    });
});
