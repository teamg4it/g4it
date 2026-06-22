import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, fakeAsync, TestBed, tick } from "@angular/core/testing";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesAiParametersComponent } from "./digital-services-ai-parameters.component";

describe("DigitalServicesAiParametersComponent", () => {
    let component: DigitalServicesAiParametersComponent;
    let fixture: ComponentFixture<DigitalServicesAiParametersComponent>;
    let mockDigitalServicesDataService: any;
    let mockAiFormsStore: any;
    let mockUserService: any;
    let mockMessageService: any;
    let mockTranslateService: any;
    let mockDigitalServicesAiData: any;
    let mockDigitalServiceStore: any;

    const mockModels = [
        {
            modelName: "GPT-3",
            parameters: "175B",
            framework: "PyTorch",
            quantization: "FP16",
        },
        {
            modelName: "GPT-3",
            parameters: "175B",
            framework: "TensorFlow",
            quantization: "INT8",
        },
        {
            modelName: "BERT",
            parameters: "110M",
            framework: "PyTorch",
            quantization: "FP32",
        },
        {
            modelName: "BERT",
            parameters: "340M",
            framework: "PyTorch",
            quantization: "FP16",
        },
    ];

    beforeEach(async () => {
        mockDigitalServicesDataService = {
            getModels: jasmine.createSpy().and.returnValue(of(mockModels)),
            digitalService$: of({
                id: "123",
                name: "Test Service",
                lastCalculationDate: undefined,
            }),
        };

        mockAiFormsStore = {
            getParameterChange: jasmine.createSpy().and.returnValue(false),
            getParametersFormData: jasmine.createSpy().and.returnValue(null),
            setParametersFormData: jasmine.createSpy(),
            setParameterChange: jasmine.createSpy(),
        };

        mockUserService = {
            isAllowedEcoMindAiWrite$: of(true),
        };

        mockMessageService = {
            add: jasmine.createSpy(),
        };

        mockTranslateService = {
            instant: jasmine.createSpy().and.callFake((key) => key),
            get: jasmine.createSpy().and.callFake((key) => of(key)),
        };

        mockDigitalServicesAiData = {
            getAiParameter: jasmine.createSpy().and.returnValue(of(null)),
        };

        mockDigitalServiceStore = {
            setEcoMindEnableCalcul: jasmine.createSpy(),
        };

        await TestBed.configureTestingModule({
            imports: [
                ReactiveFormsModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                DigitalServicesAiParametersComponent,
            ],
            providers: [
                FormBuilder,
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesDataService,
                },
                { provide: AIFormsStore, useValue: mockAiFormsStore },
                { provide: UserService, useValue: mockUserService },
                { provide: MessageService, useValue: mockMessageService },
                { provide: TranslateService, useValue: mockTranslateService },
                {
                    provide: DigitalServicesAiDataService,
                    useValue: mockDigitalServicesAiData,
                },
                {
                    provide: DigitalServiceStoreService,
                    useValue: mockDigitalServiceStore,
                },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        pathFromRoot: [
                            {
                                snapshot: {
                                    paramMap: {
                                        get: jasmine.createSpy().and.returnValue("123"),
                                    },
                                },
                            },
                        ],
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesAiParametersComponent);
        component = fixture.componentInstance;
    });

    describe("Component Initialization", () => {
        it("should create the component", () => {
            expect(component).toBeTruthy();
        });

        it("should initialize form with default values", async () => {
            await component.ngOnInit();

            expect(component.terminalsForm).toBeDefined();
            expect(component.terminalsForm.get("averageNumberToken")?.value).toBe(500);
            expect(component.terminalsForm.get("totalGeneratedTokens")?.value).toBe(
                1000000000,
            );
            expect(component.terminalsForm.get("isInference")?.value).toBe(true);
            expect(component.terminalsForm.get("numberUserYear")?.value).toBe(10000);
            expect(component.terminalsForm.get("averageNumberRequest")?.value).toBe(200);
        });

        it("should load models on initialization", async () => {
            await component.ngOnInit();

            expect(mockDigitalServicesDataService.getModels).toHaveBeenCalledWith("LLM");
            expect(component.models).toEqual(mockModels);
            expect(component.modelOptions.length).toBe(2);
        });

        it("should disable totalGeneratedTokens field", async () => {
            await component.ngOnInit();

            expect(
                component.terminalsForm.get("totalGeneratedTokens")?.disabled,
            ).toBeTrue();
        });

        it("should disable isFinetuning field initially", async () => {
            await component.ngOnInit();

            expect(component.terminalsForm.get("isFinetuning")?.disabled).toBeTrue();
        });
    });

    describe("Model Selection and Dependent Fields", () => {
        it("should update parameters when model is selected", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({ modelName: "BERT" });
            tick();

            expect(component.parameterOptions.length).toBe(2); // 110M and 340M
            expect(component.parameterOptions[0].value).toBe("110M");
        }));

        it("should update frameworks when parameters are selected", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({
                modelName: "GPT-3",
                nbParameters: "175B",
            });
            tick();

            expect(component.frameworkOptions.length).toBe(2); // PyTorch and TensorFlow
        }));

        it("should update quantization options when framework is selected", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({
                modelName: "GPT-3",
                nbParameters: "175B",
                framework: "PyTorch",
            });
            tick();

            expect(component.quantizationOptions.length).toBeGreaterThan(0);
        }));

        it("should reset dependent fields when model is cleared", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({ modelName: "GPT-3" });
            tick();

            component.terminalsForm.patchValue({ modelName: null });
            tick();

            expect(component.terminalsForm.get("nbParameters")?.value).toBeNull();
            expect(component.terminalsForm.get("framework")?.value).toBeNull();
            expect(component.terminalsForm.get("quantization")?.value).toBeNull();
        }));

        it("should reset framework and quantization when parameters are cleared", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({
                modelName: "GPT-3",
                nbParameters: "175B",
                framework: "PyTorch",
            });
            tick();

            component.terminalsForm.patchValue({ nbParameters: null });
            tick();

            expect(component.terminalsForm.get("framework")?.value).toBeNull();
            expect(component.terminalsForm.get("quantization")?.value).toBeNull();
        }));
    });

    describe("Form Validation", () => {
        it("should require modelName", async () => {
            await component.ngOnInit();

            const modelNameControl = component.terminalsForm.get("modelName");
            modelNameControl?.setValue("");

            expect(modelNameControl?.hasError("required")).toBeTrue();
            expect(component.terminalsForm.invalid).toBeTrue();
        });

        it("should require nbParameters", async () => {
            await component.ngOnInit();

            const nbParametersControl = component.terminalsForm.get("nbParameters");
            nbParametersControl?.setValue("");

            expect(nbParametersControl?.hasError("required")).toBeTrue();
        });

        it("should require framework", async () => {
            await component.ngOnInit();

            const frameworkControl = component.terminalsForm.get("framework");
            frameworkControl?.setValue("");

            expect(frameworkControl?.hasError("required")).toBeTrue();
        });

        it("should require quantization", async () => {
            await component.ngOnInit();

            const quantizationControl = component.terminalsForm.get("quantization");
            quantizationControl?.setValue("");

            expect(quantizationControl?.hasError("required")).toBeTrue();
        });

        it("should validate minimum value for averageNumberToken", async () => {
            await component.ngOnInit();

            const tokenControl = component.terminalsForm.get("averageNumberToken");
            tokenControl?.setValue(-1);

            expect(tokenControl?.hasError("min")).toBeTrue();
        });

        it("should validate minimum value for numberUserYear", async () => {
            await component.ngOnInit();

            const userYearControl = component.terminalsForm.get("numberUserYear");
            userYearControl?.setValue(-1);

            expect(userYearControl?.hasError("min")).toBeTrue();
        });

        it("should validate minimum value for averageNumberRequest", async () => {
            await component.ngOnInit();

            const requestControl = component.terminalsForm.get("averageNumberRequest");
            requestControl?.setValue(-1);

            expect(requestControl?.hasError("min")).toBeTrue();
        });
    });

    describe("Form Data Management", () => {
        it("should save form data to store on value changes", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({ averageNumberToken: 1000 });
            tick();

            expect(mockAiFormsStore.setParametersFormData).toHaveBeenCalled();
            expect(mockAiFormsStore.setParameterChange).toHaveBeenCalledWith(true);
        }));

        it("should calculate totalGeneratedTokens on form change", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({
                numberUserYear: 100,
                averageNumberRequest: 200,
                averageNumberToken: 500,
            });
            tick();

            expect(component.terminalsForm.get("totalGeneratedTokens")?.value).toBe(
                10000000,
            );
        }));

        it("should restore form data from store if available", async () => {
            const savedData = {
                modelName: "BERT",
                nbParameters: "110M",
                framework: "PyTorch",
                quantization: "FP32",
                isInference: true,
                isFinetuning: false,
                numberUserYear: 5000,
                averageNumberRequest: 100,
                averageNumberToken: 250,
                totalGeneratedTokens: 125000000,
            };

            mockAiFormsStore.getParametersFormData.and.returnValue(savedData);
            mockAiFormsStore.getParameterChange.and.returnValue(true);

            await component.ngOnInit();

            expect(component.terminalsForm.get("modelName")?.value).toBe("");
            expect(component.terminalsForm.get("numberUserYear")?.value).toBe(10000);
        });
    });

    describe("Checkbox Handling", () => {
        it("should toggle isFinetuning when checkbox is clicked", async () => {
            await component.ngOnInit();

            component.isFinetuning = false;
            component.onCheckboxChange("isFinetuning");

            expect(component.isFinetuning).toBeTrue();

            component.onCheckboxChange("isFinetuning");
            expect(component.isFinetuning).toBeFalse();
        });

        it("should toggle isInference when checkbox is clicked", async () => {
            await component.ngOnInit();

            component.isInference = true;
            component.onCheckboxChange("isInference");

            expect(component.isInference).toBeFalse();

            component.onCheckboxChange("isInference");
            expect(component.isInference).toBeTrue();
        });
    });

    describe("Form Submission", () => {
        it("should show success message on valid form submission", async () => {
            await component.ngOnInit();

            component.terminalsForm.patchValue({
                modelName: "GPT-3",
                nbParameters: "175B",
                framework: "PyTorch",
                quantization: "FP16",
            });

            component.submitFormData();

            expect(mockMessageService.add).toHaveBeenCalledWith({
                severity: "success",
                summary: "common.success",
                detail: "eco-mind-ai.ai-parameters.save-success",
            });
        });

        it("should mark all fields as touched if form is invalid", async () => {
            await component.ngOnInit();

            component.terminalsForm.patchValue({ modelName: "" });

            const markAllAsTouchedSpy = spyOn(
                component.terminalsForm,
                "markAllAsTouched",
            ).and.callThrough();

            component.submitFormData();

            expect(markAllAsTouchedSpy).toHaveBeenCalled();
            expect(mockMessageService.add).not.toHaveBeenCalled();
        });
    });

    describe("Permissions and Access Control", () => {
        it("should disable form if user does not have write permission", fakeAsync(async () => {
            mockUserService.isAllowedEcoMindAiWrite$ = of(false);

            await component.ngOnInit();
            tick();

            expect(component.terminalsForm.disabled).toBeTrue();
        }));

        it("should enable form if user has write permission", fakeAsync(async () => {
            mockUserService.isAllowedEcoMindAiWrite$ = of(true);

            await component.ngOnInit();
            tick();

            expect(component.terminalsForm.enabled).toBeTrue();
        }));
    });

    describe("Calculate Button State", () => {
        it("should enable calculate button when form is valid and dirty", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({
                modelName: "GPT-3",
                nbParameters: "175B",
                framework: "PyTorch",
                quantization: "FP16",
            });
            component.terminalsForm.markAsDirty();
            tick();

            expect(mockDigitalServiceStore.setEcoMindEnableCalcul).toHaveBeenCalledWith(
                true,
            );
        }));

        it("should disable calculate button when form is invalid", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({ modelName: "" });
            component.terminalsForm.markAsDirty();
            tick();

            expect(mockDigitalServiceStore.setEcoMindEnableCalcul).toHaveBeenCalledWith(
                false,
            );
        }));

        it("should enable calculate button for new ecomind form with no calculation date", fakeAsync(async () => {
            mockDigitalServicesDataService.digitalService$ = of({
                id: "123",
                name: "Test Service",
                lastCalculationDate: undefined,
            });

            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({
                modelName: "GPT-3",
                nbParameters: "175B",
                framework: "PyTorch",
                quantization: "FP16",
            });
            tick();

            expect(mockDigitalServiceStore.setEcoMindEnableCalcul).toHaveBeenCalledWith(
                true,
            );
        }));
    });

    describe("Component Lifecycle", () => {
        it("should not throw error if no subscription exists on destroy", () => {
            expect(() => component.ngOnDestroy()).not.toThrow();
        });
    });

    describe("Edge Cases", () => {
        it("should handle empty models array", async () => {
            mockDigitalServicesDataService.getModels.and.returnValue(of([]));

            await component.ngOnInit();

            expect(component.models).toEqual([]);
            expect(component.modelOptions).toEqual([]);
        });

        it("should handle model with no frameworks", fakeAsync(async () => {
            const singleModel = [
                {
                    modelName: "TestModel",
                    parameters: "100M",
                    framework: "TestFramework",
                    quantization: "FP32",
                },
            ];
            mockDigitalServicesDataService.getModels.and.returnValue(of(singleModel));

            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({ modelName: "TestModel" });
            tick();

            expect(component.frameworkOptions.length).toBe(1);
        }));

        it("should maintain form state when switching between models", fakeAsync(async () => {
            await component.ngOnInit();
            tick();

            component.terminalsForm.patchValue({
                modelName: "GPT-3",
                numberUserYear: 15000,
            });
            tick();

            const userYearValue = component.terminalsForm.get("numberUserYear")?.value;

            component.terminalsForm.patchValue({ modelName: "BERT" });
            tick();

            expect(component.terminalsForm.get("numberUserYear")?.value).toBe(
                userYearValue,
            );
        }));
    });
});
