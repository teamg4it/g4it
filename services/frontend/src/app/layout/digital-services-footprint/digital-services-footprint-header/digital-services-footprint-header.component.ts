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
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { ClipboardService } from "ngx-clipboard";
import { ConfirmationService, MessageService } from "primeng/api";
import { finalize, firstValueFrom, lastValueFrom, switchMap } from "rxjs";
import { OrganizationWithSubscriber } from "src/app/core/interfaces/administration.interfaces";
import {
    DigitalService,
    DSCriteriaRest,
} from "src/app/core/interfaces/digital-service.interfaces";
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
import { delay } from "src/app/core/utils/time";
import { DigitalServicesAiInfrastructureComponent } from "../digital-services-ai-infrastructure/digital-services-ai-infrastructure.component";
import { DigitalServicesAiParametersComponent } from "../digital-services-ai-parameters/digital-services-ai-parameters.component";

@Component({
    selector: "app-digital-services-footprint-header",
    templateUrl: "./digital-services-footprint-header.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesFootprintHeaderComponent implements OnInit {
    private global = inject(GlobalStoreService);
    public digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() digitalService: DigitalService = {} as DigitalService;
    @Output() digitalServiceChange = new EventEmitter<DigitalService>();
    sidebarVisible: boolean = false;
    sidebarDsVisible = false;
    isLinkCopied = false;
    selectedSubscriberName = "";
    selectedOrganizationId!: number;
    selectedOrganizationName = "";
    isShared = false;
    displayPopup = false;
    selectedCriteria: string[] = [];
    organization: OrganizationWithSubscriber = {} as OrganizationWithSubscriber;
    subscriber!: Subscriber;
    @Input() set isAi(value: boolean) {
        this.isEcoMindAi = value;
    }
    isEcoMindAi = false;

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

        if (isUpdate && (hasInPhysicalEquipments || hasInVirtualEquipments)) {
            return true;
        }
        return false;
    });
    private destroyRef = inject(DestroyRef);

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private router: Router,
        private confirmationService: ConfirmationService,
        private translate: TranslateService,
        public userService: UserService,
        private messageService: MessageService,
        private clipboardService: ClipboardService,
        private digitalServiceBusinessService: DigitalServiceBusinessService,
        private inVirtualEquipmentsService: InVirtualEquipmentsService,
        private aiFormsStore: AIFormsStore,
        private digitalServicesAiData: DigitalServicesAiDataService,
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$
            .pipe(
                takeUntilDestroyed(this.destroyRef),
                switchMap((res) => {
                    this.digitalService = res;
                    this.digitalServiceStore.setDigitalService(this.digitalService);
                    return this.inVirtualEquipmentsService.getByDigitalService(
                        this.digitalService.uid,
                    );
                }),
            )
            .subscribe(() => {
                this.digitalServiceIsShared();
            });

        this.userService.currentSubscriber$.subscribe((subscriber: Subscriber) => {
            this.selectedSubscriberName = subscriber.name;
            this.subscriber = subscriber;
        });
        this.userService.currentOrganization$.subscribe((organization: Organization) => {
            this.organization.subscriberName = this.subscriber.name;
            this.organization.subscriberId = this.subscriber.id;
            this.organization.organizationName = organization.name;
            this.organization.organizationId = organization.id;
            this.organization.status = organization.status;
            this.organization.dataRetentionDays = organization.dataRetentionDays!;
            this.organization.displayLabel = `${organization.name} - (${this.subscriber.name})`;
            this.organization.criteriaDs = organization.criteriaDs!;
            this.organization.criteriaIs = organization.criteriaIs!;
        });
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

    confirmUnlink(event: Event) {
        this.confirmationService.confirm({
            closeOnEscape: true,
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant(
                "digital-services.popup.delete-question-shared",
            )}`,
            icon: "pi pi-exclamation-triangle",
            accept: () => {
                this.global.setLoading(true);
                this.digitalServicesData
                    .unlink(this.digitalService.uid)
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
                this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
                    this.router.navigate([
                        `/subscribers/${subscriber}/organizations/${organization}/digital-services/${digitalServiceId}/footprint/dashboard`,
                    ]);
                });
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
        // serviceType sera 'digital-services' ou 'eco-mind-ai'
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

    async copyUrl() {
        this.isLinkCopied = true;
        const url = await firstValueFrom(
            this.digitalServicesData.copyUrl(this.digitalService.uid),
        );
        this.clipboardService.copy(url);

        await delay(10000);
        this.isLinkCopied = false;
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

    async digitalServiceIsShared() {
        const userId = (await firstValueFrom(this.userService.user$)).id;
        if (this.digitalService.creator?.id !== userId) {
            this.isShared = true;
        } else {
            this.isShared = false;
        }
    }

    displayPopupFct() {
        const defaultCriteria = Object.keys(this.global.criteriaList()).slice(0, 5);
        this.selectedCriteria =
            this.digitalService.criteria ??
            this.organization.criteriaDs ??
            this.subscriber.criteria ??
            defaultCriteria;
        this.displayPopup = true;
    }

    handleSaveDs(DSCriteria: DSCriteriaRest) {
        this.digitalServiceBusinessService
            .updateDsCriteria(this.digitalService.uid, DSCriteria)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => {
                this.digitalServicesData
                    .get(this.digitalService.uid)
                    .pipe(takeUntilDestroyed(this.destroyRef))
                    .subscribe();
                this.displayPopup = false;
                this.digitalServiceStore.setEnableCalcul(true);
            });
    }

    handleSave(): void {
        const parametersData = this.aiFormsStore.getParametersFormData();
        const infrastructureData = this.aiFormsStore.getInfrastructureFormData();

        // Vérifier si les deux formulaires sont complets
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

        // Vérifier que tous les champs requis sont remplis
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
            (field) =>
                infrastructureData[field] === undefined ||
                infrastructureData[field] === null ||
                infrastructureData[field] === "",
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

        // Sauvegarder les deux formulaires
        Promise.all([
            this.digitalServicesAiData
                .saveAiInfrastructure(digitalServiceUid, infrastructureData)
                .toPromise(),
            this.digitalServicesAiData
                .saveAiParameters(digitalServiceUid, parametersData)
                .toPromise(),
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
