import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Subscription } from "rxjs";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore, AIParametersForm } from "src/app/core/store/ai-forms.store";

@Component({
    selector: "app-digital-services-ai-parameters",
    templateUrl: "./digital-services-ai-parameters.component.html",
})
export class DigitalServicesAiParametersComponent implements OnInit, OnDestroy {
    terminalsForm!: FormGroup;
    private formSubscription: Subscription | undefined;
    isFinetuning = false;
    isInference = true;
    model: string = "LLM";
    models: any[] = [];
    modelOptions: any[] = [];
    parameterOptions: any[] = [];
    frameworkOptions: any[] = [];
    quantizationOptions: any[] = [];

    constructor(
        private fb: FormBuilder,
        private digitalServicesDataService: DigitalServicesDataService,
        private messageService: MessageService,
        private aiFormsStore: AIFormsStore,
        private translate: TranslateService,
    ) {}

    ngOnInit(): void {
        this.terminalsForm = this.fb.group({
            modelName: ["", Validators.required],
            averageNumberToken: [0, [Validators.required, Validators.min(0)]],
            totalGeneratedTokens: [0],
            nbParameters: ["", Validators.required],
            framework: ["", Validators.required],
            quantization: ["", Validators.required],
            isInference: [true],
            isFinetuning: [false],
            numberUserYear: [0, [Validators.required, Validators.min(0)]],
            averageNumberRequest: [0, [Validators.required, Validators.min(0)]],
        });

        this.digitalServicesDataService.getModels(this.model).subscribe({
            next: (data) => {
                this.models = data;
                this.modelOptions = Array.from(
                    new Set(this.models.map((m) => m.modelName)),
                ).map((name) => ({ label: name, value: name }));

                // Restore backed-up data if available
                const savedData = this.aiFormsStore.getParametersFormData();
                if (savedData) {
                    this.terminalsForm.patchValue(savedData);
                    this.updateDependentFields(
                        savedData.modelName,
                        savedData.nbParameters,
                        savedData.framework,
                    );
                } else {
                    // If no data saved, set default values
                    if (this.modelOptions.length > 0) {
                        const defaultModel = this.modelOptions[0].value;
                        this.terminalsForm.patchValue({ modelName: defaultModel });
                        this.updateDependentFields(defaultModel);

                        // Save default values in the store
                        const defaultData = {
                            modelName: defaultModel,
                            nbParameters: this.parameterOptions[0]?.value || "",
                            framework: this.frameworkOptions[0]?.value || "",
                            quantization: this.quantizationOptions[0]?.value || "",
                            isInference: true,
                            isFinetuning: false,
                            numberUserYear: 0,
                            averageNumberRequest: 0,
                            averageNumberToken: 0,
                            totalGeneratedTokens: 0,
                        };
                        this.aiFormsStore.setParametersFormData(defaultData);
                    }
                }

                this.terminalsForm
                    .get("modelName")
                    ?.valueChanges.subscribe((selectedModel) => {
                        if (!selectedModel) {
                            this.resetDependentFields("parameters");
                            return;
                        }
                        this.updateDependentFields(selectedModel);
                    });

                this.terminalsForm
                    .get("nbParameters")
                    ?.valueChanges.subscribe((selectedParameter) => {
                        if (!selectedParameter) {
                            this.resetDependentFields("framework");
                            return;
                        }
                        const selectedModel = this.terminalsForm.get("modelName")?.value;
                        this.updateDependentFields(selectedModel, selectedParameter);
                    });

                this.terminalsForm
                    .get("framework")
                    ?.valueChanges.subscribe((selectedFramework) => {
                        if (!selectedFramework) {
                            this.resetDependentFields("quantization");
                            return;
                        }
                        const selectedModel = this.terminalsForm.get("modelName")?.value;
                        const selectedParameter =
                            this.terminalsForm.get("nbParameters")?.value;
                        this.updateDependentFields(
                            selectedModel,
                            selectedParameter,
                            selectedFramework,
                        );
                    });
            },
            error: (err: any) => {
                this.messageService.add({
                    severity: "error",
                    summary: this.translate.instant("common.error"),
                    detail: this.translate.instant("eco-mind-ai.ai-parameters.error"),
                });
            },
        });

        // Save data whenever changes are made
        this.formSubscription = this.terminalsForm.valueChanges.subscribe((value) => {
            // Calculate totalGeneratedTokens
            const totalTokens =
                value.numberUserYear *
                value.averageNumberRequest *
                value.averageNumberToken;
            this.terminalsForm.patchValue(
                { totalGeneratedTokens: totalTokens },
                { emitEvent: false },
            );

            this.aiFormsStore.setParametersFormData(value as AIParametersForm);
        });
    }

    ngOnDestroy(): void {
        if (this.formSubscription) {
            this.formSubscription.unsubscribe();
        }
    }

    onCheckboxChange(selected: "isFinetuning" | "isInference") {
        if (selected === "isFinetuning") {
            this.isFinetuning = !this.isFinetuning;
        } else if (selected === "isInference") {
            this.isInference = !this.isInference;
        }
    }

    submitFormData(): void {
        if (this.terminalsForm.invalid) {
            this.terminalsForm.markAllAsTouched();
            return;
        }

        this.messageService.add({
            severity: "success",
            summary: this.translate.instant("common.success"),
            detail: this.translate.instant("eco-mind-ai.ai-parameters.save-success"),
        });
    }

    private updateDependentFields(
        modelName?: string,
        selectedParameter?: string,
        selectedFramework?: string,
    ): void {
        if (!modelName) return;

        // Update parameters
        const filtered = this.models.filter((m) => m.modelName === modelName);
        this.parameterOptions = Array.from(
            new Set(filtered.map((m) => m.parameters)),
        ).map((p) => ({ label: p, value: p }));

        if (this.parameterOptions.length > 0 && !selectedParameter) {
            this.terminalsForm.patchValue({
                nbParameters: this.parameterOptions[0].value,
            });
            selectedParameter = this.parameterOptions[0].value;
        }

        if (selectedParameter) {
            // Update frameworks
            const filteredByParam = filtered.filter(
                (m) => m.parameters === selectedParameter,
            );
            this.frameworkOptions = Array.from(
                new Set(filteredByParam.map((m) => m.framework)),
            ).map((f) => ({ label: f, value: f }));

            if (this.frameworkOptions.length > 0 && !selectedFramework) {
                this.terminalsForm.patchValue({
                    framework: this.frameworkOptions[0].value,
                });
                selectedFramework = this.frameworkOptions[0].value;
            }

            if (selectedFramework) {
                // Update quantizations
                const filteredByFramework = filteredByParam.filter(
                    (m) => m.framework === selectedFramework,
                );
                this.quantizationOptions = Array.from(
                    new Set(filteredByFramework.map((m) => m.quantization)),
                ).map((q) => ({ label: q, value: q }));

                if (this.quantizationOptions.length > 0) {
                    this.terminalsForm.patchValue({
                        quantization: this.quantizationOptions[0].value,
                    });
                }
            }
        }
    }

    private resetDependentFields(
        startFrom: "parameters" | "framework" | "quantization" = "parameters",
    ): void {
        const resetValues: any = {};

        if (startFrom === "parameters") {
            resetValues.nbParameters = null;
            resetValues.framework = null;
            resetValues.quantization = null;
            this.frameworkOptions = [];
            this.quantizationOptions = [];
        } else if (startFrom === "framework") {
            resetValues.framework = null;
            resetValues.quantization = null;
            this.quantizationOptions = [];
        } else if (startFrom === "quantization") {
            resetValues.quantization = null;
        }

        this.terminalsForm.patchValue(resetValues);
    }
}
