import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { InputTextModule } from "primeng/inputtext";
import { of } from "rxjs";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { OutPhysicalEquipmentsService } from "src/app/core/service/data/in-out/out-physical-equipments.service";
import { OutVirtualEquipmentsService } from "src/app/core/service/data/in-out/out-virtual-equipments.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard.component";

fdescribe("DigitalServicesFootprintDashboardComponent", () => {
    let component: DigitalServicesFootprintDashboardComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintDashboardComponent>;

    // Mock services
    const digitalServicesDataServiceMock = {
        digitalService$: of({
            name: "Test Digital Service",
            uid: "test-uid",
            isAi: false,
            creationDate: Date.now(),
            lastUpdateDate: Date.now(),
            lastCalculationDate: null,
            terminals: [],
            servers: [],
            networks: [],
        }),
        get: jasmine.createSpy("get").and.returnValue(of({})),
    };

    const digitalServiceBusinessServiceMock = {
        updateDsCriteria: jasmine.createSpy("updateDsCriteria").and.returnValue(of({})),
        triggerLaunchCalcul: jasmine.createSpy("triggerLaunchCalcul"),
    };

    const userServiceMock = {
        currentSubscriber$: of({ name: "SubName", id: 1 }),
        currentOrganization$: of({
            name: "OrgName",
            id: 2,
            status: "active",
            dataRetentionDays: 30,
            criteriaDs: [],
            criteriaIs: [],
        }),
    };

    const outPhysicalEquipmentsServiceMock = {
        get: jasmine.createSpy("get").and.returnValue(of([])),
    };

    const outVirtualEquipmentsServiceMock = {
        getByDigitalService: jasmine
            .createSpy("getByDigitalService")
            .and.returnValue(of([])),
    };

    const digitalServicesAiDataServiceMock = {
        getAiRecommendations: jasmine
            .createSpy("getAiRecommendations")
            .and.returnValue(of({ recommendations: "[]" })),
    };

    const digitalServiceStoreMock = {
        countryMap: jasmine.createSpy("countryMap").and.returnValue({}),
        networkTypes: jasmine.createSpy("networkTypes").and.returnValue([]),
        serverTypes: jasmine.createSpy("serverTypes").and.returnValue([]),
        terminalDeviceTypes: jasmine.createSpy("terminalDeviceTypes").and.returnValue([]),
        setEnableCalcul: jasmine.createSpy("setEnableCalcul"),
        criteriaList: jasmine
            .createSpy("criteriaList")
            .and.returnValue({ CO2: {}, WATER: {} }),
    };

    const globalStoreMock = {
        criteriaList: jasmine
            .createSpy("criteriaList")
            .and.returnValue({ CO2: {}, WATER: {} }),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                DigitalServicesFootprintDashboardComponent,
                IntegerPipe,
                DecimalsPipe,
            ],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                FormsModule,
                InputTextModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServicesDataServiceMock,
                },
                {
                    provide: DigitalServiceBusinessService,
                    useValue: digitalServiceBusinessServiceMock,
                },
                { provide: UserService, useValue: userServiceMock },
                {
                    provide: OutPhysicalEquipmentsService,
                    useValue: outPhysicalEquipmentsServiceMock,
                },
                {
                    provide: OutVirtualEquipmentsService,
                    useValue: outVirtualEquipmentsServiceMock,
                },
                {
                    provide: DigitalServicesAiDataService,
                    useValue: digitalServicesAiDataServiceMock,
                },
                {
                    provide: DigitalServiceStoreService,
                    useValue: digitalServiceStoreMock,
                },
                { provide: GlobalStoreService, useValue: globalStoreMock },
                TranslateService,
                IntegerPipe,
                DecimalsPipe,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesFootprintDashboardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize digitalService and organization on ngOnInit", async () => {
        await component.ngOnInit();
        expect(component.digitalService.name).toBe("Test Digital Service");
        expect(component.organization.organizationName).toBe("OrgName");
        expect(component.organization.subscriberName).toBe("SubName");
    });

    it("should call retrieveFootprintData on ngOnInit", async () => {
        spyOn(component, "retrieveFootprintData");
        await component.ngOnInit();
        expect(component.retrieveFootprintData).toHaveBeenCalled();
    });

    // it("should set onlyOneCriteria and selectedCriteria if impacts length is 1", () => {
    //     component.impacts = [{ name: "CO2" }];
    //     component.ngOnInit();
    //     expect(component.onlyOneCriteria).toBeTrue();
    //     expect(component.selectedCriteria).toBe("CO2");
    // });

    // it("should handle chart change logic", () => {
    //     component.selectedCriteria = "Global Vision";
    //     component.handleChartChange("CO2");
    //     expect(component.selectedCriteria).toBe("CO2");
    //     component.handleChartChange("CO2");
    //     expect(component.selectedCriteria).toBe("Global Vision");
    // });

    

    it("should set showInconsitencyBtn on updateInconsistent", () => {
        component.updateInconsistent(true);
        expect(component.showInconsitencyBtn).toBeTrue();
    });

    it("should set displayCriteriaPopup and selectedCriteriaPopup on displayPopupFct", () => {
        component.digitalService = { criteria: ["CO2"] } as any;
        component.organization = { criteriaDs: ["WATER"] } as any;
        component.subscriber = { criteria: ["CO2", "WATER"] } as any;
        component.displayPopupFct();
        expect(component.displayCriteriaPopup).toBeTrue();
        expect(component.selectedCriteriaPopup.length).toBeGreaterThan(0);
    });

    it("should return correct translation key from getTranslationKey", () => {
        const key = component.getTranslationKey("CO2", "title");
        expect(key).toBe("criteria.co2.title");
    });

    it("should return translated string from getTNSTranslation", () => {
        spyOn(component.translate, "instant").and.returnValue("translated");
        expect(component.getTNSTranslation("test")).toBe("translated");
    });
});
