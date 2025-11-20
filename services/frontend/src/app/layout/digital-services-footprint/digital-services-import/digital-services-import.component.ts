import {
    Component,
    DestroyRef,
    ElementRef,
    EventEmitter,
    inject,
    OnDestroy,
    OnInit,
    Output,
    signal,
    ViewChild,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormControl, FormGroup } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { sortByProperty } from "sort-by-property";
import {
    FileDescription,
    TemplateFileDescription,
} from "src/app/core/interfaces/file-system.interfaces";
import { TaskRest } from "src/app/core/interfaces/inventory.interfaces";
import { CustomSidebarMenuForm } from "src/app/core/interfaces/sidebar-menu-form.interface";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { FileSystemBusinessService } from "src/app/core/service/business/file-system.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { Constants } from "src/constants";

@Component({
    selector: "app-digital-services-import",
    templateUrl: "./digital-services-import.component.html",
    styleUrl: "./digital-services-import.component.scss",
})
export class DigitalServicesImportComponent implements OnInit, OnDestroy {
    private readonly destroyRef = inject(DestroyRef);
    private readonly userService = inject(UserService);
    private readonly fileSystemBusinessService = inject(FileSystemBusinessService);
    private readonly inDatacentersService = inject(InDatacentersService);
    public readonly digitalServiceStore = inject(DigitalServiceStoreService);
    importDetails: CustomSidebarMenuForm = this.buildImportDetails();

    private buildImportDetails(): CustomSidebarMenuForm {
        const common = {
            subTitle: this.translate.instant("common.optional"),
            description: this.translate.instant("common.no-document-upload"),
            iconClass: "pi pi-exclamation-circle",
            optional: true,
        };

        const menuConfigs: { titleKey: string; textKey: string }[] = [
            {
                titleKey: "digital-services-terminals.devices",
                textKey: "digital-services-import.terminal-text",
            },
            {
                titleKey: "digital-services.Network",
                textKey: "digital-services-import.network-text",
            },
            {
                titleKey: "digital-services.Server",
                textKey: "digital-services-import.non-cloud-text",
            },
            {
                titleKey: "digital-services.CloudService",
                textKey: "digital-services-import.cloud-text",
            },
        ];

        return {
            menu: menuConfigs.map((c) => ({
                ...common,
                title: this.translate.instant(c.titleKey),
                descriptionText: this.translate.instant(c.textKey),
            })),
            form: [
                { name: "terminal" },
                { name: "network" },
                { name: "nonCloud" },
                { name: "cloud" },
            ],
        };
    }

    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();

    selectedMenuIndex: number | null = null;

    tasks: TaskRest[] = [];
    tableTasks: TaskRest[] = [];
    digitalServicesId = this.route.snapshot.paramMap.get("digitalServiceVersionId") ?? "";
    templateFilesDescription: TemplateFileDescription[] = [];
    templateFileVisible = signal<TemplateFileDescription[]>([]);
    dataModel: TemplateFileDescription | undefined;
    anyRejectedFiles = false;
    selectedWorkspace!: string;
    selectedOrganization!: string;
    digitalServiceInterval: any;
    waitingLoop = 10000;
    toReloadDigitalService = false;
    @ViewChild("addFocus", { static: false }) addFocusElement!: ElementRef;
    constructor(
        private readonly translate: TranslateService,
        protected readonly templateFileService: TemplateFileService,
        private readonly route: ActivatedRoute,
        private readonly digitalServicesData: DigitalServicesDataService,
    ) {}

    importForm = new FormGroup({
        terminal: new FormControl<string | undefined>(undefined),
        network: new FormControl<string | undefined>(undefined),
        nonCloud: new FormControl<string | undefined>(undefined),
        cloud: new FormControl<string | undefined>(undefined),
    });

    ngOnInit(): void {
        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization.name;
            });
        this.userService.currentWorkspace$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((workspace: Workspace) => {
                this.selectedWorkspace = workspace.name;
            });
        this.getTemplates();
        this.getDigitalServiceStatus();
    }

    focusFirstTemplate() {
        if (this.addFocusElement) {
            this.addFocusElement.nativeElement.focus();
        }
    }

    getTemplates() {
        this.templateFileService
            .getTemplateFiles()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((templateFiles: FileDescription[]) => {
                templateFiles = templateFiles.filter(
                    (file) => !file.name.includes("inv_"),
                );
                if (templateFiles.length === 0) {
                    this.templateFilesDescription = [];
                    return;
                }
                this.templateFilesDescription =
                    this.templateFileService.transformTemplateFiles(templateFiles, true);
                for (const file of this.templateFilesDescription) {
                    if (file.type === "csv") {
                        file.displayFileName = this.translate.instant(
                            `digital-services-import.templates.${file.csvFileType}-template-file`,
                        );
                    } else {
                        file.displayFileName = this.translate.instant(
                            "digital-services-import.templates.data-model",
                        );
                    }
                }
                this.templateFilesDescription.sort((a, b) => {
                    if (a.csvFileType === "virtual" && b.csvFileType === "virtual") {
                        return (
                            (b.name.includes("non_cloud") ? 1 : 0) -
                                (a.name.includes("non_cloud") ? 1 : 0) ||
                            a.name.localeCompare(b.name)
                        );
                    }
                    return (
                        Constants.FILE_TYPES.indexOf(a.csvFileType ?? "") -
                        Constants.FILE_TYPES.indexOf(b.csvFileType ?? "")
                    );
                });
                this.dataModel = this.templateFilesDescription.find((file) =>
                    file.name?.toLowerCase()?.includes("datamodel"),
                );
                this.selectTab(0);
                setTimeout(() => {
                    this.focusFirstTemplate();
                }, 100);
            });
    }

    downloadTemplateFile(selectedFileName: string) {
        this.templateFileService.getdownloadTemplateFile(selectedFileName);
    }

    async getDigitalServiceStatus() {
        const ds = await lastValueFrom(
            this.digitalServicesData.getDsTasks(this.digitalServicesId),
        );

        this.tasks = (ds.tasks ?? [])
            .filter((task) => task.type?.toUpperCase() === "LOADING")
            .map((task) => ({
                ...task,
                creationDate: new Date(`${task.creationDate}Z`),
                cssClass: this.getClassStatus(task.status, true),
                tooltip: this.getClassStatus(task.status, false),
            }));

        this.anyRejectedFiles = this.tasks.some((task) =>
            ["COMPLETED_WITH_ERRORS", "SKIPPED", "FAILED"].includes(task.status),
        );

        this.tasks.sort(sortByProperty("creationDate", "desc"));
        this.tableTasks = this.tasks.slice(0, 5);

        const lastTaskStatus = this.tasks[0]?.status;
        this.toReloadDigitalService =
            !Constants.EVALUATION_BATCH_COMPLETED_FAILED_STATUSES.includes(
                lastTaskStatus,
            );
        if (!this.toReloadDigitalService) {
            this.callInputApis();
        }
    }

    async loopLoadDigitalServices() {
        this.digitalServiceInterval = setInterval(async () => {
            if (!this.toReloadDigitalService) {
                clearInterval(this.digitalServiceInterval);
            } else {
                await this.getDigitalServiceStatus();
            }
        }, this.waitingLoop);
    }

    async callInputApis(): Promise<void> {
        const inDatacenters = await firstValueFrom(
            this.inDatacentersService.get(this.digitalServicesId),
        );
        this.digitalServiceStore.setInDatacenters(inDatacenters);
        this.digitalServiceStore.initInPhysicalEquipments(this.digitalServicesId);
        this.digitalServiceStore.initInVirtualEquipments(this.digitalServicesId);
    }

    getClassStatus(status: string, isCss: boolean): string {
        let cssClass = "";
        let toolTip = "";
        const defaultClasses = "status-tag";
        if (Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(status)) {
            cssClass = `yellow-tag ${defaultClasses}`;
            toolTip = this.translate.instant("common.running");
        } else if (status === "COMPLETED") {
            cssClass = `green-tag ${defaultClasses}`;
            toolTip = this.translate.instant("common.completed");
        } else if (status === "FAILED") {
            cssClass = `red-tag ${defaultClasses}`;
            toolTip = this.translate.instant("common.failed");
        } else if (status === "FAILED_HEADERS") {
            cssClass = `red-tag ${defaultClasses}`;
            toolTip = this.translate.instant("common.failed-headers");
        } else if (status === "COMPLETED_WITH_ERRORS" || status === "SKIPPED") {
            cssClass = `orange-tag ${defaultClasses}`;
            toolTip = this.translate.instant("common.completed-with-errors");
        } else {
            cssClass = `orange-tag bg-warning ${defaultClasses}`;
            toolTip = this.translate.instant("common.pending");
        }
        return isCss ? cssClass : toolTip;
    }

    async onFormSubmit(event: any) {
        if (event === "submit") {
            clearInterval(this.digitalServiceInterval);
            await this.getDigitalServiceStatus();
            this.loopLoadDigitalServices();
        }
    }

    previousTab(index: number) {
        if (index > 0) {
            this.selectTab(--index);
            this.focusFirstTemplate();
        }
    }

    nextTab(index: number) {
        if (index < this.importDetails["menu"].length - 1) {
            this.selectTab(++index);
            this.focusFirstTemplate();
        }
    }

    selectTab(index: number) {
        this.selectedMenuIndex = index;
        const files = this.templateFilesDescription;
        this.templateFileVisible.set(this.getSelectedTemplates(files));
        for (const [i, detail] of this.importDetails.menu.entries()) {
            detail.active = i === index;
        }
    }

    getSelectedTemplates(files: TemplateFileDescription[]): TemplateFileDescription[] {
        if (this.selectedMenuIndex === 0)
            return files.filter((file) =>
                file.name.includes("physical_equipment_terminal"),
            );
        else if (this.selectedMenuIndex === 1)
            return files.filter((file) =>
                file.name.includes("physical_equipment_network"),
            );
        else if (this.selectedMenuIndex === 2)
            return files.filter(
                (file) =>
                    ![
                        "virtual_equipment_cloud",
                        "physical_equipment_terminal",
                        "physical_equipment_network",
                        "datamodel",
                    ].some((type) => file.name?.toLowerCase()?.includes(type)),
            );

        return files.filter((file) => file.name.includes("virtual_equipment_cloud"));
    }

    closeSidebar() {
        this.selectTab(0);
        this.importForm.reset();
        this.sidebarVisibleChange.emit(false);
    }

    downloadFile(task: TaskRest) {
        if (task.status === "COMPLETED_WITH_ERRORS" || task.status === "SKIPPED") {
            this.downloadFileDs(String(task.id));
        } else {
            this.getTaskDetail(String(task.id));
        }
    }

    downloadFileDs(taskId: string) {
        this.fileSystemBusinessService.downloadFile(
            taskId,
            this.selectedOrganization,
            this.selectedWorkspace,
            this.digitalServicesId,
        );
    }

    getTaskDetail(taskId: string) {
        this.fileSystemBusinessService.getTaskDetail(taskId);
    }

    ngOnDestroy() {
        clearInterval(this.digitalServiceInterval);
    }
}
