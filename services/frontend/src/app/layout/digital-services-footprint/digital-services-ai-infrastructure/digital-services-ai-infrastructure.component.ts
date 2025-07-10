import { Component, inject, OnDestroy } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { lastValueFrom, Subscription, take } from "rxjs";
import {
    DigitalService,
    EcomindType,
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
    typesOptions: EcomindType[] = [];
    writeRight: boolean = false;

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
        this.userService.isAllowedEcoMindAiWrite$.pipe(take(1)).subscribe((isAllowed) => {
            if (isAllowed) {
                this.writeRight = true;
            }
        });

        // Load countries from API
        await this.loadCountries();

        const defaultInfrastructureType =
            this.typesOptions.find((t) => t.value === "Server") ?? this.typesOptions[0];

        //set default value
        this.infrastructureForm = this.fb.group({
            infrastructureType: [null, Validators.required],
            nbCpuCores: [null, Validators.min(0)],
            nbGpu: [null, [Validators.required, Validators.min(0)]],
            gpuMemory: [null, [Validators.required, Validators.min(0)]],
            ramSize: [null, [Validators.required, Validators.min(0)]],
            pue: [null, [Validators.required, Validators.min(1)]],
            complementaryPue: [null, [Validators.required, Validators.min(1)]],
            location: [null, Validators.required],
        });

        //change default fields value on infrastructure type value change
        this.infrastructureForm
            .get("infrastructureType")
            ?.valueChanges.subscribe((selectedEcomindType: EcomindType) => {
                if (!selectedEcomindType) return;

                this.infrastructureForm.patchValue(
                    {
                        nbCpuCores: selectedEcomindType.defaultCpuCores,
                        nbGpu: selectedEcomindType.defaultGpuCount,
                        gpuMemory: selectedEcomindType.defaultGpuMemory,
                        ramSize: selectedEcomindType.defaultRamSize,
                        pue: selectedEcomindType.defaultDatacenterPue,
                    },
                    { emitEvent: false },
                );
                if (
                    selectedEcomindType.value === "Desktop" ||
                    selectedEcomindType.value === "Laptop" ||
                    !this.writeRight
                ) {
                    this.infrastructureForm.get("pue")?.disable();
                } else {
                    this.infrastructureForm.get("pue")?.enable();
                }
            });

        //get the digital service uid with the activatedRoute
        const uid = this.route.pathFromRoot
            .map((r) => r.snapshot.paramMap.get("digitalServiceId"))
            .find((v) => v !== null);
        // default value for the form
        const defaultData = {
            infrastructureType: defaultInfrastructureType,
            nbCpuCores: defaultInfrastructureType.defaultCpuCores,
            nbGpu: defaultInfrastructureType.defaultGpuCount,
            gpuMemory: defaultInfrastructureType.defaultGpuMemory,
            ramSize: defaultInfrastructureType.defaultRamSize,
            pue: defaultInfrastructureType.defaultDatacenterPue,
            complementaryPue: 1.3,
            location: "France",
        };
        //to get it only one time
        if (!this.aiFormsStore.getInfrastructureChange() && uid) {
            this.digitalServicesAiData.getAiInfrastructure(uid).subscribe({
                next: (data) => {
                    if (data) {
                        const selectedType = this.typesOptions.find(
                            (t) => t.value === data.infrastructureType.value,
                        );
                        this.infrastructureForm.patchValue({
                            ...data,
                            infrastructureType: selectedType ?? defaultInfrastructureType,
                        });
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
        this.formSubscription = this.infrastructureForm.valueChanges.subscribe(() => {
            this.aiFormsStore.setInfrastructureChange(true);
            // Get all values from the form including disabled ones
            const formData = this.infrastructureForm.getRawValue();
            this.aiFormsStore.setInfrastructureFormData(formData as AIInfrastructureForm);
        });

        this.userService.isAllowedEcoMindAiWrite$.pipe(take(1)).subscribe((isAllowed) => {
            if (!isAllowed) {
                this.infrastructureForm.disable({ emitEvent: false });
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
