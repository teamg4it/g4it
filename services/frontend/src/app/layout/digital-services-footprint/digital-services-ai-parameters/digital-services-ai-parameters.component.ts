import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
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

                // Initialiser les options pour le modèle sélectionné
                const initialModel = this.terminalsForm.get("modelName")?.value;
                if (initialModel) {
                    const filtered = this.models.filter(
                        (m) => m.modelName === initialModel,
                    );
                    this.parameterOptions = Array.from(
                        new Set(filtered.map((m) => m.parameters)),
                    ).map((p) => ({ label: p, value: p }));
                }

                this.terminalsForm
                    .get("modelName")
                    ?.valueChanges.subscribe((selectedModelName) => {
                        if (!selectedModelName) {
                            this.parameterOptions = [];
                            this.frameworkOptions = [];
                            this.quantizationOptions = [];
                            return;
                        }

                        const filtered = this.models.filter(
                            (m) => m.modelName === selectedModelName,
                        );
                        this.parameterOptions = Array.from(
                            new Set(filtered.map((m) => m.parameters)),
                        ).map((p) => ({ label: p, value: p }));
                        this.frameworkOptions = [];
                        this.quantizationOptions = [];
                        this.terminalsForm.patchValue({
                            nbParameters: null,
                            framework: null,
                            quantization: null,
                        });
                    });

                this.terminalsForm
                    .get("nbParameters")
                    ?.valueChanges.subscribe((selectedParameter) => {
                        if (!selectedParameter) {
                            this.frameworkOptions = [];
                            this.quantizationOptions = [];
                            return;
                        }

                        const selectedModelName =
                            this.terminalsForm.get("modelName")?.value;
                        const filtered = this.models.filter(
                            (m) =>
                                m.modelName === selectedModelName &&
                                m.parameters === selectedParameter,
                        );
                        this.frameworkOptions = Array.from(
                            new Set(filtered.map((m) => m.framework)),
                        ).map((f) => ({ label: f, value: f }));
                        this.quantizationOptions = [];
                        this.terminalsForm.patchValue({
                            framework: null,
                            quantization: null,
                        });
                    });

                this.terminalsForm
                    .get("framework")
                    ?.valueChanges.subscribe((selectedFramework) => {
                        if (!selectedFramework) {
                            this.quantizationOptions = [];
                            return;
                        }

                        const selectedModelName =
                            this.terminalsForm.get("modelName")?.value;
                        const selectedParameter =
                            this.terminalsForm.get("nbParameters")?.value;
                        const filtered = this.models.filter(
                            (m) =>
                                m.modelName === selectedModelName &&
                                m.parameters === selectedParameter &&
                                m.framework === selectedFramework,
                        );

                        this.quantizationOptions = Array.from(
                            new Set(filtered.map((m) => m.quantization)),
                        ).map((q) => ({ label: q, value: q }));
                        this.terminalsForm.patchValue({
                            quantization: null,
                        });
                    });
            },
            error: (err: any) => {
                console.error("Erreur lors de la récupération des modèles IA:", err);
            },
        });

        // Restaurer les données sauvegardées si elles existent
        const savedData = this.aiFormsStore.getParametersFormData();
        if (savedData) {
            this.terminalsForm.patchValue(savedData);
            //this.updateTotalTokens();
        }

        // Sauvegarder les données à chaque changement
        this.formSubscription = this.terminalsForm.valueChanges.subscribe((value) => {
            // Calculer totalGeneratedTokens
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

    submitFormData(): void {
        if (this.terminalsForm.invalid) {
            this.terminalsForm.markAllAsTouched();
            return;
        }

        const formValue = this.terminalsForm.value;
        console.log(
            "Paramètres AI - Données envoyées au backend :",
            JSON.stringify(formValue, null, 2),
        );

        this.messageService.add({
            severity: "success",
            summary: "Succès",
            detail: "Paramètres sauvegardés avec succès.",
        });
    }
}
