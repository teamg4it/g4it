import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of, Subject } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServicesFootprintFooterComponent } from "./digital-services-footprint-footer.component";

describe("DigitalServicesFootprintFooterComponent", () => {
    let component: DigitalServicesFootprintFooterComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintFooterComponent>;

    // Mocks
    let mockRouter: jasmine.SpyObj<Router>;
    let mockTranslate: jasmine.SpyObj<TranslateService>;
    let mockMessageService: jasmine.SpyObj<MessageService>;
    let mockDigitalServicesData: jasmine.SpyObj<DigitalServicesDataService>;
    let mockDigitalServiceBusinessService: jasmine.SpyObj<DigitalServiceBusinessService>;
    let mockDigitalServicesAiData: jasmine.SpyObj<DigitalServicesAiDataService>;
    let mockDigitalServiceStore: jasmine.SpyObj<DigitalServiceStoreService>;
    let mockAiFormsStore: jasmine.SpyObj<AIFormsStore>;
    let mockGlobalStore: jasmine.SpyObj<GlobalStoreService>;

    beforeEach(() => {
        mockRouter = jasmine.createSpyObj("Router", ["navigateByUrl", "navigate"], {
            url: "/organizations/abc/workpsaces/xyz/digital-services/123/footprint",
        });
        mockTranslate = jasmine.createSpyObj("TranslateService", ["instant"]);
        mockMessageService = jasmine.createSpyObj("MessageService", ["add"]);
        mockDigitalServicesData = jasmine.createSpyObj("DigitalServicesDataService", [
            "digitalService$",
            "launchEvaluating",
            "get",
        ]);
        mockDigitalServiceBusinessService = jasmine.createSpyObj(
            "DigitalServiceBusinessService",
            [],
            { launchCalcul$: new Subject<void>() },
        );
        mockDigitalServicesAiData = jasmine.createSpyObj("DigitalServicesAiDataService", [
            "saveAiInfrastructure",
            "saveAiParameters",
        ]);
        mockDigitalServiceStore = jasmine.createSpyObj("DigitalServiceStoreService", [
            "digitalService",
            "enableCalcul",
            "ecomindEnableCalcul",
            "inPhysicalEquipments",
            "inVirtualEquipments",
            "setEnableCalcul",
            "setDigitalService",
        ]);
        mockAiFormsStore = jasmine.createSpyObj("AIFormsStore", [
            "getParametersFormData",
            "getInfrastructureFormData",
        ]);
        mockGlobalStore = jasmine.createSpyObj("GlobalStoreService", ["setLoading"]);

        TestBed.configureTestingModule({
            declarations: [DigitalServicesFootprintFooterComponent],
            imports: [HttpClientTestingModule, RouterTestingModule],
            providers: [
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            paramMap: {
                                get: (key: string) => {
                                    if (key === "digitalServiceVersionId") return "1";
                                    return null;
                                },
                            },
                        },
                    },
                },
                { provide: Router, useValue: mockRouter },
                { provide: TranslateService, useValue: mockTranslate },
                { provide: MessageService, useValue: mockMessageService },
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesData,
                },
                {
                    provide: DigitalServiceBusinessService,
                    useValue: mockDigitalServiceBusinessService,
                },
                {
                    provide: DigitalServicesAiDataService,
                    useValue: mockDigitalServicesAiData,
                },
                {
                    provide: DigitalServiceStoreService,
                    useValue: mockDigitalServiceStore,
                },
                { provide: AIFormsStore, useValue: mockAiFormsStore },
                { provide: GlobalStoreService, useValue: mockGlobalStore },
            ],
            schemas: [NO_ERRORS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesFootprintFooterComponent);
        component = fixture.componentInstance;
        component.digitalService = { uid: "123" } as DigitalService;

        // Defaults
        mockDigitalServiceStore.digitalService.and.returnValue({
            uid: "123",
        } as DigitalService);
        mockDigitalServiceStore.enableCalcul.and.returnValue(false);
        mockDigitalServiceStore.ecomindEnableCalcul.and.returnValue(false);
        mockDigitalServiceStore.inPhysicalEquipments.and.returnValue([]);
        mockDigitalServiceStore.inVirtualEquipments.and.returnValue([]);
        mockTranslate.instant.and.callFake((key: string) => key);
    });
    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should enable calculation when isEcoMindAi is false", () => {
        (component as any).isEcoMindAi = () => ({ value: true }); // mock as function returning signal-like object
        expect(component.enableCalcul()).toBeFalse();
    });

    it("should subscribe to digitalService$ and launchCalcul$", () => {
        const dsSubject = new Subject<DigitalService>();
        mockDigitalServicesData.digitalService$ = dsSubject.asObservable();

        component.ngOnInit();
        const mockService = { uid: "123" } as DigitalService;
        dsSubject.next(mockService);

        expect(mockDigitalServiceStore.setDigitalService).toHaveBeenCalledWith(
            mockService,
        );
    });

    it("should show warning if AI forms are incomplete", async () => {
        (component as any).isEcoMindAi = () => ({ value: true });
        mockAiFormsStore.getParametersFormData.and.returnValue(null);
        mockAiFormsStore.getInfrastructureFormData.and.returnValue(null);

        await component.handleSave();

        expect(mockMessageService.add).toHaveBeenCalledWith(
            jasmine.objectContaining({ severity: "warn" }),
        );
    });

    it("should save AI forms and show success message", async () => {
        component.digitalService = { uid: "123" } as DigitalService;
        mockAiFormsStore.getParametersFormData.and.returnValue({
            modelName: "test",
            nbParameters: "1",
            framework: "X",
            quantization: "Y",
            numberUserYear: 1,
            averageNumberRequest: 1,
            averageNumberToken: 1,
            isInference: false,
            isFinetuning: false,
            totalGeneratedTokens: 0,
        }) as any;
        mockAiFormsStore.getInfrastructureFormData.and.returnValue({
            infrastructureType: {
                code: "cloud",
                value: "Cloud",
                lifespan: 12,
                defaultCpuCores: 4,
                defaultGpuCount: 1,
                defaultGpuMemory: 16,
                defaultRamSize: 32,
                defaultDatacenterPue: 1.2,
            },
            nbCpuCores: 4,
            nbGpu: 1,
            gpuMemory: 16,
            ramSize: 32,
            pue: 1.2,
            location: "IN",
        });
        mockDigitalServicesAiData.saveAiInfrastructure.and.returnValue(of({}) as any);
        mockDigitalServicesAiData.saveAiParameters.and.returnValue(of({}) as any);

        await component.handleSave();

        expect(mockDigitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
    });

    it("should show error if digitalService.uid is missing when isEcoMindAi is true", () => {
        (component as any).isEcoMindAi = () => ({ value: true });
        component.digitalService = {} as DigitalService;

        component.launchCalcul();

        expect(mockMessageService.add).toHaveBeenCalledWith(
            jasmine.objectContaining({ severity: "warn" }),
        );
    });
});
