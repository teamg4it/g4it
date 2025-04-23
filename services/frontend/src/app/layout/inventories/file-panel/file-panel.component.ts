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
    EventEmitter,
    Input,
    OnInit,
    Output,
    signal,
    SimpleChanges,
    ViewChild,
    ViewContainerRef,
} from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { MessageService } from "primeng/api";
import { RadioButton } from "primeng/radiobutton";
import { firstValueFrom, Subject, takeUntil } from "rxjs";
import {
    FileDescription,
    FileType,
    TemplateFileDescription,
} from "src/app/core/interfaces/file-system.interfaces";
import { CreateInventory, Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { extractFileName } from "src/app/core/utils/path";
import { delay } from "src/app/core/utils/time";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { SelectFileComponent } from "./select-file/select-file.component";

@Component({
    selector: "app-file-panel",
    templateUrl: "./file-panel.component.html",
})
export class FilePanelComponent implements OnInit {
    className: string = "default-calendar max-w-full";

    @ViewChild("uploaderContainer", { read: ViewContainerRef })
    uploaderContainer!: ViewContainerRef;
    @Input() purpose: string = "";
    @Input() name: string = ""; // inventoryDate (for IS Type)
    @Input() inventoryId?: number = 0;
    @Input() isNewArch: boolean = true;
    @Input() doExport: boolean = true;
    @Input() doExportVerbose: boolean = true;
    @Input() allSimulations: Inventory[] = [];
    @Input() inventories: Inventory[] = [];

    @Output() sidebarPurposeChange: EventEmitter<any> = new EventEmitter();
    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();
    @Output() reloadInventoriesAndLoop = new EventEmitter<number>();

    @ViewChild("firstInputElement", { static: false }) firstInputElement:
        | RadioButton
        | undefined;

    public fileTypes: FileType[] = [];
    invalidDates: Date[] = [];
    selectedType: string = Constants.INVENTORY_TYPE.INFORMATION_SYSTEM;
    inventoryDates: Date[] = [];
    simulationNames: string[] = [];
    inventoriesForm!: FormGroup;
    inventoryType = Constants.INVENTORY_TYPE;
    showBetaFeatures: string = environment.showBetaFeatures;
    isFileUploaded = signal(false);
    allowedFileExtensions = [".csv", ".xlsx", ".ods"];

    ngUnsubscribe = new Subject<void>();

    private readonly uploaderOutpoutHandlerReset$ = new Subject<void>();
    arrayComponents: Array<ComponentRef<SelectFileComponent>> = [];

    templateFiles: TemplateFileDescription[] = [];

    constructor(
        private inventoryService: InventoryDataService,
        private filesSystemService: FileSystemDataService,
        private loadingService: LoadingDataService,
        private messageService: MessageService,
        private translate: TranslateService,
        private readonly formBuilder: FormBuilder,
        private templateFileService: TemplateFileService,
    ) {}

    async ngOnInit() {
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

        this.getTemplateFiles();
    }

    ngAfterViewInit(): void {
        this.fileTypes.forEach((type) => this.addComponent(type));
    }

    ngOnChanges(changes: SimpleChanges) {
        this.invalidDates = [];
        this.inventories?.forEach((inventory) => {
            const month = inventory.date!.getMonth();
            let year = inventory.date!.getFullYear();
            for (let day = 1; day < 32; day++) {
                this.invalidDates.push(new Date(year, month, day));
            }
        });
    }

    getTemplateFiles() {
        this.templateFileService
            .getTemplateFiles()
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((templateFiles: FileDescription[]) => {
                if (templateFiles.length === 0) {
                    this.templateFiles = [];
                    return;
                }

                let zipFile: TemplateFileDescription = {} as TemplateFileDescription;
                let xlsxFile: TemplateFileDescription = {} as TemplateFileDescription;
                const csvFiles: TemplateFileDescription[] = [];

                templateFiles.forEach((res: FileDescription) => {
                    let templateFileDescription = { ...res } as TemplateFileDescription;
                    templateFileDescription.name = extractFileName(
                        templateFileDescription.name,
                    );

                    if (res.name.includes("zip")) {
                        templateFileDescription.type = "zip";
                        templateFileDescription.displayFileName = this.translate.instant(
                            "inventories.templates.all-template-files",
                            {
                                type: templateFileDescription.type,
                                size: this.toKB(res.metadata.size),
                            },
                        );
                        zipFile = templateFileDescription;
                    }
                    if (res.name.includes("xlsx")) {
                        templateFileDescription.type = "xlsx";
                        templateFileDescription.displayFileName = this.translate.instant(
                            "inventories.templates.data-model",
                            {
                                type: templateFileDescription.type,
                                size: this.toKB(res.metadata.size),
                            },
                        );
                        xlsxFile = templateFileDescription;
                    }
                    if (res.name.includes("csv")) {
                        templateFileDescription.type = "csv";
                        Constants.FILE_TYPES.forEach((csvFileType) => {
                            if (res.name.includes(csvFileType)) {
                                templateFileDescription.displayFileName =
                                    this.translate.instant(
                                        `inventories.templates.${csvFileType}-template-file`,
                                        {
                                            type: templateFileDescription.type,
                                            size: this.toKB(res.metadata.size),
                                        },
                                    );
                                templateFileDescription.csvFileType = csvFileType;
                            }
                        });

                        csvFiles.push(templateFileDescription);
                    }
                });

                csvFiles.sort(
                    (a, b) =>
                        Constants.FILE_TYPES.indexOf(a.csvFileType || "") -
                        Constants.FILE_TYPES.indexOf(b.csvFileType || ""),
                );
                this.templateFiles = [zipFile, ...csvFiles, xlsxFile];
            });
    }

    toKB(bytes: string | undefined) {
        if (bytes === undefined) return 0;
        return (parseInt(bytes) / 1024).toFixed(2);
    }

    checkForDuplicate() {
        return this.allSimulations.some(
            (inventory) => inventory.name == this.name?.trim(),
        );
    }

    get inventoriesFormControls() {
        return this.inventoriesForm.controls;
    }

    deleteComponent(index: number) {
        this.arrayComponents.at(index)?.destroy();
        this.arrayComponents.splice(index, 1);
        this.arrayComponents.forEach(({ instance }, index) => (instance.index = index));
    }

    addComponent(type = this.fileTypes[0]) {
        const componentRef = this.uploaderContainer.createComponent(SelectFileComponent);
        componentRef.setInput("fileTypes", this.fileTypes);
        componentRef.setInput("allowedFileExtensions", this.allowedFileExtensions);
        componentRef.instance.type = type;
        this.arrayComponents.push(componentRef);
        this.uploaderOutpoutHandlerReset$.next();
        this.arrayComponents.forEach(({ instance }, index) => {
            instance.index = index;
            instance.onDelete
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
        });
    }

    checkfileUploaded() {
        const isFileUploaded = this.arrayComponents.some(
            (compRef) => compRef?.instance?.file,
        );
        this.isFileUploaded.set(isFileUploaded);
    }

    submitFormData() {
        if (this.name === "") {
            this.className = "ng-invalid ng-dirty";
            return;
        }
        let formData = new FormData();
        let bodyLoading: FileDescription[] = [];

        this.arrayComponents.forEach(({ instance }) => {
            const { type, file } = instance;
            if (file) {
                formData.append(type.value, file, file.name);
                bodyLoading.push({
                    name: file.name,
                    type: type.value,
                    metadata: {
                        creationTime: new Date().toString(),
                    },
                });
            }
        });
        if (this.purpose === "new") {
            const creationObj: CreateInventory = {
                name: this.name,
                type: this.selectedType,
                isNewArch: this.isNewArch,
                doExport: this.doExport,
                doExportVerbose: this.doExportVerbose,
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
                    if (bodyLoading.length !== 0) {
                        this.uploadAndLaunchLoading(formData, bodyLoading, response.id);
                    } else {
                        this.reloadInventoriesAndLoop.emit(response.id);
                        this.close();
                    }
                },
                error: (error) => {},
            });
        } else {
            if (bodyLoading.length !== 0) {
                this.uploadAndLaunchLoading(formData, bodyLoading, this.inventoryId);
            }
        }
    }

    onSelectToDate(date: Date) {
        const monthNumber = `${date.getMonth() + 1}`;
        this.name = `${monthNumber.padStart(2, "0")}-${date.getFullYear()}`;
        this.className = "default-calendar";
    }

    onClearDate() {
        this.name = "";
        this.className = "default-calendar";
    }

    uploadAndLaunchLoading(
        formData: FormData,
        bodyLaunchLoading: FileDescription[],
        inventoryId: number = 0,
    ) {
        if (this.isNewArch) {
            this.loadingService.launchLoadInputFiles(inventoryId, formData).subscribe({
                next: async () => {
                    await delay(500);
                    this.sidebarVisibleChange.emit(false);
                    this.reloadInventoriesAndLoop.emit(inventoryId);
                    this.close();
                },
                error: () => {},
            });
        } else {
            this.filesSystemService
                .postFileSystemUploadFile(inventoryId, formData)
                .subscribe({
                    next: async () => {
                        this.loadingService
                            .launchLoading(bodyLaunchLoading, inventoryId)
                            .subscribe({
                                next: async () => {
                                    await delay(1000);
                                    this.sidebarVisibleChange.emit(false);
                                    this.reloadInventoriesAndLoop.emit(inventoryId);
                                    this.close();
                                },
                                error: () => {},
                            });
                    },
                    error: () => {
                        this.sidebarPurposeChange.emit("upload");
                    },
                });
        }
    }

    clearSidePanel() {
        this.arrayComponents.forEach((component) => {
            component.destroy();
        });
        this.arrayComponents = [];
        this.fileTypes.forEach((type) => this.addComponent(type));
    }

    close() {
        if (this.purpose === "new") {
            this.name = "";
        }
        this.sidebarVisibleChange.emit(false);
        this.clearSidePanel();
    }

    async downloadTemplateFile(selectedFileName: string) {
        try {
            const blob: Blob = await firstValueFrom(
                this.templateFileService.downloadTemplateFile(selectedFileName),
            );
            saveAs(blob, selectedFileName);
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
