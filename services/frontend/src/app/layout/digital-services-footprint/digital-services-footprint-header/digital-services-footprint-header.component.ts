/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    computed,
    DestroyRef,
    EventEmitter,
    inject,
    Input,
    OnInit,
    Output,
    ViewChild,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { ConfirmationService, MessageService } from "primeng/api";
import { finalize, firstValueFrom, lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { Note } from "src/app/core/interfaces/note.interface";
import { Organization, Subscriber } from "src/app/core/interfaces/user.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServicesAiInfrastructureComponent } from "../digital-services-ai-infrastructure/digital-services-ai-infrastructure.component";
import { DigitalServicesAiParametersComponent } from "../digital-services-ai-parameters/digital-services-ai-parameters.component";

@Component({
    selector: "app-digital-services-footprint-header",
    templateUrl: "./digital-services-footprint-header.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesFootprintHeaderComponent implements OnInit {
    private readonly global = inject(GlobalStoreService);
    public digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() digitalService: DigitalService = {} as DigitalService;
    @Output() digitalServiceChange = new EventEmitter<DigitalService>();
    isZoom125 = computed(() => this.global.zoomLevel() >= 125);
    sidebarVisible: boolean = false;
    importSidebarVisible = false;
    selectedSubscriberName = "";
    selectedOrganizationId!: number;
    selectedOrganizationName = "";
    subscriber!: Subscriber;
    isEcoMindEnabledForCurrentSubscriber: boolean = false;
    isEcoMindAi: boolean = false;
    @Input() set isAi(value: boolean) {
        this.isEcoMindAi = value;
    }

    @ViewChild(DigitalServicesAiParametersComponent) aiParametersComponent:
        | DigitalServicesAiParametersComponent
        | undefined;
    @ViewChild(DigitalServicesAiInfrastructureComponent) aiInfrastructureComponent:
        | DigitalServicesAiInfrastructureComponent
        | undefined;

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
    private readonly destroyRef = inject(DestroyRef);

    constructor(
        private readonly digitalServicesData: DigitalServicesDataService,
        private readonly router: Router,
        private readonly confirmationService: ConfirmationService,
        private readonly translate: TranslateService,
        public readonly userService: UserService,
        private readonly messageService: MessageService,
        private readonly digitalServiceBusinessService: DigitalServiceBusinessService,
        private readonly inVirtualEquipmentsService: InVirtualEquipmentsService,
        private readonly aiFormsStore: AIFormsStore,
        private readonly digitalServicesAiData: DigitalServicesAiDataService,
        private readonly route: ActivatedRoute,
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                this.digitalService = res;
                this.digitalServiceStore.setDigitalService(this.digitalService);
            });

        this.userService.currentSubscriber$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((subscriber: Subscriber) => {
                this.selectedSubscriberName = subscriber.name;
                this.subscriber = subscriber;
                this.isEcoMindEnabledForCurrentSubscriber = subscriber.ecomindai;
            });
        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: Organization) => {
                this.selectedOrganizationName = organization.name;
            });
        //to reset the form when a new digitalService is set
        if (this.digitalService.isAi) {
            this.aiFormsStore.setParameterChange(false);
            this.aiFormsStore.setInfrastructureChange(false);
            this.aiFormsStore.clearForms();
        }

        this.digitalServiceBusinessService.launchCalcul$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => this.launchCalcul());
    }

    onNameUpdate(digitalServiceName: string) {
        if (digitalServiceName != "") {
            this.digitalService.name = digitalServiceName;
            this.digitalServiceChange.emit(this.digitalService);
        }
    }

    confirmDelete(event: Event) {
        this.confirmationService.confirm({
            closeOnEscape: true,
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant(
                "digital-services.popup.delete-question",
            )} ${this.digitalService.name} ?
            ${this.translate.instant("digital-services.popup.delete-text")}`,
            icon: "pi pi-exclamation-triangle",
            accept: () => {
                this.global.setLoading(true);

                this.digitalServicesData
                    .delete(this.digitalService.uid)
                    .pipe(
                        takeUntilDestroyed(this.destroyRef),
                        finalize(() => {
                            this.global.setLoading(false);
                        }),
                    )
                    .subscribe(() =>
                        this.router.navigateByUrl(this.changePageToDigitalServices()),
                    );
            },
        });
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

    canLaunchCompute(hasCloudService: boolean): boolean {
        const hasNetworks = this.digitalService.networks?.length > 0;
        const hasTerminals = this.digitalService.terminals?.length > 0;
        const hasServers = this.digitalService.servers?.length > 0;

        const hasData = hasNetworks || hasTerminals || hasServers || hasCloudService;

        const hasDigitalServiceBeenUpdated =
            this.digitalService.lastCalculationDate == null
                ? true
                : this.digitalService.lastUpdateDate >
                  this.digitalService.lastCalculationDate;

        if (hasDigitalServiceBeenUpdated && hasData) {
            return true;
        }
        return false;
    }

    changePageToDigitalServices() {
        let [_, _1, subscriber, _2, organization, serviceType] =
            this.router.url.split("/");
        // serviceType can be 'digital-services' or 'eco-mind-ai'
        if (serviceType === "eco-mind-ai") {
            return `/subscribers/${subscriber}/organizations/${organization}/eco-mind-ai`;
        } else {
            return `/subscribers/${subscriber}/organizations/${organization}/digital-services`;
        }
    }

    noteSaveValue(event: any) {
        this.digitalService.note = {
            content: event,
        } as Note;

        this.digitalServicesData.update(this.digitalService).subscribe((res) => {
            this.sidebarVisible = false;
            this.messageService.add({
                severity: "success",
                summary: this.translate.instant("common.note.save"),
                sticky: false,
            });
        });
    }

    noteDelete() {
        this.digitalService.note = undefined;
        this.digitalServicesData.update(this.digitalService).subscribe((res) => {
            this.messageService.add({
                severity: "success",
                summary: this.translate.instant("common.note.delete"),
                sticky: false,
            });
        });
    }

    async exportData() {
        try {
            const filename = `g4it_${this.selectedSubscriberName}_${this.selectedOrganizationName}_${this.digitalService.uid}_export-result-files`;
            const blob: Blob = await lastValueFrom(
                this.digitalServicesData.downloadFile(this.digitalService.uid),
            );
            saveAs(blob, filename);
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
    }

    importData(): void {
        this.importSidebarVisible = true;
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
