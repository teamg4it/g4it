import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { Subscription } from "rxjs";
import { AIFormsStore, AIParametersForm } from "src/app/core/store/ai-forms.store";
import { DigitalServiceParameterIa } from "../../../core/interfaces/digital-service.interfaces";
import { ParameterService } from "../../../core/service/business/parameter.service";

//TODO : A modifier une fois que le backend sera fait

@Component({
    selector: "app-digital-services-ai-parameters",
    templateUrl: "./digital-services-ai-parameters.component.html",
})
export class DigitalServicesAiParametersComponent implements OnInit, OnDestroy {
    terminalsForm!: FormGroup;
    private formSubscription: Subscription | undefined;

    modelOptions: any[] = ["option1", "option2", "option3"];
    parameterOptions: any[] = ["option1", "option2", "option3"];
    frameworkOptions: any[] = ["option1", "option2", "option3"];
    quantizationOptions: any[] = ["option1", "option2", "option3"];

    constructor(
        private fb: FormBuilder,
        private inferenceService: ParameterService,
        private messageService: MessageService,
        private aiFormsStore: AIFormsStore,
    ) {}

    ngOnInit(): void {
        this.terminalsForm = this.fb.group({
            model: [null, Validators.required],
            parameter: [null, Validators.required],
            framework: [null, Validators.required],
            quantization: [null, Validators.required],
            inference: [true],
            finetuning: [false],
            numberOfUsers: [null, Validators.required],
            averageRequest: [null, Validators.required],
            averageToken: [null, Validators.required],
            totalTokenGenerate: [{ value: 0, disabled: true }],
        });

        // Restaurer les données sauvegardées si elles existent
        const savedData = this.aiFormsStore.getParametersFormData();
        if (savedData) {
            this.terminalsForm.patchValue(savedData);
        }

        // Sauvegarder les données à chaque changement
        this.formSubscription = this.terminalsForm.valueChanges.subscribe((value) => {
            this.aiFormsStore.setParametersFormData(value as AIParametersForm);
        });

        // Chargement des options depuis les services
        this.inferenceService
            .getModels()
            .subscribe(
                (data) => (this.modelOptions = data.map((v) => ({ label: v, value: v }))),
            );
        this.inferenceService
            .getParameters()
            .subscribe(
                (data) =>
                    (this.parameterOptions = data.map((v) => ({ label: v, value: v }))),
            );
        this.inferenceService
            .getFrameworks()
            .subscribe(
                (data) =>
                    (this.frameworkOptions = data.map((v) => ({ label: v, value: v }))),
            );
        this.inferenceService.getQuantizations().subscribe(
            (data) =>
                (this.quantizationOptions = data.map((v) => ({
                    label: v,
                    value: v,
                }))),
        );

        // Mise à jour automatique des tokens générés
        this.terminalsForm.valueChanges.subscribe(() => this.updateTotalTokens());
    }

    ngOnDestroy(): void {
        if (this.formSubscription) {
            this.formSubscription.unsubscribe();
        }
    }

    updateTotalTokens(): void {
        const u = this.terminalsForm.get("numberOfUsers")?.value || 0;
        const r = this.terminalsForm.get("averageRequest")?.value || 0;
        const t = this.terminalsForm.get("averageToken")?.value || 0;
        const total = u * r * t;
        this.terminalsForm.get("totalTokenGenerate")?.setValue(total);
    }

    submitFormData(): void {
        if (this.terminalsForm.invalid) {
            this.terminalsForm.markAllAsTouched();
            return;
        }

        const formValue: DigitalServiceParameterIa = {
            modelDetails: this.terminalsForm.value.model,
            parameters: this.terminalsForm.value.parameter,
            framework: this.terminalsForm.value.framework,
            quantization: this.terminalsForm.value.quantization,
            inference: this.terminalsForm.value.inference,
            finetuning: this.terminalsForm.value.finetuning,
            numberOfUsersPerYear: this.terminalsForm.value.numberOfUsers,
            averageRequestsPerUser: this.terminalsForm.value.averageRequest,
            averageTokensPerRequest: this.terminalsForm.value.averageToken,
            totalTokenGenerate: this.terminalsForm.getRawValue().totalTokenGenerate,
        };

        console.log(
            "📦 Paramètres AI - Données envoyées au backend :",
            JSON.stringify(formValue, null, 2),
        );

        this.inferenceService.submitForm(formValue).subscribe({
            next: () => {
                this.messageService.add({
                    severity: "success",
                    summary: "Succès",
                    detail: "Paramètres sauvegardés avec succès.",
                });

                this.terminalsForm.reset();
                this.terminalsForm.patchValue({
                    inference: true,
                    finetuning: false,
                });
            },
            error: (err) => {
                console.error("Erreur lors de la soumission :", err);
                this.messageService.add({
                    severity: "error",
                    summary: "Erreur",
                    detail: "Échec de la sauvegarde des paramètres.",
                });
            },
        });
    }
}
