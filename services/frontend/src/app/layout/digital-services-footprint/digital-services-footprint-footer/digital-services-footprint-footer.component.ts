import {
    Component,
    computed,
    DestroyRef,
    effect,
    EventEmitter,
    inject,
    Input,
    Output,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-digital-services-footprint-footer",
    templateUrl: "./digital-services-footprint-footer.component.html",
})
export class DigitalServicesFootprintFooterComponent {
    isEcoMindAi: boolean = false;
    @Input() set isAi(value: boolean) {
        this.isEcoMindAi = value;
    }
    @Input() digitalService: DigitalService = {} as DigitalService;
    @Output() updateEnableCalculation = new EventEmitter<boolean>();
    protected readonly userService = inject(UserService);
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly digitalServiceBusinessService = inject(
        DigitalServiceBusinessService,
    );
    private readonly aiFormsStore = inject(AIFormsStore);
    private readonly global = inject(GlobalStoreService);
    private readonly digitalServicesData = inject(DigitalServicesDataService);
    private readonly router = inject(Router);
    private readonly messageService = inject(MessageService);
    private readonly translate = inject(TranslateService);
    private readonly digitalServicesAiData = inject(DigitalServicesAiDataService);
    enableCalcul = computed(() => {
        const digitalService = this.digitalServiceStore.digitalService();

        if (this.digitalServiceStore.enableCalcul()) return true;

        const hasInPhysicalEquipments =
            this.digitalServiceStore.inPhysicalEquipments().length > 0;
        const hasInVirtualEquipments =
            this.digitalServiceStore.inVirtualEquipments().length > 0;

        const isUpdate =
            digitalService.lastCalculationDate == null
                ? true
                : digitalService.lastUpdateDate > digitalService.lastCalculationDate;

        if (
            (isUpdate && (hasInPhysicalEquipments || hasInVirtualEquipments)) ||
            this.isEcoMindAi
        ) {
            return true;
        }
        return false;
    });

    constructor() {
        effect(() => {
            this.updateEnableCalculation.emit(this.enableCalcul());
        });
    }

    ngOnInit() {
        this.digitalServicesData.digitalService$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                this.digitalService = res;
                this.digitalServiceStore.setDigitalService(this.digitalService);
            });

        this.digitalServiceBusinessService.launchCalcul$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => this.launchCalcul());
    }

    async launchCalcul() {
        if (this.isEcoMindAi) {
            await this.handleSave();
            if (
                !this.aiFormsStore.getInfrastructureFormData() ||
                !this.aiFormsStore.getParametersFormData()
            ) {
                return;
            }
        }
        this.global.setLoading(true);
        await firstValueFrom(
            this.digitalServicesData.launchEvaluating(this.digitalService.uid),
        );

        this.digitalService = await lastValueFrom(
            this.digitalServicesData.get(this.digitalService.uid),
        );
        this.global.setLoading(false);
        this.digitalServiceStore.setEnableCalcul(false);
        const urlSegments = this.router.url.split("/").slice(1);
        if (urlSegments.length > 3) {
            const subscriber = urlSegments[1];
            const organization = urlSegments[3];
            // Ensure digitalServiceId is not undefined or null
            const digitalServiceId = this.digitalService?.uid;

            if (digitalServiceId) {
                if (this.isEcoMindAi) {
                    this.router
                        .navigateByUrl("/", { skipLocationChange: true })
                        .then(() => {
                            this.router.navigate([
                                `/subscribers/${subscriber}/organizations/${organization}/eco-mind-ai/${digitalServiceId}/footprint/dashboard`,
                            ]);
                        });
                } else {
                    this.router
                        .navigateByUrl("/", { skipLocationChange: true })
                        .then(() => {
                            this.router.navigate([
                                `/subscribers/${subscriber}/organizations/${organization}/digital-services/${digitalServiceId}/footprint/dashboard`,
                            ]);
                        });
                }
            }
        }
    }

    async handleSave() {
        const parametersData = this.aiFormsStore.getParametersFormData();
        const infrastructureData = this.aiFormsStore.getInfrastructureFormData();

        // Check that both forms are complete
        if (!parametersData || !infrastructureData) {
            this.messageService.add({
                severity: "warn",
                summary: this.translate.instant("common.attention"),
                detail: this.translate.instant(
                    "eco-mind-ai.ai-parameters.fill-all-fields",
                ),
            });
            return;
        }

        // Check that all required fields have been completed
        const requiredParametersFields = [
            "modelName",
            "nbParameters",
            "framework",
            "quantization",
            "numberUserYear",
            "averageNumberRequest",
            "averageNumberToken",
        ] as const;

        const requiredInfrastructureFields = [
            "infrastructureType",
            "nbCpuCores",
            "nbGpu",
            "gpuMemory",
            "ramSize",
            "pue",
            "complementaryPue",
            "location",
        ] as const;

        const missingParametersFields = requiredParametersFields.filter(
            (field) =>
                parametersData[field] === undefined ||
                parametersData[field] === null ||
                parametersData[field] === "",
        );
        const missingInfrastructureFields = requiredInfrastructureFields.filter(
            (field) => {
                const value = infrastructureData[field];
                // For numeric fields, 0 is accepted as the valid value.
                if (typeof value === "number") {
                    return value === undefined || value === null;
                }
                // For other string fields, check as before
                return value === undefined || value === null || value === "";
            },
        );

        if (
            missingParametersFields.length > 0 ||
            missingInfrastructureFields.length > 0
        ) {
            let missingFieldsMessage =
                this.translate.instant("eco-mind-ai.ai-parameters.missing-fields") +
                " :\n";

            if (missingParametersFields.length > 0) {
                missingFieldsMessage +=
                    "\n" +
                    this.translate.instant("eco-mind-ai.ai-parameters.parameters-form") +
                    " :\n" +
                    missingParametersFields.join(", ");
            }

            if (missingInfrastructureFields.length > 0) {
                missingFieldsMessage +=
                    "\n" +
                    this.translate.instant(
                        "eco-mind-ai.ai-parameters.infrastructure-form",
                    ) +
                    " :\n" +
                    missingInfrastructureFields.join(", ");
            }

            this.messageService.add({
                severity: "warn",
                summary: this.translate.instant("common.attention"),
                detail: missingFieldsMessage,
            });
            return;
        }

        const digitalServiceUid = this.digitalService?.uid;

        if (!digitalServiceUid) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.error"),
                detail: this.translate.instant(
                    "eco-mind-ai.ai-parameters.service-id-missing",
                ),
            });
            return;
        }

        this.global.setLoading(true);

        // Save both forms
        await Promise.all([
            firstValueFrom(
                this.digitalServicesAiData.saveAiInfrastructure(
                    digitalServiceUid,
                    infrastructureData,
                ),
            ),
            firstValueFrom(
                this.digitalServicesAiData.saveAiParameters(
                    digitalServiceUid,
                    parametersData,
                ),
            ),
        ])
            .then(() => {
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.success"),
                    detail: this.translate.instant(
                        "eco-mind-ai.ai-parameters.save-success",
                    ),
                });
                this.digitalServiceStore.setEnableCalcul(true);
            })
            .catch((error) => {
                this.messageService.add({
                    severity: "error",
                    summary: this.translate.instant("common.error"),
                    detail: this.translate.instant(
                        "eco-mind-ai.ai-parameters.save-error",
                    ),
                });
            })
            .finally(() => {
                this.global.setLoading(false);
            });
    }
}
