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
    input,
    Input,
    OnInit,
    Output,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { ConfirmationService, MessageService } from "primeng/api";
import { finalize, lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { Note } from "src/app/core/interfaces/note.interface";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-digital-services-footprint-header",
    templateUrl: "./digital-services-footprint-header.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesFootprintHeaderComponent implements OnInit {
    protected readonly global = inject(GlobalStoreService);
    public digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() digitalService: DigitalService = {} as DigitalService;
    @Input() isSharedDs = false;
    @Output() digitalServiceChange = new EventEmitter<DigitalService>();
    @Output() digitalMobileOptionsChange = new EventEmitter<boolean>();
    isZoom125 = computed(() => this.global.zoomLevel() >= 125);
    isMobile = computed(() => this.global.mobileView());
    sidebarVisible: boolean = false;
    importSidebarVisible = false;
    selectedOrganizationName = "";
    selectedOrganizationId!: number;
    selectedWorkspaceName = "";
    organization!: Organization;
    isEcoMindEnabledForCurrentOrganization: boolean = false;
    isEcoMindAi = input<boolean>(false);
    showKebabMenu = false;
    displayLinkCreatePopup = false;
    shareLink = "";
    expiryDate: Date | null = null;

    private readonly destroyRef = inject(DestroyRef);

    constructor(
        private readonly digitalServicesData: DigitalServicesDataService,
        private readonly router: Router,
        private readonly confirmationService: ConfirmationService,
        private readonly translate: TranslateService,
        public readonly userService: UserService,
        private readonly messageService: MessageService,
        private readonly aiFormsStore: AIFormsStore,
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                this.digitalService = res;
                this.digitalServiceStore.setDigitalService(this.digitalService);
            });

        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization) => {
                this.selectedOrganizationName = organization.name;
                this.organization = organization;
                this.isEcoMindEnabledForCurrentOrganization = organization.ecomindai;
            });
        this.userService.currentWorkspace$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((workspace: Workspace) => {
                this.selectedWorkspaceName = workspace.name;
            });
        //to reset the form when a new digitalService is set
        if (this.digitalService.isAi) {
            this.aiFormsStore.setParameterChange(false);
            this.aiFormsStore.setInfrastructureChange(false);
            this.aiFormsStore.clearForms();
        }
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

    changePageToDigitalServices() {
        let [_, _1, organization, _2, workspace, serviceType] =
            this.router.url.split("/");
        // serviceType can be 'digital-services' or 'eco-mind-ai'
        if (serviceType === "eco-mind-ai") {
            return `/organizations/${organization}/workspaces/${workspace}/eco-mind-ai`;
        } else {
            return `/organizations/${organization}/workspaces/${workspace}/digital-services`;
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
            const filename = `g4it_${this.selectedOrganizationName}_${this.selectedWorkspaceName}_${this.digitalService.uid}_export-result-files`;
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

    shareDs(): void {
        this.displayLinkCreatePopup = !this.displayLinkCreatePopup;

        if (!this.shareLink && this.displayLinkCreatePopup) {
            this.global.setLoading(true);
            this.digitalServicesData
                .copyUrl(this.digitalService.uid)
                .pipe(
                    takeUntilDestroyed(this.destroyRef),
                    finalize(() => {
                        this.global.setLoading(false);
                    }),
                )
                .subscribe((res) => {
                    this.shareLink = res.url;
                    this.expiryDate = new Date(res.expiryDate.toString() + "Z");
                    if (!this.digitalService.isShared) {
                        this.digitalServicesData.get(this.digitalService.uid).subscribe();
                    }
                });
        }
    }
}
