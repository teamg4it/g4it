/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    AfterViewInit,
    Component,
    ComponentRef,
    EventEmitter,
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
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { RadioButton } from "primeng/radiobutton";
import { delay, Subject, takeUntil } from "rxjs";
import {
    FileDescription,
    FileType,
    TemplateFileDescription,
} from "src/app/core/interfaces/file-system.interfaces";
import { CreateInventory, Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { Constants } from "src/constants";
import { SelectFileComponent } from "./select-file/select-file.component";

@Component({
    selector: "app-file-panel",
    templateUrl: "./file-panel.component.html",
})
export class FilePanelComponent implements OnInit, OnDestroy, AfterViewInit, OnChanges {
    className: string = "default-calendar max-w-full";

    @ViewChild("uploaderContainer", { read: ViewContainerRef })
    uploaderContainer!: ViewContainerRef;
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

    public fileTypes: FileType[] = [];
    invalidDates: Date[] = [];
    selectedType: string = Constants.INVENTORY_TYPE.INFORMATION_SYSTEM;
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
    isTemplateParam = Constants.TEMPLATE_PARAMS.IS_MODULE;
    constructor(
        private readonly inventoryService: InventoryDataService,
        private readonly loadingService: LoadingDataService,
        private readonly messageService: MessageService,
        private readonly translate: TranslateService,
        private readonly formBuilder: FormBuilder,
        private readonly templateFileService: TemplateFileService,
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

        this.getTemplateFiles();
    }

    ngAfterViewInit(): void {
        for (const type of this.fileTypes) {
            this.addComponent(type);
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        this.invalidDates = [];
        for (const inventory of this.inventories) {
            const month = inventory.date!.getMonth();
            let year = inventory.date!.getFullYear();
            for (let day = 1; day < 32; day++) {
                this.invalidDates.push(new Date(year, month, day));
            }
        }
    }

    getTemplateFiles() {
        this.templateFileService
            .getTemplateFiles(this.isTemplateParam)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((templateFiles: FileDescription[]) => {
                if (templateFiles.length === 0) {
                    this.templateFiles = [];
                    return;
                }
                this.templateFiles = this.templateFileService.transformTemplateFiles(
                    templateFiles,
                    false,
                );
            });
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
        if (this.name === "") {
            this.className = "ng-invalid ng-dirty";
            return;
        }
        let formData = new FormData();
        let bodyLoading: FileDescription[] = [];

        for (const { instance } of this.arrayComponents) {
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
        }
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
                    if (bodyLoading.length !== 0) {
                        this.uploadAndLaunchLoading(formData, response.id);
                    } else {
                        this.reloadInventoriesAndLoop.emit(response.id);
                        this.close();
                    }
                },
                error: (error) => {},
            });
            return;
        }
        if (bodyLoading.length !== 0) {
            this.uploadAndLaunchLoading(formData, this.inventoryId);
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

    uploadAndLaunchLoading(formData: FormData, inventoryId: number = 0) {
        this.loadingService
            .launchLoadInputFiles(inventoryId, formData)
            .pipe(delay(500))
            .subscribe({
                next: () => {
                    this.sidebarVisibleChange.emit(false);
                    this.reloadInventoriesAndLoop.emit(inventoryId);
                    this.close();
                },
                error: () => {
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
        }
        this.sidebarVisibleChange.emit(false);
        this.clearSidePanel();
    }

    downloadTemplateFile(selectedFileName: string) {
        this.templateFileService.getdownloadTemplateFile(
            selectedFileName,
            this.isTemplateParam,
        );
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
