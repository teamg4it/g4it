/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, fakeAsync, TestBed, tick } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of, throwError } from "rxjs";
import { TemplateFileDescription } from "src/app/core/interfaces/file-system.interfaces";
import {
    Inventory,
    InventoryUpdateRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { InvFilePanelComponent } from "./inv-file-panel.component";

describe("InvFilePanelComponent", () => {
    let component: InvFilePanelComponent;
    let fixture: ComponentFixture<InvFilePanelComponent>;
    let inventoryDataService: jasmine.SpyObj<InventoryDataService>;
    let loadingDataService: jasmine.SpyObj<LoadingDataService>;
    let messageService: jasmine.SpyObj<MessageService>;
    let translateService: jasmine.SpyObj<TranslateService>;
    let templateFileService: jasmine.SpyObj<TemplateFileService>;
    let globalStore: jasmine.SpyObj<GlobalStoreService>;

    const buildInventory = (overrides: Partial<Inventory> = {}): Inventory =>
        ({
            id: 1,
            type: Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            name: "06-2023",
            creationDate: new Date(),
            lastUpdateDate: new Date(),
            workspace: "Workspace",
            dataCenterCount: 0,
            physicalEquipmentCount: 0,
            virtualEquipmentCount: 0,
            applicationCount: 0,
            enableDataInconsistency: false,
            date: new Date(2023, 5, 1),
            criteria: ["Climate change"],
            tasks: [],
            ...overrides,
        }) as Inventory;

    const buildTemplateFile = (name: string): TemplateFileDescription => ({
        name,
        type: "csv",
        metadata: {},
    });

    beforeEach(async () => {
        inventoryDataService = jasmine.createSpyObj("InventoryDataService", [
            "createInventory",
            "updateInventory",
            "downloadWorkspaceSettingsZip",
        ]);
        loadingDataService = jasmine.createSpyObj("LoadingDataService", [
            "launchLoadInputFiles",
        ]);
        messageService = jasmine.createSpyObj("MessageService", ["add"]);
        translateService = jasmine.createSpyObj("TranslateService", ["instant"]);
        translateService.instant.and.callFake((key: string) => key);
        templateFileService = jasmine.createSpyObj("TemplateFileService", [
            "getTemplateFiles",
            "transformTemplateFiles",
            "getdownloadTemplateFile",
        ]);
        templateFileService.getTemplateFiles.and.returnValue(of([]));
        templateFileService.transformTemplateFiles.and.callFake(
            (files: any) => files as TemplateFileDescription[],
        );
        globalStore = jasmine.createSpyObj("GlobalStoreService", ["setLoading"]);

        await TestBed.configureTestingModule({
            imports: [InvFilePanelComponent],
            providers: [
                { provide: InventoryDataService, useValue: inventoryDataService },
                { provide: LoadingDataService, useValue: loadingDataService },
                { provide: MessageService, useValue: messageService },
                { provide: TranslateService, useValue: translateService },
                { provide: TemplateFileService, useValue: templateFileService },
                {
                    provide: UserService,
                    useValue: { currentWorkspace$: of({ id: 7, name: "Workspace" }) },
                },
                { provide: GlobalStoreService, useValue: globalStore },
            ],
        })
            .overrideComponent(InvFilePanelComponent, {
                set: {
                    template: `
                        @for (menu of importDetails.menu; track $index; let menuIndex = $index; let lastIndex = $last) {
                            <div class="space-form--input"
                                [class.active]="selectedMenuIndex === menuIndex"
                                [hidden]="selectedMenuIndex !== menuIndex">
                                @if (menuIndex > 0) {
                                    <span data-navigation="previous">
                                        <button (click)="previousTab(menuIndex)">Previous</button>
                                    </span>
                                }
                                @if (!lastIndex) {
                                    <span data-navigation="next">
                                        <button (click)="nextTab(menuIndex)">Next</button>
                                    </span>
                                }
                            </div>
                        }
                    `,
                },
            })
            .compileComponents();

        fixture = TestBed.createComponent(InvFilePanelComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("navigation focus", () => {
        for (const scenario of [
            { start: 2, direction: "previous", target: 1, focused: "previous" },
            { start: 1, direction: "previous", target: 0, focused: "next" },
            { start: 1, direction: "next", target: 2, focused: "next" },
            { start: 3, direction: "next", target: 4, focused: "previous" },
        ]) {
            it(`should focus ${scenario.focused} after ${scenario.direction} from tab ${scenario.start}`, fakeAsync(() => {
                component.selectTab(scenario.start);
                fixture.detectChanges();
                const source = fixture.nativeElement.querySelector(
                    `.space-form--input.active [data-navigation="${scenario.direction}"] button`,
                ) as HTMLButtonElement;
                source.focus();
                source.click();
                fixture.detectChanges();
                tick();

                const target = fixture.nativeElement.querySelector(
                    `.space-form--input.active [data-navigation="${scenario.focused}"] button`,
                ) as HTMLButtonElement;
                expect(component.selectedMenuIndex).toBe(scenario.target);
                expect(document.activeElement).toBe(target);
            }));
        }
    });

    describe("ngOnInit", () => {
        it("should build the form, snapshot the initial name and load template files", () => {
            expect(component.inventoriesForm).toBeTruthy();
            expect(component.initialName).toBe(component.name);
            expect(templateFileService.getTemplateFiles).toHaveBeenCalled();
            expect(component.templateFiles).toEqual([]);
        });
    });

    describe("getTemplateFiles", () => {
        it("should filter out ds_ and workspace files and select the first tab", () => {
            const files = [
                buildTemplateFile("ds_application.csv"),
                buildTemplateFile("workspace-settings.csv"),
                buildTemplateFile("DataModel.xlsx"),
            ];
            templateFileService.getTemplateFiles.and.returnValue(of(files));

            component.getTemplateFiles();

            const transformedArg =
                templateFileService.transformTemplateFiles.calls.mostRecent()
                    .args[0] as TemplateFileDescription[];
            expect(transformedArg.map((f) => f.name)).toEqual(["DataModel.xlsx"]);
            expect(component.templateFiles.map((f) => f.name)).toEqual([
                "DataModel.xlsx",
            ]);
            expect(component.selectedMenuIndex).toBe(0);
        });

        it("should clear templateFiles and not select a tab when nothing remains after filtering", () => {
            component.selectedMenuIndex = null;
            templateFileService.getTemplateFiles.and.returnValue(
                of([buildTemplateFile("ds_application.csv")]),
            );

            component.getTemplateFiles();

            expect(component.templateFiles).toEqual([]);
            expect(component.selectedMenuIndex).toBeNull();
        });
    });

    describe("checkForDuplicate", () => {
        beforeEach(() => {
            component.allSimulations = [
                buildInventory({ id: 2, name: "Sim A" }),
                buildInventory({ id: 3, name: "Sim B" }),
            ];
        });

        it("should return true when another simulation already has this name", () => {
            component.inventoryId = 5;
            component.name = "Sim A";
            expect(component.checkForDuplicate()).toBeTrue();
        });

        it("should return false when the match is the inventory being edited itself", () => {
            component.inventoryId = 2;
            component.name = "Sim A";
            expect(component.checkForDuplicate()).toBeFalse();
        });

        it("should return false when no simulation matches the name", () => {
            component.inventoryId = 5;
            component.name = "No Match";
            expect(component.checkForDuplicate()).toBeFalse();
        });
    });

    describe("ngOnChanges", () => {
        it("should compute invalid dates for all inventories when creating a new one", () => {
            component.purpose = "new";
            component.inventoryId = 5;
            component.inventories = [
                buildInventory({ id: 5, date: new Date(2023, 0, 1) }),
                buildInventory({ id: 6, date: new Date(2023, 1, 1) }),
            ];

            component.ngOnChanges({} as any);

            expect(component.invalidDates).toHaveSize(62);
            expect(component.selectedType).toBe(
                Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            );
            expect(component.originalInventory).toBeUndefined();
        });

        it("should exclude the edited inventory and prefill type/date when editing an information system", () => {
            component.purpose = "upload";
            component.inventoryId = 5;
            const edited = buildInventory({ id: 5, date: new Date(2023, 0, 1) });
            component.inventories = [
                edited,
                buildInventory({ id: 6, date: new Date(2023, 1, 1) }),
            ];
            component.allSimulations = [];

            component.ngOnChanges({} as any);

            expect(component.invalidDates).toHaveSize(31);
            expect(component.selectedType).toBe(
                Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            );
            expect(component.selectedDate).toBe(edited.date as Date);
            expect(component.originalInventory).toBe(edited);
        });

        it("should prefill type when editing a simulation", () => {
            component.purpose = "upload";
            component.inventoryId = 9;
            component.inventories = [buildInventory({ id: 5 })];
            const simulation = buildInventory({
                id: 9,
                type: Constants.INVENTORY_TYPE.SIMULATION,
                name: "SimX",
            });
            component.allSimulations = [simulation];

            component.ngOnChanges({} as any);

            expect(component.selectedType).toBe(Constants.INVENTORY_TYPE.SIMULATION);
            expect(component.originalInventory).toBe(simulation);
        });

        it("should leave type/originalInventory untouched when the edited id matches nothing", () => {
            component.purpose = "upload";
            component.inventoryId = 999;
            component.inventories = [buildInventory({ id: 5 })];
            component.allSimulations = [buildInventory({ id: 9 })];

            component.ngOnChanges({} as any);

            expect(component.selectedType).toBe(
                Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            );
            expect(component.originalInventory).toBeUndefined();
        });

        it("should skip the edited-inventory lookup entirely when there is no inventoryId", () => {
            component.purpose = "upload";
            component.inventoryId = 0;
            component.inventories = [buildInventory({ id: 5 })];

            component.ngOnChanges({} as any);

            expect(component.originalInventory).toBeUndefined();
        });
    });

    describe("submitFormData", () => {
        it("should mark the form invalid and stop when name is empty", () => {
            component.name = "";

            component.submitFormData();

            expect(component.className).toBe("ng-invalid ng-dirty");
            expect(globalStore.setLoading).toHaveBeenCalledWith(true);
            expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            expect(inventoryDataService.createInventory).not.toHaveBeenCalled();
        });

        describe("purpose new", () => {
            beforeEach(() => {
                component.purpose = "new";
                component.name = "10-2023";
                component.selectedType = Constants.INVENTORY_TYPE.INFORMATION_SYSTEM;
            });

            it("should create the inventory and close when no files were selected", () => {
                inventoryDataService.createInventory.and.returnValue(
                    of(buildInventory({ id: 42 })),
                );
                spyOn(component, "close");
                spyOn(component.reloadInventoriesAndLoop, "emit");

                component.submitFormData();

                expect(messageService.add).toHaveBeenCalled();
                expect(component.reloadInventoriesAndLoop.emit).toHaveBeenCalledWith(42);
                expect(component.close).toHaveBeenCalled();
                expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            });

            it("should create the inventory and upload the files when files were selected", () => {
                inventoryDataService.createInventory.and.returnValue(
                    of(buildInventory({ id: 42 })),
                );
                const formData = new FormData();
                formData.append("file", new Blob(["x"]));
                component.invMultiFileImport = { getFormData: () => formData } as any;
                spyOn(component, "uploadAndLaunchLoading");

                component.submitFormData();

                expect(component.uploadAndLaunchLoading).toHaveBeenCalledWith(
                    formData,
                    42,
                );
            });

            it("should stop loading when creation fails", () => {
                inventoryDataService.createInventory.and.returnValue(
                    throwError(() => new Error("fail")),
                );

                component.submitFormData();

                expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            });
        });

        describe("purpose upload", () => {
            beforeEach(() => {
                component.purpose = "upload";
                component.inventoryId = 5;
                component.name = "New Name";
            });

            it("should update the inventory then upload files when the name changed", () => {
                component.initialName = "Old Name";
                component.originalInventory = buildInventory({
                    id: 5,
                    criteria: ["Climate change"],
                    enableDataInconsistency: true,
                });
                inventoryDataService.updateInventory.and.returnValue(
                    of({} as InventoryUpdateRest),
                );
                const formData = new FormData();
                formData.append("file", new Blob(["x"]));
                component.invMultiFileImport = { getFormData: () => formData } as any;
                spyOn(component, "uploadAndLaunchLoading");

                component.submitFormData();

                expect(inventoryDataService.updateInventory).toHaveBeenCalledWith({
                    id: 5,
                    name: "New Name",
                    note: undefined,
                    criteria: ["Climate change"],
                    enableDataInconsistency: true,
                });
                expect(component.uploadAndLaunchLoading).toHaveBeenCalledWith(
                    formData,
                    5,
                );
            });

            it("should update the inventory then close when the name changed and no files were selected", () => {
                component.initialName = "Old Name";
                component.originalInventory = buildInventory({ id: 5 });
                inventoryDataService.updateInventory.and.returnValue(
                    of({} as InventoryUpdateRest),
                );
                spyOn(component, "close");

                component.submitFormData();

                expect(component.close).toHaveBeenCalled();
                expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            });

            it("should stop loading when the update fails", () => {
                component.initialName = "Old Name";
                component.originalInventory = buildInventory({ id: 5 });
                inventoryDataService.updateInventory.and.returnValue(
                    throwError(() => new Error("fail")),
                );

                component.submitFormData();

                expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            });

            it("should upload files directly when the name did not change", () => {
                component.initialName = "New Name";
                component.originalInventory = buildInventory({ id: 5 });
                const formData = new FormData();
                formData.append("file", new Blob(["x"]));
                component.invMultiFileImport = { getFormData: () => formData } as any;
                spyOn(component, "uploadAndLaunchLoading");

                component.submitFormData();

                expect(inventoryDataService.updateInventory).not.toHaveBeenCalled();
                expect(component.uploadAndLaunchLoading).toHaveBeenCalledWith(
                    formData,
                    5,
                );
            });

            it("should close directly when there is no original inventory to update", () => {
                component.initialName = "Old Name";
                component.originalInventory = undefined;
                spyOn(component, "close");

                component.submitFormData();

                expect(inventoryDataService.updateInventory).not.toHaveBeenCalled();
                expect(component.close).toHaveBeenCalled();
                expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            });
        });
    });

    describe("date and name handlers", () => {
        it("should set the name from the selected month on onSelectToDate", () => {
            const date = new Date(2023, 9, 15);

            component.onSelectToDate(date);

            expect(component.selectedDate).toBe(date);
            expect(component.name).toBe("10-2023");
            expect(component.className).toBe("default-calendar");
            expect(component.importForm.controls.info.value).toBe("10-2023");
        });

        it("should clear the date and name on onClearDate", () => {
            component.onSelectToDate(new Date(2023, 9, 15));

            component.onClearDate();

            expect(component.selectedDate).toBeNull();
            expect(component.name).toBe("");
            expect(component.className).toBe("default-calendar");
            expect(component.importForm.controls.info.value).toBeUndefined();
        });

        it("should update the name and form control on onSimulationNameChange", () => {
            component.onSimulationNameChange("My Simulation");

            expect(component.name).toBe("My Simulation");
            expect(component.inventoriesForm.controls["name"].value).toBe(
                "My Simulation",
            );
            expect(component.importForm.controls.info.value).toBe("My Simulation");
        });

        it("should update the type when creating a new inventory", () => {
            component.purpose = "new";
            component.name = "something";
            component.selectedDate = new Date();

            component.onInventoryTypeChanged(Constants.INVENTORY_TYPE.SIMULATION);

            expect(component.selectedType).toBe(Constants.INVENTORY_TYPE.SIMULATION);
            expect(component.name).toBe("");
            expect(component.selectedDate).toBeNull();
        });

        it("should ignore type changes when not creating a new inventory", () => {
            component.purpose = "upload";
            component.selectedType = Constants.INVENTORY_TYPE.INFORMATION_SYSTEM;

            component.onInventoryTypeChanged(Constants.INVENTORY_TYPE.SIMULATION);

            expect(component.selectedType).toBe(
                Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            );
        });
    });

    describe("uploadAndLaunchLoading", () => {
        it("should close the panel and reload on success", fakeAsync(() => {
            loadingDataService.launchLoadInputFiles.and.returnValue(of({} as any));
            spyOn(component, "close");
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component.reloadInventoriesAndLoop, "emit");

            component.uploadAndLaunchLoading(new FormData(), 7);
            tick(500);

            expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
            expect(component.reloadInventoriesAndLoop.emit).toHaveBeenCalledWith(7);
            expect(component.close).toHaveBeenCalled();
        }));

        it("should switch back to upload purpose on error", fakeAsync(() => {
            loadingDataService.launchLoadInputFiles.and.returnValue(
                throwError(() => new Error("fail")),
            );
            spyOn(component.sidebarPurposeChange, "emit");

            component.uploadAndLaunchLoading(new FormData(), 7);
            tick(500);

            expect(globalStore.setLoading).toHaveBeenCalledWith(false);
            expect(component.sidebarPurposeChange.emit).toHaveBeenCalledWith("upload");
        }));
    });

    describe("close", () => {
        it("should reset name and date when creating a new inventory", () => {
            component.purpose = "new";
            component.name = "10-2023";
            component.selectedDate = new Date();
            spyOn(component.sidebarVisibleChange, "emit");

            component.close();

            expect(component.name).toBe("");
            expect(component.selectedDate).toBeNull();
            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should keep name and date when not creating a new inventory", () => {
            component.purpose = "upload";
            component.name = "Kept Name";
            const date = new Date();
            component.selectedDate = date;
            spyOn(component.sidebarVisibleChange, "emit");

            component.close();

            expect(component.name).toBe("Kept Name");
            expect(component.selectedDate).toBe(date);
            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });
    });

    it("should download the template file", () => {
        component.downloadTemplateFile("datacenter.csv");

        expect(templateFileService.getdownloadTemplateFile).toHaveBeenCalledWith(
            "datacenter.csv",
        );
    });

    describe("selectTab and getSelectedTemplates", () => {
        beforeEach(() => {
            component.templateFiles = [
                buildTemplateFile("DataModel.xlsx"),
                buildTemplateFile("Datacenter.csv"),
                buildTemplateFile("PhysicalEquipment.csv"),
                buildTemplateFile("VirtualEquipment.csv"),
                buildTemplateFile("Application.csv"),
                buildTemplateFile("AIservices.csv"),
                buildTemplateFile("SomethingElse.csv"),
            ];
        });

        it("should select the data model file for tab 0", () => {
            component.selectTab(0);

            expect(component.selectedMenuIndex).toBe(0);
            expect(component.templateFileVisible().map((f) => f.name)).toEqual([
                "DataModel.xlsx",
            ]);
            expect(component.importDetails.menu[0].active).toBeTrue();
            expect(component.importDetails.menu[1].active).toBeFalse();
        });

        it("should select data center related files for tab 1", () => {
            component.selectTab(1);

            expect(component.templateFileVisible().map((f) => f.name)).toEqual([
                "DataModel.xlsx",
                "Datacenter.csv",
                "PhysicalEquipment.csv",
            ]);
        });

        it("should select physical equipment related files for tab 2", () => {
            component.selectTab(2);

            expect(component.templateFileVisible().map((f) => f.name)).toEqual([
                "DataModel.xlsx",
                "PhysicalEquipment.csv",
            ]);
        });

        it("should select application related files for tab 3", () => {
            component.selectTab(3);

            expect(component.templateFileVisible().map((f) => f.name)).toEqual([
                "DataModel.xlsx",
                "VirtualEquipment.csv",
                "Application.csv",
            ]);
        });

        it("should select AI services related files for any other tab", () => {
            component.selectTab(4);

            expect(component.templateFileVisible().map((f) => f.name)).toEqual([
                "DataModel.xlsx",
                "VirtualEquipment.csv",
                "AIservices.csv",
            ]);
        });
    });

    describe("tab navigation", () => {
        it("should not go before the first tab", () => {
            spyOn(component, "selectTab");

            component.previousTab(0);

            expect(component.selectTab).not.toHaveBeenCalled();
        });

        it("should go to the previous tab", () => {
            spyOn(component, "selectTab");

            component.previousTab(2);

            expect(component.selectTab).toHaveBeenCalledWith(1);
        });

        it("should not go past the last tab", () => {
            spyOn(component, "selectTab");

            component.nextTab(component.importDetails["menu"].length - 1);

            expect(component.selectTab).not.toHaveBeenCalled();
        });

        it("should go to the next tab", () => {
            spyOn(component, "selectTab");

            component.nextTab(2);

            expect(component.selectTab).toHaveBeenCalledWith(3);
        });
    });

    it("should emit sidebarVisibleChange on closeSidebar", () => {
        spyOn(component.sidebarVisibleChange, "emit");

        component.closeSidebar();

        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should download the workspace reference data", () => {
        inventoryDataService.downloadWorkspaceSettingsZip.and.returnValue(
            of(new Blob(["data"])),
        );

        expect(() => component.downloadWorkspaceReferenceData()).not.toThrow();

        expect(inventoryDataService.downloadWorkspaceSettingsZip).toHaveBeenCalled();
    });

    it("should complete the unsubscribe subject on destroy", () => {
        component.ngOnDestroy();

        expect(component.ngUnsubscribe.isStopped).toBeTrue();
    });
});
