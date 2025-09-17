import {
    Component,
    computed,
    DestroyRef,
    inject,
    OnDestroy,
    OnInit,
    Signal,
} from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { lastValueFrom, Subscription, take } from "rxjs";
import {
    DigitalService,
    EcomindType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore, AIInfrastructureForm } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-digital-services-ai-infrastructure",
    templateUrl: "./digital-services-ai-infrastructure.component.html",
})
export class DigitalServicesAiInfrastructureComponent implements OnInit, OnDestroy {
    infrastructureForm!: FormGroup;
    private formSubscription: Subscription | undefined;
    locationOptions: Signal<
        {
            label: string;
            value: string;
        }[]
    > = computed(() => {
        const countries = [];
        const countryMap = this.digitalServiceStore.countryMap();
        for (const key in countryMap) {
            countries.push({
                label: countryMap[key],
                value: countryMap[key],
            });
        }

        countries.sort((a, b) => a.label.localeCompare(b.label));
        return countries;
    });
    digitalService: DigitalService = {} as DigitalService;
    public userService = inject(UserService);
    typesOptions: EcomindType[] = [];
    writeRight: boolean = false;
    private readonly destroyRef = inject(DestroyRef);
    constructor(
        private readonly fb: FormBuilder,
        private readonly messageService: MessageService,
        private readonly aiFormsStore: AIFormsStore,
        private readonly digitalServicesAiData: DigitalServicesAiDataService,
        private readonly translate: TranslateService,
        private readonly digitalServicesDataService: DigitalServicesDataService,
        private readonly route: ActivatedRoute,
        private readonly digitalServiceStore: DigitalServiceStoreService,
    ) {}

    ngOnInit() {
        (async () => {
            this.userService.isAllowedEcoMindAiWrite$
                .pipe(take(1))
                .subscribe((isAllowed) => {
                    if (isAllowed) {
                        this.writeRight = true;
                    }
                });

            // Load countries from API
            await this.loadEcomindTypes();

            const defaultInfrastructureType =
                this.typesOptions.find((t) => t.value === "Server") ??
                this.typesOptions[0];

            //set default value
            this.infrastructureForm = this.fb.group({
                infrastructureType: [null, Validators.required],
                nbCpuCores: [{ value: null, disabled: true }, Validators.min(0)],
                nbGpu: [
                    { value: null, disabled: true },
                    [Validators.required, Validators.min(0)],
                ],
                gpuMemory: [
                    { value: null, disabled: true },
                    [Validators.required, Validators.min(0)],
                ],
                ramSize: [
                    { value: null, disabled: true },
                    [Validators.required, Validators.min(0)],
                ],
                pue: [null, [Validators.required, Validators.min(1)]],
                complementaryPue: [
                    { value: 1, disabled: true },
                    [Validators.required, Validators.min(1)],
                ],
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
                complementaryPue: 1,
                location: "France",
            };
            //to get it only one time
            if (!this.aiFormsStore.getInfrastructureChange() && uid) {
                const data = await lastValueFrom(
                    this.digitalServicesAiData.getAiInfrastructure(uid),
                );
                if (data) {
                    const selectedType = this.typesOptions.find(
                        (t) => t.value === data.infrastructureType.value,
                    );
                    this.infrastructureForm.patchValue({
                        ...data,
                        infrastructureType: selectedType ?? defaultInfrastructureType,
                    });
                    this.handlingValueChangesForCalculateButton();
                } else {
                    //set the value
                    this.infrastructureForm.patchValue(defaultData);
                    //save the value
                    this.aiFormsStore.setInfrastructureFormData(defaultData);
                    this.handlingValueChangesForCalculateButton();
                }
            } else {
                const data = this.aiFormsStore.getInfrastructureFormData();
                this.infrastructureForm.patchValue(data ?? defaultData);
                this.handlingValueChangesForCalculateButton();
            }

            // Save data whenever changes are made
            this.formSubscription = this.infrastructureForm.valueChanges.subscribe(() => {
                this.aiFormsStore.setInfrastructureChange(true);
                // Get all values from the form including disabled ones
                const formData = this.infrastructureForm.getRawValue();
                this.aiFormsStore.setInfrastructureFormData(
                    formData as AIInfrastructureForm,
                );
            });

            this.userService.isAllowedEcoMindAiWrite$
                .pipe(take(1))
                .subscribe((isAllowed) => {
                    if (!isAllowed) {
                        this.infrastructureForm.disable({ emitEvent: false });
                    }
                });
        })();
    }

    async handlingValueChangesForCalculateButton() {
        this.formSubscription = this.infrastructureForm.valueChanges.subscribe(() => {
            if (this.infrastructureForm.valid && this.infrastructureForm.dirty) {
                this.digitalServiceStore.setEcoMindEnableCalcul(true);
            } else {
                this.digitalServiceStore.setEcoMindEnableCalcul(false);
            }
        });
    }

    async loadEcomindTypes() {
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
