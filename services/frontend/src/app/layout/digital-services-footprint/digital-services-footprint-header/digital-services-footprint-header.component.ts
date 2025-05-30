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
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
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
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";

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
    selectedSubscriberName = "";
    selectedOrganizationId!: number;
    selectedOrganizationName = "";
    displayPopup = false;
    selectedCriteria: string[] = [];
    organization: OrganizationWithSubscriber = {} as OrganizationWithSubscriber;
    subscriber!: Subscriber;

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
        private digitalServiceBusinessService: DigitalServiceBusinessService,
        private inVirtualEquipmentsService: InVirtualEquipmentsService,
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
            .subscribe();

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
        let [_, _1, subscriber, _2, organization] = this.router.url.split("/");
        return `/subscribers/${subscriber}/organizations/${organization}/digital-services`;
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
}
