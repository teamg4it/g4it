import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of, throwError } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { DigitalServicesAiInfrastructureComponent } from "./digital-services-ai-infrastructure.component";

describe("DigitalServicesAiInfrastructureComponent", () => {
    let component: DigitalServicesAiInfrastructureComponent;
    let fixture: ComponentFixture<DigitalServicesAiInfrastructureComponent>;

    // Mocks
    let mockAiDataService: any;
    let mockAiFormsStore: any;
    let mockUserService: any;
    let mockMessageService: any;
    let mockTranslateService: any;

    beforeEach(async () => {
        mockAiDataService = {
            getBoaviztapiCountryMap: jasmine
                .createSpy()
                .and.returnValue(of({ France: "FR", Germany: "DE" })),
            getEcomindReferential: jasmine
                .createSpy()
                .and.returnValue(of([{ value: "GPU" }, { value: "CPU" }])),
            getAiInfrastructure: jasmine.createSpy().and.returnValue(of(null)),
        };

        mockAiFormsStore = {
            getInfrastructureChange: jasmine.createSpy().and.returnValue(false),
            getInfrastructureFormData: jasmine.createSpy().and.returnValue(null),
            setInfrastructureFormData: jasmine.createSpy(),
            setInfrastructureChange: jasmine.createSpy(),
        };

        mockUserService = {
            isAllowedEcoMindAiWrite$: of(true),
        };

        mockMessageService = {
            add: jasmine.createSpy(),
        };

        mockTranslateService = {
            instant: jasmine.createSpy().and.callFake((key) => key),
        };

        await TestBed.configureTestingModule({
            declarations: [DigitalServicesAiInfrastructureComponent],
            imports: [ReactiveFormsModule, HttpClientTestingModule],
            providers: [
                FormBuilder,
                { provide: DigitalServicesAiDataService, useValue: mockAiDataService },
                { provide: AIFormsStore, useValue: mockAiFormsStore },
                { provide: UserService, useValue: mockUserService },
                { provide: MessageService, useValue: mockMessageService },
                { provide: TranslateService, useValue: mockTranslateService },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        pathFromRoot: [
                            {
                                snapshot: {
                                    paramMap: new Map([["digitalServiceId", "123"]]),
                                },
                            },
                        ],
                    },
                },
                { provide: DigitalServicesDataService, useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesAiInfrastructureComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize form and load data on ngOnInit()", async () => {
        await component.ngOnInit();

        expect(mockAiDataService.getBoaviztapiCountryMap).toHaveBeenCalled();
        expect(mockAiDataService.getEcomindReferential).toHaveBeenCalled();
        expect(mockAiDataService.getAiInfrastructure).toHaveBeenCalledWith("123");
        expect(component.infrastructureForm).toBeDefined();
        expect(component.locationOptions.length).toBeGreaterThan(0);
        expect(component.typesOptions.length).toBeGreaterThan(0);
        expect(mockAiFormsStore.setInfrastructureFormData).toHaveBeenCalled();
    });

    it("should patch form with store data if infrastructure change is true", async () => {
        mockAiFormsStore.getInfrastructureChange.and.returnValue(true);
        mockAiFormsStore.getInfrastructureFormData.and.returnValue({
            nbCpuCores: 2,
            nbGpu: 1,
            gpuMemory: 8,
            ramSize: 16,
            pue: 1.2,
            complementaryPue: 1.1,
            location: "Germany",
            infrastructureType: "GPU",
        });

        await component.ngOnInit();

        expect(component.infrastructureForm.value.nbCpuCores).toBe(2);
        expect(mockAiDataService.getAiInfrastructure).not.toHaveBeenCalled();
    });

    it("should handle error during infrastructure load", async () => {
        mockAiDataService.getAiInfrastructure.and.returnValue(
            throwError(() => new Error("Error")),
        );
        await component.ngOnInit();
        expect(mockMessageService.add).toHaveBeenCalled();
    });

    it("should disable form if user is not allowed", async () => {
        mockUserService.isAllowedEcoMindAiWrite$ = of(false);
        await component.ngOnInit();
        expect(component.infrastructureForm.disabled).toBeTrue();
    });

    it("should unsubscribe on destroy", () => {
        const mockSub = jasmine.createSpyObj("Subscription", ["unsubscribe"]);

        Object.defineProperty(component as any, "formSubscription", {
            value: mockSub,
            writable: true,
        });

        component.ngOnDestroy();
        expect(mockSub.unsubscribe).toHaveBeenCalled();
    });

    it("should mark form as touched and not submit if invalid", () => {
        component.infrastructureForm = new FormBuilder().group({
            nbCpuCores: [null, Validators.required],
        });

        const markAllAsTouchedSpy = spyOn(
            component.infrastructureForm,
            "markAllAsTouched",
        ).and.callThrough();

        component.submitFormData();

        expect(component.infrastructureForm.invalid).toBeTrue();
        expect(markAllAsTouchedSpy).toHaveBeenCalled();
        expect(mockMessageService.add).not.toHaveBeenCalled();
    });

    it("should submit if form is valid", () => {
        component.infrastructureForm = new FormBuilder().group({
            infrastructureType: ["GPU"],
            nbCpuCores: [1],
            nbGpu: [1],
            gpuMemory: [1],
            ramSize: [1],
            pue: [1],
            complementaryPue: [1],
            location: ["France"],
        });

        component.submitFormData();
        expect(mockMessageService.add).toHaveBeenCalled();
    });
});
