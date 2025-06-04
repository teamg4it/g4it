import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Subscription } from "rxjs";
import { MapString } from "src/app/core/interfaces/generic.interfaces";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { AIFormsStore, AIInfrastructureForm } from "src/app/core/store/ai-forms.store";

@Component({
    selector: "app-digital-services-ai-infrastructure",
    templateUrl: "./digital-services-ai-infrastructure.component.html",
})
export class DigitalServicesAiInfrastructureComponent implements OnInit, OnDestroy {
    infrastructureForm!: FormGroup;
    private formSubscription: Subscription | undefined;
    locationOptions: { label: string; value: string }[] = [];

    constructor(
        private fb: FormBuilder,
        private messageService: MessageService,
        private aiFormsStore: AIFormsStore,
        private digitalServicesAiData: DigitalServicesAiDataService,
        private translate: TranslateService,
    ) {}

    ngOnInit(): void {
        this.infrastructureForm = this.fb.group({
            infrastructureType: [
                "SERVER_DC",
                [Validators.required, Validators.pattern(/^(SERVER_DC|LAPTOP|DESKTOP)$/)],
            ],
            nbCpuCores: [0, [Validators.required, Validators.min(0)]],
            nbGpu: [0, [Validators.required, Validators.min(0)]],
            gpuMemory: [0, [Validators.required, Validators.min(0)]],
            ramSize: [0, [Validators.required, Validators.min(0)]],
            pue: [0.1, [Validators.required, Validators.min(0.1)]],
            complementaryPue: [0.1, [Validators.required, Validators.min(0.1)]],
            location: ["", Validators.required],
        });

        // Charger les pays depuis l'API
        this.loadCountries();

        // Restaurer les données sauvegardées si elles existent
        const savedData = this.aiFormsStore.getInfrastructureFormData();
        if (savedData) {
            this.infrastructureForm.patchValue(savedData);
        }

        // Sauvegarder les données à chaque changement
        this.formSubscription = this.infrastructureForm.valueChanges.subscribe(
            (value) => {
                // Extraire les valeurs simples du formulaire
                const formData = {
                    infrastructureType: value.infrastructureType,
                    nbCpuCores: value.nbCpuCores,
                    nbGpu: value.nbGpu,
                    gpuMemory: value.gpuMemory,
                    ramSize: value.ramSize,
                    pue: value.pue,
                    complementaryPue: value.complementaryPue,
                    location: value.location,
                };
                this.aiFormsStore.setInfrastructureFormData(
                    formData as AIInfrastructureForm,
                );
            },
        );
    }

    private loadCountries(): void {
        this.digitalServicesAiData.getBoaviztapiCountryMap().subscribe(
            (countries: MapString) => {
                this.locationOptions = Object.entries(countries).map(([name, code]) => ({
                    label: name,
                    value: name,
                }));
            },
            (error) => {
                this.messageService.add({
                    severity: "error",
                    summary: this.translate.instant("common.error"),
                    detail: "Impossible de charger la liste des pays",
                });
            },
        );
    }

    ngOnDestroy(): void {
        if (this.formSubscription) {
            this.formSubscription.unsubscribe();
        }
    }

    submitFormData(): void {
        if (this.infrastructureForm.invalid) {
            this.infrastructureForm.markAllAsTouched();
            return;
        }

        this.messageService.add({
            severity: "success",
            summary: this.translate.instant("common.success"),
            detail: this.translate.instant("eco-mind-ai.ai-parameters.save-success"),
        });
    }
}
