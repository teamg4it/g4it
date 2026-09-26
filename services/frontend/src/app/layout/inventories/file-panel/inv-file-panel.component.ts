/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    ComponentRef,
    DestroyRef,
    EventEmitter,
    inject,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    signal,
    SimpleChanges,
    ViewChild,
    ViewContainerRef,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import {
    FormBuilder,
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import saveAs from "file-saver";
import { MessageService } from "primeng/api";
import { RadioButton, RadioButtonModule } from "primeng/radiobutton";
import { delay, Subject, switchMap, takeUntil, tap } from "rxjs";
import {
    FileDescription,
    FileType,
    TemplateFileDescription,
} from "src/app/core/interfaces/file-system.interfaces";
import {
    CreateInventory,
    Inventory,
    InventoryUpdateRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { WorkspaceReferenceDataService } from "src/app/core/service/data/workspace-reference-data.service";
import { Constants } from "src/constants";
import { SelectFileComponent } from "./select-file/select-file.component";

import { NgClass, NgTemplateOutlet } from "@angular/common";
import { Button } from "primeng/button";
import { DatePickerModule } from "primeng/datepicker";
import { InputTextModule } from "primeng/inputtext";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { AutofocusDirective } from "src/app/core/directives/auto-focus.directive";
import { CustomSidebarMenuForm } from "src/app/core/interfaces/sidebar-menu-form.interface";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { FormNavComponent } from "../../common/form-nav/form-nav.component";
import { InvMultiFileImportComponent } from "./inv-multi-file-import/inv-multi-file-import.component";

@Component({
    selector: "app-inv-file-panel",
    styleUrls: ["./inv-file-panel.component.scss"],
    templateUrl: "./inv-file-panel.component.html",
    standalone: true,
    imports: [
        FormsModule,
        RadioButtonModule,
        DatePickerModule,
        ReactiveFormsModule,
        InputTextModule,
        FormNavComponent,
        ScrollPanelModule,
        Button,
        TranslatePipe,
        NgTemplateOutlet,
        NgClass,
        InvMultiFileImportComponent,
        AutofocusDirective,
    ],
})
export class InvFilePanelComponent implements OnInit, OnDestroy, OnChanges {
    private readonly userService = inject(UserService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly workspaceReferenceDataService = inject(
        WorkspaceReferenceDataService,
    );
    protected readonly global = inject(GlobalStoreService);
    className: string = "default-calendar max-w-full";

    @ViewChild("uploaderContainer", { read: ViewContainerRef })
    uploaderContainer!: ViewContainerRef;
    @ViewChild(InvMultiFileImportComponent)
    invMultiFileImport?: InvMultiFileImportComponent;
    @Input() purpose: string = "";
    @Input() name: string = ""; // inventoryDate (for IS Type)
    @Input() inventoryId?: number = 0;
    @Input() allSimulations: Inventory[] = [];
    @Input() inventories: Inventory[] = [];

    @Output() sidebarPurposeChange: EventEmitter<any> = new EventEmitter();
    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();
    @Output() reloadInventoriesAndLoop = new EventEmitter<number>();

    @ViewChild("firstInputElement", { static: false }) firstInputElement:
        | RadioButton
        | undefined;

    importDetails: CustomSidebarMenuForm = this.buildImportDetails();
    selectedMenuIndex: number | null = null;
    private buildImportDetails(): CustomSidebarMenuForm {
        const common = {
            subTitle: this.translate.instant("common.optional"),
            description: this.translate.instant("common.no-document-upload"),
            iconClass: "pi pi-exclamation-circle",
            optional: true,
        };

        const menuConfigs: { titleKey: string; textKey: string; config?: any }[] = [
            {
                titleKey: "inventories.create-drawer.info.title",
                textKey: "inventories.create-drawer.info.description",
                config: {
                    subTitle: this.translate.instant("common.workspace.mandatory"),
                    description: this.translate.instant("common.no-month-name-choosen"),
                    iconClass: "pi pi-exclamation-circle",
                    active: true,
                },
            },
            {
                titleKey: "inventories.create-drawer.infra.title",
                textKey: "inventories.create-drawer.infra.description",
                config: common,
            },
            {
                titleKey: "inventories.create-drawer.end-user.title",
                textKey: "inventories.create-drawer.end-user.description",
                config: common,
            },
            {
                titleKey: "inventories.create-drawer.app-service.title",
                textKey: "inventories.create-drawer.app-service.description",
                config: common,
            },
            {
                titleKey: "inventories.create-drawer.external-services.title",
                textKey: "inventories.create-drawer.external-services.description",
                config: common,
            },
        ];

        return {
            menu: menuConfigs.map((c) => ({
                ...c.config,
                title: this.translate.instant(c.titleKey),
                descriptionText: this.translate.instant(c.textKey),
            })),
            form: [
                { name: "info" },
                { name: "infra" },
                { name: "endUser" },
                { name: "appServices" },
                { name: "externalServices" },
            ],
        };
    }

    importForm = new FormGroup({
        info: new FormControl<string | undefined>(undefined, [Validators.required]),
        infra: new FormControl<string | undefined>(undefined),
        endUser: new FormControl<string | undefined>(undefined),
        appServices: new FormControl<string | undefined>(undefined),
        externalServices: new FormControl<string | undefined>(undefined),
    });

    public fileTypes: FileType[] = [];
    invalidDates: Date[] = [];
    // true while a create/update/upload request is in flight, used to disable the submit button
    isSubmitting: boolean = false;
    // snapshot of name at sidebar open, used for the static "load files on" title
    initialName: string = "";
    // full inventory/simulation being edited, kept to build the updateInventory payload
    originalInventory?: Inventory;
    selectedType: string = Constants.INVENTORY_TYPE.INFORMATION_SYSTEM;
    // keeps the datepicker's displayed selection when the tab is destroyed/recreated on switch
    selectedDate: Date | null = null;
    inventoryDates: Date[] = [];
    simulationNames: string[] = [];
    inventoriesForm!: FormGroup;
    inventoryType = Constants.INVENTORY_TYPE;
    isFileUploaded = signal(false);
    allowedFileExtensions = [".csv", ".xlsx", ".ods"];

    ngUnsubscribe = new Subject<void>();

    private readonly uploaderOutpoutHandlerReset$ = new Subject<void>();
    arrayComponents: Array<ComponentRef<SelectFileComponent>> = [];

    templateFiles: TemplateFileDescription[] = [];

    templateFileVisible = signal<TemplateFileDescription[]>([]);
    constructor(
        private readonly inventoryService: InventoryDataService,
        private readonly loadingService: LoadingDataService,
        private readonly messageService: MessageService,
        private readonly translate: TranslateService,
        private readonly formBuilder: FormBuilder,
        protected readonly templateFileService: TemplateFileService,
    ) {}

    ngOnInit(): void {
        this.fileTypes = [
            {
                value: "DATACENTER",
                text: this.translate.instant("inventories.type.dc"),
            },
            {
                value: "EQUIPEMENT_PHYSIQUE",
                text: this.translate.instant("inventories.type.eq-phys"),
            },
            {
                value: "EQUIPEMENT_VIRTUEL",
                text: this.translate.instant("inventories.type.eq-virt"),
            },
            {
                value: "APPLICATION",
                text: this.translate.instant("inventories.type.app"),
            },
        ];
        this.inventoriesForm = this.formBuilder.group({
            name: ["", [Validators.pattern(/^[^<>]+$/), Validators.maxLength(255)]],
        });
        this.initialName = this.name;
        this.getTemplateFiles();
    }

    // ngAfterViewInit(): void {
    //     for (const type of this.fileTypes) {
    //         this.addComponent(type);
    //     }
    // }

    ngOnChanges(changes: SimpleChanges) {
        this.invalidDates = [];
        for (const inventory of this.inventories) {
            // exclude the inventory being edited so its own month isn't blocked
            if (this.purpose !== "new" && inventory.id === this.inventoryId) {
                continue;
            }
            const month = inventory.date!.getMonth();
            let year = inventory.date!.getFullYear();
            for (let day = 1; day < 32; day++) {
                this.invalidDates.push(new Date(year, month, day));
            }
        }

        if (this.purpose !== "new" && this.inventoryId) {
            const editedInventory = this.inventories.find(
                (inventory) => inventory.id === this.inventoryId,
            );
            const editedSimulation = this.allSimulations.find(
                (simulation) => simulation.id === this.inventoryId,
            );
            if (editedInventory) {
                this.selectedType = Constants.INVENTORY_TYPE.INFORMATION_SYSTEM;
                this.selectedDate = editedInventory.date ?? null;
                this.originalInventory = editedInventory;
            } else if (editedSimulation) {
                this.selectedType = Constants.INVENTORY_TYPE.SIMULATION;
                this.originalInventory = editedSimulation;
            }
        }

        // keeps the info tab marked "completed" when a name/date is already prefilled on edit
        this.syncInfoControl();
    }

    getTemplateFiles() {
        this.templateFileService
            .getTemplateFiles()
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((templateFiles: FileDescription[]) => {
                // conditon added to not include workpsace data model and to use platform data model instead
                console.log(templateFiles);
                templateFiles = templateFiles.filter(
                    (file) =>
                        !file.name.includes("ds_") &&
                        !file.name.includes(Constants.DATA_MODEL_CONDITION),
                );
                if (templateFiles.length === 0) {
                    this.templateFiles = [];
                    return;
                }
                this.templateFiles = this.templateFileService.transformTemplateFiles(
                    templateFiles,
                    false,
                );
                this.selectTab(0);
            });
    }

    checkForDuplicate() {
        return this.allSimulations.some(
            (inventory) =>
                inventory.name == this.name?.trim() && inventory.id !== this.inventoryId,
        );
    }

    get inventoriesFormControls() {
        return this.inventoriesForm.controls;
    }

    deleteComponent(index: number) {
        this.arrayComponents.at(index)?.destroy();
        this.arrayComponents.splice(index, 1);
        for (const [index, { instance }] of this.arrayComponents.entries()) {
            instance.index = index;
        }
    }

    addComponent(type = this.fileTypes[0]) {
        const componentRef = this.uploaderContainer.createComponent(SelectFileComponent);
        componentRef.setInput("fileTypes", this.fileTypes);
        componentRef.setInput("allowedFileExtensions", this.allowedFileExtensions);
        componentRef.instance.type = type;
        this.arrayComponents.push(componentRef);
        this.uploaderOutpoutHandlerReset$.next();
        for (const [index, { instance }] of this.arrayComponents.entries()) {
            instance.index = index;
            instance.outDelete
                .asObservable()
                .pipe(takeUntil(this.uploaderOutpoutHandlerReset$))
                .subscribe(() => {
                    this.deleteComponent(instance.index);
                    this.checkfileUploaded();
                });
            instance.fileSelected
                .asObservable()
                .pipe(takeUntil(this.uploaderOutpoutHandlerReset$))
                .subscribe(() => {
                    this.checkfileUploaded();
                });
        }
    }

    checkfileUploaded() {
        const isFileUploaded = this.arrayComponents.some(
            (compRef) => compRef?.instance?.file,
        );
        this.isFileUploaded.set(isFileUploaded);
    }

    submitFormData() {
        this.isSubmitting = true;
        this.global.setLoading(true);
        if (this.name === "") {
            this.className = "ng-invalid ng-dirty";
            this.isSubmitting = false;
            this.global.setLoading(false);
            return;
        }
        const formData = this.invMultiFileImport?.getFormData() ?? new FormData();
        const hasFiles = Array.from(formData.keys()).length !== 0;

        if (this.purpose === "new") {
            const creationObj: CreateInventory = {
                name: this.name,
                type: this.selectedType,
            };
            this.inventoryService.createInventory(creationObj).subscribe({
                next: (response) => {
                    this.messageService.add({
                        severity: "success",
                        summary: this.translate.instant(
                            "inventories.creation-successful",
                        ),
                        detail: `${this.translate.instant("inventories.inventory")} ${
                            this.name
                        } ${this.translate.instant("inventories.created")}`,
                    });
                    if (!hasFiles) {
                        this.isSubmitting = false;
                        this.global.setLoading(false);
                        this.reloadInventoriesAndLoop.emit(response.id);
                        this.close();
                    } else {
                        this.uploadAndLaunchLoading(formData, response.id);
                    }
                },
                error: (error) => {
                    this.isSubmitting = false;
                    this.global.setLoading(false);
                },
            });
            return;
        }

        if (this.name !== this.initialName && this.originalInventory) {
            const inventoryRest: InventoryUpdateRest = {
                id: this.originalInventory.id,
                name: this.name,
                note: this.originalInventory.note,
                criteria: this.originalInventory.criteria!,
                enableDataInconsistency: this.originalInventory.enableDataInconsistency,
            };
            this.inventoryService.updateInventory(inventoryRest).subscribe({
                next: () => this.uploadOrClose(formData, hasFiles),
                error: () => {
                    this.isSubmitting = false;
                    this.global.setLoading(false);
                },
            });
            return;
        }

        this.uploadOrClose(formData, hasFiles);
    }

    private uploadOrClose(formData: FormData, hasFiles: boolean) {
        if (hasFiles) {
            this.uploadAndLaunchLoading(formData, this.inventoryId);
        } else {
            this.isSubmitting = false;
            this.global.setLoading(false);
            this.close();
        }
    }

    onSelectToDate(date: Date) {
        this.selectedDate = date;
        const monthNumber = `${date.getMonth() + 1}`;
        this.name = `${monthNumber.padStart(2, "0")}-${date.getFullYear()}`;
        this.className = "default-calendar";
        this.syncInfoControl();
    }

    onClearDate() {
        this.selectedDate = null;
        this.name = "";
        this.className = "default-calendar";
        this.syncInfoControl();
    }

    onSimulationNameChange(value: string): void {
        this.name = value;
        this.inventoriesForm.controls["name"].setValue(value);
        this.syncInfoControl();
    }

    onInventoryTypeChanged(type: string): void {
        if (this.purpose !== "new") {
            return;
        }
        this.selectedType = type;
        this.name = "";
        this.selectedDate = null;
        this.syncInfoControl();
    }

    // keeps the mandatory "info" tab's completed status in sync with the entered name/date
    private syncInfoControl(): void {
        this.importForm.controls.info.setValue(this.name?.trim() || undefined);
    }

    uploadAndLaunchLoading(formData: FormData, inventoryId: number = 0) {
        this.loadingService
            .launchLoadInputFiles(inventoryId, formData)
            .pipe(delay(500))
            .subscribe({
                next: () => {
                    this.isSubmitting = false;
                    this.global.setLoading(false);
                    this.sidebarVisibleChange.emit(false);
                    this.reloadInventoriesAndLoop.emit(inventoryId);
                    this.close();
                },
                error: () => {
                    this.isSubmitting = false;
                    this.global.setLoading(false);
                    this.sidebarPurposeChange.emit("upload");
                },
            });
    }

    clearSidePanel() {
        for (const component of this.arrayComponents) {
            component.destroy();
        }
        this.arrayComponents = [];
        for (const type of this.fileTypes) {
            this.addComponent(type);
        }
    }

    close() {
        if (this.purpose === "new") {
            this.name = "";
            this.selectedDate = null;
        }
        this.sidebarVisibleChange.emit(false);
        this.clearSidePanel();
    }

    downloadTemplateFile(selectedFileName: string) {
        this.templateFileService.getdownloadTemplateFile(selectedFileName);
    }

    async downloadWorkspaceReferenceData() {
        this.userService.currentWorkspace$
            .pipe(
                switchMap((workSpace) =>
                    this.inventoryService.downloadWorkspaceSettingsZip().pipe(
                        tap((blob) => {
                            saveAs(blob, `workspace-referential-${workSpace.id}.zip`);
                        }),
                    ),
                ),
                takeUntilDestroyed(this.destroyRef),
            )
            .subscribe();
    }

    selectTab(index: number) {
        this.selectedMenuIndex = index;
        const files = this.templateFiles;
        console.log(files);
        this.templateFileVisible.set(this.getSelectedTemplates(files));
        for (const [i, detail] of this.importDetails.menu.entries()) {
            detail.active = i === index;
        }
    }

    getSelectedTemplates(files: TemplateFileDescription[]): TemplateFileDescription[] {
        if (this.selectedMenuIndex === 0)
            return files.filter((file) =>
                ["datamodel"].some((type) => file.name?.toLowerCase()?.includes(type)),
            );
        else if (this.selectedMenuIndex === 1)
            return files.filter((file) =>
                ["datamodel", "datacenter", "physicalequipment"].some((type) =>
                    file.name?.toLowerCase()?.includes(type),
                ),
            );
        else if (this.selectedMenuIndex === 2)
            return files.filter((file) =>
                ["datamodel", "physicalequipment"].some((type) =>
                    file.name?.toLowerCase()?.includes(type),
                ),
            );
        else if (this.selectedMenuIndex === 3)
            return files.filter((file) =>
                ["datamodel", "virtualequipment", "application"].some((type) =>
                    file.name?.toLowerCase()?.includes(type),
                ),
            );

        return files.filter((file) =>
            ["datamodel", "virtualequipment", "AIservices"].some((type) =>
                file.name?.toLowerCase()?.includes(type),
            ),
        );
    }

    closeSidebar() {
        this.sidebarVisibleChange.emit(false);
    }

    previousTab(index: number) {
        if (index > 0) {
            this.selectTab(--index);
        }
    }

    nextTab(index: number) {
        if (index < this.importDetails["menu"].length - 1) {
            this.selectTab(++index);
        }
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
