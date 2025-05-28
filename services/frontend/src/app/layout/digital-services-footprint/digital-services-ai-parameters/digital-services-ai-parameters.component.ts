import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { Subscription } from "rxjs";
import { AIFormsStore, AIParametersForm } from "src/app/core/store/ai-forms.store";

@Component({
    selector: "app-digital-services-ai-parameters",
    templateUrl: "./digital-services-ai-parameters.component.html",
})
export class DigitalServicesAiParametersComponent implements OnInit, OnDestroy {
    terminalsForm!: FormGroup;
    private formSubscription: Subscription | undefined;

    modelOptions = [
        { label: "GPT-4", value: "GPT-4" },
        { label: "GPT-3.5", value: "GPT-3.5" },
        { label: "LLaMA", value: "LLaMA" },
    ];

    parameterOptions = [
        { label: "7B", value: "7B" },
        { label: "13B", value: "13B" },
        { label: "70B", value: "70B" },
    ];

    frameworkOptions = [
        { label: "PyTorch", value: "PyTorch" },
        { label: "TensorFlow", value: "TensorFlow" },
        { label: "JAX", value: "JAX" },
    ];

    quantizationOptions = [
        { label: "FP16", value: "FP16" },
        { label: "INT8", value: "INT8" },
        { label: "INT4", value: "INT4" },
    ];

    constructor(
        private fb: FormBuilder,
        private messageService: MessageService,
        private aiFormsStore: AIFormsStore,
    ) {}

    ngOnInit(): void {
        this.terminalsForm = this.fb.group({
            modelName: ["", Validators.required],
            nbParameters: ["", Validators.required],
            framework: ["", Validators.required],
            quantization: ["", Validators.required],
            isInference: [true],
            isFinetuning: [false],
            numberUserYear: [0, [Validators.required, Validators.min(0)]],
            averageNumberRequest: [0, [Validators.required, Validators.min(0)]],
            averageNumberToken: [0, [Validators.required, Validators.min(0)]],
            totalGeneratedTokens: [0],
        });

        // Restaurer les données sauvegardées si elles existent
        const savedData = this.aiFormsStore.getParametersFormData();
        if (savedData) {
            this.terminalsForm.patchValue(savedData);
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
