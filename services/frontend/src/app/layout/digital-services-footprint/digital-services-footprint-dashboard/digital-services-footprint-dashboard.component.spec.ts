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
import { Constants } from "src/constants";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard.component";

describe("DigitalServicesFootprintDashboardComponent", () => {
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
        update: jasmine.createSpy("update"),
    };

    const digitalServiceBusinessServiceMock = {
        updateDsCriteria: jasmine.createSpy("updateDsCriteria").and.returnValue(of({})),
        triggerLaunchCalcul: jasmine.createSpy("triggerLaunchCalcul"),
    };

    const userServiceMock = {
        currentOrganization$: of({ name: "SubName", id: 1 }),
        currentWorkspace$: of({
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
        isSharedDS: jasmine.createSpy("isSharedDS").and.returnValue(false),
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
        component.ngOnInit();
        expect(component.digitalService.name).toBe("Test Digital Service");
    });

    it("should handle chart change logic", () => {
        component.selectedCriteria = "Global Vision";
        component.handleChartChange("CO2");
        expect(component.selectedCriteria).toBe("CO2");
        component.handleChartChange("CO2");
        expect(component.selectedCriteria).toBe("Global Vision");
    });

    it("should set displayCriteriaPopup and selectedCriteriaPopup on displayPopupFct", () => {
        component.digitalService = { criteria: ["CO2"] } as any;
        component.workspace = { criteriaDs: ["WATER"] } as any;
        component.organization = { criteria: ["CO2", "WATER"] } as any;
        component.displayPopupFct();
        expect(component.displayCriteriaPopup).toBeTrue();
        expect(component.selectedCriteriaPopup.length).toBeGreaterThan(0);
    });

    it("should return translated string from getTNSTranslation", () => {
        spyOn(component.translate, "instant").and.returnValue("translated");
        expect(component.getTNSTranslation("test")).toBe("translated");
    });

    it("should call updateDsCriteria, get, setEnableCalcul, and triggerLaunchCalcul in handleSaveDs", () => {
        const DSCriteria = { criteria: ["CO2"] } as any;
        component.digitalService = { uid: "test-uid" } as any;

        // Spies for chained calls
        const updateDsCriteriaSpy = digitalServiceBusinessServiceMock.updateDsCriteria;
        const getSpy = digitalServicesDataServiceMock.get;
        const setEnableCalculSpy = digitalServiceStoreMock.setEnableCalcul;
        const triggerLaunchCalculSpy =
            digitalServiceBusinessServiceMock.triggerLaunchCalcul;

        component.displayCriteriaPopup = true;
        component.handleSaveDs(DSCriteria);

        expect(updateDsCriteriaSpy).toHaveBeenCalledWith("test-uid", DSCriteria);
        expect(getSpy).toHaveBeenCalledWith("test-uid");
        expect(setEnableCalculSpy).toHaveBeenCalledWith(true);
        expect(triggerLaunchCalculSpy).toHaveBeenCalled();
        expect(component.displayCriteriaPopup).toBeFalse();
    });

    it("should set enableDataInconsistency to false, set showInconsitency to false, and update digitalService when event is false", async () => {
        component.showInconsitency = true;
        digitalServicesDataServiceMock.update.and.returnValue(
            of({ uid: "test-uid", enableDataInconsistency: false }),
        );
        await component.updateDataConsistencyInDS(false);
        expect(component.digitalService.enableDataInconsistency).toBeFalse();
        expect(component.showInconsitency).toBeFalse();
    });

    it("should set enableDataInconsistency to true, set showInconsitency to true, and update digitalService when event is true", async () => {
        component.showInconsitency = true;
        digitalServicesDataServiceMock.update.and.returnValue(
            of({ uid: "test-uid", enableDataInconsistency: true }),
        );
        await component.updateDataConsistencyInDS(true);
        expect(component.digitalService.enableDataInconsistency).toBeTrue();
        expect(component.showInconsitency).toBeTrue();
    });

    it("should return empty string if textType is not 'digital-services-card-content'", () => {
        component.aiRecommendation = { recommendations: "[]" } as any;
        const result = component.getEcoMindRecomendation();
        expect(result).toBe("");
    });

    it("should return empty string if aiRecommendation is null", () => {
        component.aiRecommendation = null as any;
        const result = component.getEcoMindRecomendation();
        expect(result).toBe("");
    });

    it("should return empty string if recommendations is null", () => {
        component.aiRecommendation = { recommendations: null } as any;
        const result = component.getEcoMindRecomendation();
        expect(result).toBe("");
    });

    it("should return empty string if recommendations is not an array", () => {
        component.aiRecommendation = { recommendations: "{}" } as any;
        const result = component.getEcoMindRecomendation();
        expect(result).toBe("");
    });

    it("should return empty string if recommendations array is empty", () => {
        component.aiRecommendation = { recommendations: "[]" } as any;
        const result = component.getEcoMindRecomendation();
        expect(result).toBe("");
    });

    it("should return HTML table if recommendations array is valid", () => {
        const recommendations = [
            { action: "Reduce usage", impact: "High" },
            { action: "Switch provider", impact: "Medium" },
        ];
        component.digitalService = { isAi: true } as any;
        component.aiRecommendation = {
            recommendations: JSON.stringify(recommendations),
        } as any;
        const result = component.getEcoMindRecomendation();
        expect(result).toContain("<table");
        expect(result).toContain("Reduce usage");
        expect(result).toContain("Switch provider");
    });

    it("should return empty string and log error if JSON.parse throws", () => {
        component.aiRecommendation = { recommendations: "not-a-json" } as any;
        const result = component.getEcoMindRecomendation();
        expect(result).toBe("");
    });

    it("should return 'ds-graph-description.server.' when not barChartChild and selectedParam is 'Server'", () => {
        component.selectedParam = "Server";
        component.barChartChild = false;
        const result = component.getBarTranslateKey();
        expect(result).toBe("ds-graph-description.server.");
    });

    it("should return 'ds-graph-description.cloud-lifecycle.' when barChartChild and selectedParam is CLOUD_SERVICE", () => {
        component.selectedParam = Constants.CLOUD_SERVICE;
        component.barChartChild = true;
        const result = component.getBarTranslateKey();
        expect(result).toBe("ds-graph-description.cloud-lifecycle.");
    });
    it("should return 'ds-graph-description.terminal-lifecycle.' when barChartChild and selectedParam is TERMINAL", () => {
        component.selectedParam = Constants.TERMINAL;
        component.barChartChild = true;
        const result = component.getBarTranslateKey();
        expect(result).toBe("ds-graph-description.terminal-lifecycle.");
    });

    it("should return generic key for other params", () => {
        component.selectedParam = "Other Param";
        component.barChartChild = false;
        const result = component.getBarTranslateKey();
        expect(result).toBe("ds-graph-description.other-param.");
    });

    it("should return empty string if no impacts", () => {
        component.barChartTopThreeImpact = [];
        const result = component.getBarChartTextDescription("prefix.");
        expect(result).toBe("");
    });

    describe("getGlobalVisionTextDescription", () => {
        it("should return empty string if topThreeImpacts is empty", () => {
            component.topThreeImpacts = [];
            const result = component.getGlobalVisionTextDescription("prefix.");
            expect(result).toBe("");
        });
    });

    describe("getCriteriaTextDescription", () => {
        it("should return empty string if topPieThreeImpacts is empty", () => {
            component.topPieThreeImpacts = [];
            const result = component.getCriteriaTextDescription(
                "prefix.",
                "criteria-key",
            );
            expect(result).toBe("");
        });
    });

    it("should getContentText to be defined", () => {
        const result = component.getContentText();
        expect(component.getContentText()).toBeDefined();
    });

    it("should getEcoMindRecomendation to be defined", () => {
        const result = component.getEcoMindRecomendation();
        expect(component.getEcoMindRecomendation).toBeDefined();
    });

    it("should call initImpacts and return if globalFootprintData is empty", () => {
        const spy = spyOn(component, "initImpacts");
        component.setCriteriaButtons([]);
        expect(spy).toHaveBeenCalled();
    });

    it("should return formatted description for barChartTopThreeImpact", () => {
        component.barChartTopThreeImpact = [
            { name: "Impact1", totalSipValue: 10, totalRawValue: 5, unit: "kg" },
            { name: "Impact2", totalSipValue: 20, totalRawValue: 15, unit: "g" },
            { name: "Impact3", totalSipValue: 30, totalRawValue: 25, unit: "t" },
        ];
        const result = component.getBarChartTextDescription(
            "ds-graph-description.terminal-type.",
        );
        expect(result).toContain("<br />");
        expect(result).toContain(",");
    });

    it("should return formatted description for barChartTopThreeImpact", () => {
        component.topThreeImpacts = [
            {
                name: "Impact1",
                totalSipValue: 10,
                totalRawValue: 5,
                unit: "kg",
                maxCriteria: { name: "Test1" },
            },
            {
                name: "Impact2",
                totalSipValue: 20,
                totalRawValue: 15,
                unit: "g",
                maxCriteria: { name: "test1" },
            },
            {
                name: "Impact3",
                totalSipValue: 30,
                totalRawValue: 25,
                unit: "t",
                maxCriteria: { name: "Test2" },
            },
        ];
        const result = component.getGlobalVisionTextDescription(
            "ds-graph-description.global-vision.",
        );
        expect(result).toContain("<br />");
        expect(result).toContain(",");
    });

    it("should return formatted description for barChartTopThreeImpact", () => {
        component.topPieThreeImpacts = [
            { name: "Impact1", totalSipValue: 10, totalRawValue: 5, unit: "kg" },
            { name: "Impact2", totalSipValue: 20, totalRawValue: 15, unit: "g" },
            { name: "Impact3", totalSipValue: 30, totalRawValue: 25, unit: "t" },
        ];
        const result = component.getCriteriaTextDescription(
            "ds-graph-description.criteria.",
            "climate-change",
        );
        expect(result).toContain("<br />");
        expect(result).toContain(",");
    });
});
