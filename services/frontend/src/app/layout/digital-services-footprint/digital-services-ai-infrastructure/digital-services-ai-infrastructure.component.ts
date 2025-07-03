import { Component, inject, OnDestroy } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { lastValueFrom, Subscription } from "rxjs";
import {
    DigitalService,
    TerminalsType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { MapString } from "src/app/core/interfaces/generic.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore, AIInfrastructureForm } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-digital-services-ai-infrastructure",
    templateUrl: "./digital-services-ai-infrastructure.component.html",
})
export class DigitalServicesAiInfrastructureComponent implements OnDestroy {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);
    infrastructureForm!: FormGroup;
    private formSubscription: Subscription | undefined;
    locationOptions: { label: string; value: string }[] = [];
    digitalService: DigitalService = {} as DigitalService;
    public userService = inject(UserService);
    typesOptions: TerminalsType[] = [];

    constructor(
        private readonly fb: FormBuilder,
        private readonly messageService: MessageService,
        private readonly aiFormsStore: AIFormsStore,
        private readonly digitalServicesAiData: DigitalServicesAiDataService,
        private readonly translate: TranslateService,
        private readonly digitalServicesDataService: DigitalServicesDataService,
        private readonly route: ActivatedRoute,
    ) {}

    async ngOnInit() {
        // Load countries from API
        await this.loadCountries();
        //set default value
        this.infrastructureForm = this.fb.group({
            infrastructureType: [this.typesOptions[1], [Validators.required]],
            nbCpuCores: [0, [Validators.min(0)]],
            nbGpu: [0, [Validators.required, Validators.min(0)]],
            gpuMemory: [0, [Validators.required, Validators.min(0)]],
            ramSize: [0, [Validators.required, Validators.min(0)]],
            pue: [1, [Validators.required, Validators.min(1)]],
            complementaryPue: [1, [Validators.required, Validators.min(1)]],
            location: ["France", Validators.required],
        });

        //get the digital service uid with the activatedRoute
        const uid = this.route.pathFromRoot
            .map((r) => r.snapshot.paramMap.get("digitalServiceId"))
            .find((v) => v !== null);
        // default value for the form
        const defaultData = {
            infrastructureType: this.typesOptions[1],
            nbCpuCores: 0,
            nbGpu: 0,
            gpuMemory: 0,
            ramSize: 0,
            pue: 1.5,
            complementaryPue: 1.3,
            location: "France",
        };
        //to get it only one time
        if (!this.aiFormsStore.getInfrastructureChange() && uid) {
            this.digitalServicesAiData.getAiInfrastructure(uid).subscribe({
                next: (data) => {
                    if (data) {
                        this.infrastructureForm.patchValue(data);
                    } else {
                        //set the value
                        this.infrastructureForm.patchValue(defaultData);
                        //save the value
                        this.aiFormsStore.setInfrastructureFormData(defaultData);
                    }
                },
                error: (err: any) => {
                    this.messageService.add({
                        severity: "error",
                        summary: this.translate.instant("common.error"),
                        detail: this.translate.instant("eco-mind-ai.ai-parameters.error"),
                    });
                },
            });
        } else {
            const data = this.aiFormsStore.getInfrastructureFormData();
            this.infrastructureForm.patchValue(data ?? defaultData);
        }

        // Save data whenever changes are made
        this.formSubscription = this.infrastructureForm.valueChanges.subscribe(
            (value) => {
                this.aiFormsStore.setInfrastructureChange(true);
                // Extract simple values from the form
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

        this.userService.isAllowedEcoMindAiWrite$.subscribe((isAllowed) => {
            if (!isAllowed) {
                this.infrastructureForm.disable();
            }
        });
    }

    async loadCountries() {
        this.digitalServicesAiData.getBoaviztapiCountryMap().subscribe({
            next: (countries: MapString) => {
                this.locationOptions = Object.entries(countries).map(([name, code]) => ({
                    label: name,
                    value: name,
                }));
            },
            error: (err) => {
                this.messageService.add({
                    severity: "error",
                    summary: this.translate.instant("common.error"),
                    detail: "Unable to load country list",
                });
            },
        });
        const referentials = await lastValueFrom(
            this.digitalServicesAiData.getEcomindReferential(),
        );

        this.typesOptions = [...referentials].sort((a, b) =>
            a.value.localeCompare(b.value),
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
