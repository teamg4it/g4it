import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { BehaviorSubject, of, throwError } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { WorkspaceReferenceDataService } from "src/app/core/service/data/workspace-reference-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FilePanelComponent } from "./file-panel.component";
import { SelectFileComponent } from "./select-file/select-file.component";

describe("FilePanelComponent", () => {
    let component: FilePanelComponent;
    let fixture: ComponentFixture<FilePanelComponent>;
    let mockTemplateFileService: jasmine.SpyObj<TemplateFileService>;
    let mockLoadingService: jasmine.SpyObj<LoadingDataService>;
    let mockMessageService: jasmine.SpyObj<MessageService>;
    let mockInventoryService: jasmine.SpyObj<InventoryDataService>;
    let mockUserService: jasmine.SpyObj<UserService>;
    let mockWorkspaceReferenceDataService: jasmine.SpyObj<WorkspaceReferenceDataService>;

    const createMockInventory = (id: number): Inventory => ({
        id,
        name: `Test Inventory ${id}`,
        creationDate: new Date(),
        lastUpdateDate: new Date(),
        workspace: "test-workspace",
        dataCenterCount: 0,
        physicalEquipmentCount: 0,
        virtualEquipmentCount: 0,
        applicationCount: 0,
        enableDataInconsistency: false,
        tasks: [],
        type: "INFORMATION_SYSTEM",
    });

    beforeEach(async () => {
        mockTemplateFileService = jasmine.createSpyObj("TemplateFileService", [
            "getTemplateFiles",
            "transformTemplateFiles",
            "getdownloadTemplateFile",
        ]);
        mockLoadingService = jasmine.createSpyObj("LoadingDataService", [
            "launchLoadInputFiles",
        ]);
        mockMessageService = jasmine.createSpyObj("MessageService", ["add"]);
        mockInventoryService = jasmine.createSpyObj("InventoryDataService", [
            "createInventory",
        ]);
        mockUserService = jasmine.createSpyObj("UserService", [], {
            currentWorkspace$: new BehaviorSubject({ id: 1, name: "Test Workspace" }),
            currentOrganization$: new BehaviorSubject({ id: 1, name: "Test Org" }),
        });
        mockWorkspaceReferenceDataService = jasmine.createSpyObj(
            "WorkspaceReferenceDataService",
            ["workspaceDownloadZipFile", "getZipFileName"],
        );

        await TestBed.configureTestingModule({
            declarations: [FilePanelComponent, SelectFileComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                ReactiveFormsModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],

            providers: [
                FormBuilder,
                TranslateService,
                { provide: TemplateFileService, useValue: mockTemplateFileService },
                { provide: LoadingDataService, useValue: mockLoadingService },
                { provide: MessageService, useValue: mockMessageService },
                { provide: InventoryDataService, useValue: mockInventoryService },
                { provide: UserService, useValue: mockUserService },
                {
                    provide: WorkspaceReferenceDataService,
                    useValue: mockWorkspaceReferenceDataService,
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(FilePanelComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize fileTypes and inventoriesForm on ngOnInit", () => {
        mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
        component.ngOnInit();
        expect(component.fileTypes.length).toBeGreaterThan(0);
        expect(component.inventoriesForm).toBeDefined();
    });

    it("should call getdownloadTemplateFile on downloadTemplateFile", () => {
        component.downloadTemplateFile("template.csv");
        expect(mockTemplateFileService.getdownloadTemplateFile).toHaveBeenCalledWith(
            "template.csv",
        );
    });

    describe("clearSidePanel", () => {
        it("should destroy all components in arrayComponents", () => {
            // Setup: Create mock component refs
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();

            const mockComponentRef1 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            const mockComponentRef2 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            const mockComponentRef3 = jasmine.createSpyObj("ComponentRef", ["destroy"]);

            component.arrayComponents = [
                mockComponentRef1,
                mockComponentRef2,
                mockComponentRef3,
            ];

            // Mock addComponent to prevent actual component creation
            spyOn(component, "addComponent");

            // Execute
            component.clearSidePanel();

            // Verify all components were destroyed
            expect(mockComponentRef1.destroy).toHaveBeenCalled();
            expect(mockComponentRef2.destroy).toHaveBeenCalled();
            expect(mockComponentRef3.destroy).toHaveBeenCalled();
        });

        it("should clear the arrayComponents array", () => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();

            const mockComponentRef = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            component.arrayComponents = [mockComponentRef];

            // Mock addComponent to prevent actual component creation
            const addComponentSpy = spyOn(component, "addComponent");

            component.clearSidePanel();

            // Verify array was cleared and addComponent called for each file type
            expect(addComponentSpy).toHaveBeenCalledTimes(component.fileTypes.length);
        });

        it("should recreate components for each fileType", () => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();

            const initialFileTypesCount = component.fileTypes.length;
            spyOn(component, "addComponent");

            component.clearSidePanel();

            // Verify addComponent was called for each file type
            expect(component.addComponent).toHaveBeenCalledTimes(initialFileTypesCount);
        });

        it("should call addComponent with each fileType in order", () => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();

            spyOn(component, "addComponent");

            component.clearSidePanel();

            // Verify addComponent was called with each file type
            for (const type of component.fileTypes) {
                expect(component.addComponent).toHaveBeenCalledWith(type);
            }
        });
    });

    describe("close", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
            // Mock addComponent to prevent ViewContainerRef issues in tests
            spyOn(component, "addComponent");
            fixture.detectChanges();
        });

        it("should clear the name when purpose is 'new'", () => {
            component.purpose = "new";
            component.name = "Test Inventory";
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component, "clearSidePanel");

            component.close();

            expect(component.name).toBe("");
        });

        it("should not clear the name when purpose is 'upload'", () => {
            component.purpose = "upload";
            component.name = "Test Inventory";
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component, "clearSidePanel");

            component.close();

            expect(component.name).toBe("Test Inventory");
        });

        it("should not clear the name when purpose is not 'new'", () => {
            component.purpose = "edit";
            component.name = "Existing Inventory";
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component, "clearSidePanel");

            component.close();

            expect(component.name).toBe("Existing Inventory");
        });

        it("should emit false to sidebarVisibleChange", () => {
            component.purpose = "new";
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component, "clearSidePanel");

            component.close();

            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should call clearSidePanel", () => {
            component.purpose = "new";
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component, "clearSidePanel");

            component.close();

            expect(component.clearSidePanel).toHaveBeenCalled();
        });

        it("should execute methods in correct order: clear name, emit, then clear panel", () => {
            component.purpose = "new";
            component.name = "Test Name";
            const callOrder: string[] = [];

            spyOn(component.sidebarVisibleChange, "emit").and.callFake(() => {
                callOrder.push("emit");
                // At this point, name should already be cleared
                expect(component.name).toBe("");
            });

            spyOn(component, "clearSidePanel").and.callFake(() => {
                callOrder.push("clearSidePanel");
                // At this point, emit should have been called
                expect(callOrder).toContain("emit");
            });

            component.close();

            expect(callOrder).toEqual(["emit", "clearSidePanel"]);
        });

        it("should handle close when purpose is empty string", () => {
            component.purpose = "";
            component.name = "Some Inventory";
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component, "clearSidePanel");

            component.close();

            // Name should not be cleared when purpose is not "new"
            expect(component.name).toBe("Some Inventory");
            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
            expect(component.clearSidePanel).toHaveBeenCalled();
        });
    });

    describe("onSelectToDate", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
        });

        it("should format and set name with month and year in MM-YYYY format", () => {
            const testDate = new Date(2024, 2, 15); // March 15, 2024
            component.onSelectToDate(testDate);

            expect(component.name).toBe("03-2024");
        });

        it("should pad single-digit months with leading zero", () => {
            const testDate = new Date(2024, 0, 10); // January 10, 2024
            component.onSelectToDate(testDate);

            expect(component.name).toBe("01-2024");
        });

        it("should handle double-digit months correctly", () => {
            const testDate = new Date(2024, 11, 25); // December 25, 2024
            component.onSelectToDate(testDate);

            expect(component.name).toBe("12-2024");
        });

        it("should set className to 'default-calendar'", () => {
            const testDate = new Date(2024, 5, 20); // June 20, 2024
            component.className = "some-other-class";

            component.onSelectToDate(testDate);

            expect(component.className).toBe("default-calendar");
        });

        it("should handle different years correctly", () => {
            const testDate = new Date(2026, 3, 9); // April 9, 2026
            component.onSelectToDate(testDate);

            expect(component.name).toBe("04-2026");
        });

        it("should override existing name value", () => {
            component.name = "Old Name";
            const testDate = new Date(2024, 6, 1); // July 1, 2024

            component.onSelectToDate(testDate);

            expect(component.name).toBe("07-2024");
        });
    });

    describe("onClearDate", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
        });

        it("should clear the name to empty string", () => {
            component.name = "03-2024";

            component.onClearDate();

            expect(component.name).toBe("");
        });

        it("should set className to 'default-calendar'", () => {
            component.className = "ng-invalid ng-dirty";

            component.onClearDate();

            expect(component.className).toBe("default-calendar");
        });

        it("should clear name even when it's already empty", () => {
            component.name = "";

            component.onClearDate();

            expect(component.name).toBe("");
        });

        it("should reset both name and className", () => {
            component.name = "12-2025";
            component.className = "some-custom-class";

            component.onClearDate();

            expect(component.name).toBe("");
            expect(component.className).toBe("default-calendar");
        });
    });

    describe("checkfileUploaded", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
        });

        it("should set isFileUploaded to true when at least one component has a file", () => {
            const mockInstance1 = { file: new File(["content"], "test1.csv") };
            const mockInstance2 = { file: undefined };
            const mockComponentRef1 = { instance: mockInstance1 } as any;
            const mockComponentRef2 = { instance: mockInstance2 } as any;

            component.arrayComponents = [mockComponentRef1, mockComponentRef2];

            component.checkfileUploaded();

            expect(component.isFileUploaded()).toBe(true);
        });

        it("should set isFileUploaded to false when no components have files", () => {
            const mockInstance1 = { file: undefined };
            const mockInstance2 = { file: null };
            const mockComponentRef1 = { instance: mockInstance1 } as any;
            const mockComponentRef2 = { instance: mockInstance2 } as any;

            component.arrayComponents = [mockComponentRef1, mockComponentRef2];

            component.checkfileUploaded();

            expect(component.isFileUploaded()).toBe(false);
        });

        it("should set isFileUploaded to false when arrayComponents is empty", () => {
            component.arrayComponents = [];

            component.checkfileUploaded();

            expect(component.isFileUploaded()).toBe(false);
        });

        it("should handle null instance gracefully", () => {
            const mockComponentRef = { instance: null } as any;
            component.arrayComponents = [mockComponentRef];

            component.checkfileUploaded();

            expect(component.isFileUploaded()).toBe(false);
        });

        it("should set isFileUploaded to true when all components have files", () => {
            const mockInstance1 = { file: new File(["content"], "test1.csv") };
            const mockInstance2 = { file: new File(["content"], "test2.csv") };
            const mockComponentRef1 = { instance: mockInstance1 } as any;
            const mockComponentRef2 = { instance: mockInstance2 } as any;

            component.arrayComponents = [mockComponentRef1, mockComponentRef2];

            component.checkfileUploaded();

            expect(component.isFileUploaded()).toBe(true);
        });
    });

    describe("submitFormData", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
            spyOn(component, "addComponent");
        });

        it("should set className and return early when name is empty", () => {
            component.name = "";
            component.className = "default-calendar";

            component.submitFormData();

            expect(component.className).toBe("ng-invalid ng-dirty");
        });

        it("should not proceed with form submission when name is empty", () => {
            component.name = "";
            spyOn(component.reloadInventoriesAndLoop, "emit");

            component.submitFormData();

            expect(mockInventoryService.createInventory).not.toHaveBeenCalled();
            expect(component.reloadInventoriesAndLoop.emit).not.toHaveBeenCalled();
        });

        it("should create inventory when purpose is 'new' and no files uploaded", () => {
            component.purpose = "new";
            component.name = "Test Inventory";
            component.selectedType = "INFORMATION_SYSTEM";
            component.arrayComponents = [];

            const mockResponse = createMockInventory(123);
            mockInventoryService.createInventory.and.returnValue(of(mockResponse));
            spyOn(component.reloadInventoriesAndLoop, "emit");
            spyOn(component, "close");

            component.submitFormData();

            expect(mockInventoryService.createInventory).toHaveBeenCalledWith({
                name: "Test Inventory",
                type: "INFORMATION_SYSTEM",
            });
            expect(mockMessageService.add).toHaveBeenCalled();
            expect(component.reloadInventoriesAndLoop.emit).toHaveBeenCalledWith(123);
            expect(component.close).toHaveBeenCalled();
        });

        it("should create inventory and upload files when purpose is 'new' with files", () => {
            component.purpose = "new";
            component.name = "Test Inventory";
            component.selectedType = "INFORMATION_SYSTEM";

            const mockFile = new File(["content"], "test.csv");
            const mockType = { value: "DATACENTER", text: "Data Center" };
            const mockInstance = { file: mockFile, type: mockType };
            const mockComponentRef = { instance: mockInstance } as any;
            component.arrayComponents = [mockComponentRef];

            const mockResponse = createMockInventory(123);
            mockInventoryService.createInventory.and.returnValue(of(mockResponse));
            spyOn(component, "uploadAndLaunchLoading");

            component.submitFormData();

            expect(mockInventoryService.createInventory).toHaveBeenCalled();
            expect(component.uploadAndLaunchLoading).toHaveBeenCalledWith(
                jasmine.any(FormData),
                123,
            );
        });

        it("should add success message when inventory is created", () => {
            component.purpose = "new";
            component.name = "New Inventory";
            component.arrayComponents = [];

            const mockResponse = createMockInventory(456);
            mockInventoryService.createInventory.and.returnValue(of(mockResponse));
            spyOn(component, "close");

            component.submitFormData();

            expect(mockMessageService.add).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    severity: "success",
                }),
            );
        });

        it("should upload files when purpose is not 'new' and files exist", () => {
            component.purpose = "upload";
            component.name = "Existing Inventory";
            component.inventoryId = 789;

            const mockFile = new File(["content"], "test.csv");
            const mockType = { value: "APPLICATION", text: "Application" };
            const mockInstance = { file: mockFile, type: mockType };
            const mockComponentRef = { instance: mockInstance } as any;
            component.arrayComponents = [mockComponentRef];

            spyOn(component, "uploadAndLaunchLoading");

            component.submitFormData();

            expect(component.uploadAndLaunchLoading).toHaveBeenCalledWith(
                jasmine.any(FormData),
                789,
            );
        });

        it("should not upload when purpose is not 'new' and no files exist", () => {
            component.purpose = "upload";
            component.name = "Existing Inventory";
            component.arrayComponents = [];

            spyOn(component, "uploadAndLaunchLoading");

            component.submitFormData();

            expect(component.uploadAndLaunchLoading).not.toHaveBeenCalled();
        });

        it("should handle creation error gracefully", () => {
            component.purpose = "new";
            component.name = "Test Inventory";
            component.arrayComponents = [];

            mockInventoryService.createInventory.and.returnValue(
                throwError(() => new Error("Creation failed")),
            );

            // Should not throw - error is handled in subscribe error callback
            expect(() => component.submitFormData()).not.toThrow();
        });
    });

    describe("checkForDuplicate", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
        });

        it("should return true when name matches an existing inventory", () => {
            const existingInventory = createMockInventory(1);
            existingInventory.name = "Existing Inventory";
            component.allSimulations = [existingInventory];
            component.name = "Existing Inventory";

            const result = component.checkForDuplicate();

            expect(result).toBe(true);
        });

        it("should return true when name matches after trimming whitespace", () => {
            const existingInventory = createMockInventory(1);
            existingInventory.name = "Existing Inventory";
            component.allSimulations = [existingInventory];
            component.name = "  Existing Inventory  ";

            const result = component.checkForDuplicate();

            expect(result).toBe(true);
        });

        it("should return false when name does not match any inventory", () => {
            const existingInventory = createMockInventory(1);
            existingInventory.name = "Existing Inventory";
            component.allSimulations = [existingInventory];
            component.name = "New Inventory";

            const result = component.checkForDuplicate();

            expect(result).toBe(false);
        });

        it("should return false when allSimulations is empty", () => {
            component.allSimulations = [];
            component.name = "Any Name";

            const result = component.checkForDuplicate();

            expect(result).toBe(false);
        });

        it("should return false when name is empty", () => {
            const existingInventory = createMockInventory(1);
            existingInventory.name = "Existing Inventory";
            component.allSimulations = [existingInventory];
            component.name = "";

            const result = component.checkForDuplicate();

            expect(result).toBe(false);
        });

        it("should check against multiple inventories", () => {
            const inventory1 = createMockInventory(1);
            inventory1.name = "Inventory 1";
            const inventory2 = createMockInventory(2);
            inventory2.name = "Inventory 2";
            const inventory3 = createMockInventory(3);
            inventory3.name = "Inventory 3";

            component.allSimulations = [inventory1, inventory2, inventory3];
            component.name = "Inventory 2";

            const result = component.checkForDuplicate();

            expect(result).toBe(true);
        });

        it("should handle null or undefined name gracefully", () => {
            const existingInventory = createMockInventory(1);
            existingInventory.name = "Existing Inventory";
            component.allSimulations = [existingInventory];
            component.name = null as any;

            const result = component.checkForDuplicate();

            expect(result).toBe(false);
        });
    });

    describe("inventoriesFormControls", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
        });

        it("should return form controls", () => {
            const controls = component.inventoriesFormControls;

            expect(controls).toBeDefined();
            expect(controls).toBe(component.inventoriesForm.controls);
        });

        it("should have name control", () => {
            const controls = component.inventoriesFormControls;

            expect(controls["name"]).toBeDefined();
        });
    });

    describe("deleteComponent", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
        });

        it("should destroy component at specified index", () => {
            const mockComponentRef = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef.instance = { index: 0 };
            component.arrayComponents = [mockComponentRef];

            component.deleteComponent(0);

            expect(mockComponentRef.destroy).toHaveBeenCalled();
        });

        it("should remove component from arrayComponents", () => {
            const mockComponentRef = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef.instance = { index: 0 };
            component.arrayComponents = [mockComponentRef];

            component.deleteComponent(0);

            expect(component.arrayComponents.length).toBe(0);
        });

        it("should re-index remaining components", () => {
            const mockComponentRef1 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef1.instance = { index: 0 };
            const mockComponentRef2 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef2.instance = { index: 1 };
            const mockComponentRef3 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef3.instance = { index: 2 };

            component.arrayComponents = [
                mockComponentRef1,
                mockComponentRef2,
                mockComponentRef3,
            ];

            component.deleteComponent(0);

            expect(component.arrayComponents.length).toBe(2);
            expect(component.arrayComponents[0].instance.index).toBe(0);
            expect(component.arrayComponents[1].instance.index).toBe(1);
        });

        it("should delete middle component and re-index correctly", () => {
            const mockComponentRef1 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef1.instance = { index: 0 };
            const mockComponentRef2 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef2.instance = { index: 1 };
            const mockComponentRef3 = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef3.instance = { index: 2 };

            component.arrayComponents = [
                mockComponentRef1,
                mockComponentRef2,
                mockComponentRef3,
            ];

            component.deleteComponent(1);

            expect(mockComponentRef2.destroy).toHaveBeenCalled();
            expect(component.arrayComponents.length).toBe(2);
            expect(component.arrayComponents[0].instance.index).toBe(0);
            expect(component.arrayComponents[1].instance.index).toBe(1);
        });

        it("should handle empty arrayComponents gracefully", () => {
            component.arrayComponents = [];

            expect(() => component.deleteComponent(0)).not.toThrow();
        });

        it("should handle out of bounds index gracefully", () => {
            const mockComponentRef = jasmine.createSpyObj("ComponentRef", ["destroy"]);
            mockComponentRef.instance = { index: 0 };
            component.arrayComponents = [mockComponentRef];

            expect(() => component.deleteComponent(5)).not.toThrow();
        });
    });

    describe("addComponent", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();

            // Mock uploaderContainer ViewChild
            component.uploaderContainer = {
                createComponent: jasmine.createSpy("createComponent"),
            } as any;
        });

        it("should use first fileType as default when no type provided", () => {
            (component.uploaderContainer.createComponent as jasmine.Spy).and.returnValue({
                setInput: jasmine.createSpy(),
                instance: {
                    type: null,
                    index: 0,
                    outDelete: {
                        asObservable: () => of(),
                    },
                    fileSelected: {
                        asObservable: () => of(),
                    },
                },
            } as any);

            component.addComponent();

            const createdType = (
                component.uploaderContainer.createComponent as jasmine.Spy
            ).calls.mostRecent().returnValue.instance.type;
            expect(createdType).toEqual(component.fileTypes[0]);
        });

        it("should create component with specified type", () => {
            const customType = { value: "CUSTOM_TYPE", text: "Custom" };
            (component.uploaderContainer.createComponent as jasmine.Spy).and.returnValue({
                setInput: jasmine.createSpy(),
                instance: {
                    type: customType,
                    index: 0,
                    outDelete: {
                        asObservable: () => of(),
                    },
                    fileSelected: {
                        asObservable: () => of(),
                    },
                },
            } as any);

            component.addComponent(customType);

            const createdType = (
                component.uploaderContainer.createComponent as jasmine.Spy
            ).calls.mostRecent().returnValue.instance.type;
            expect(createdType).toEqual(customType);
        });

        it("should set fileTypes input on created component", () => {
            const mockSetInput = jasmine.createSpy();
            (component.uploaderContainer.createComponent as jasmine.Spy).and.returnValue({
                setInput: mockSetInput,
                instance: {
                    type: null,
                    index: 0,
                    outDelete: {
                        asObservable: () => of(),
                    },
                    fileSelected: {
                        asObservable: () => of(),
                    },
                },
            } as any);

            component.addComponent();

            expect(mockSetInput).toHaveBeenCalledWith("fileTypes", component.fileTypes);
        });

        it("should set allowedFileExtensions input on created component", () => {
            const mockSetInput = jasmine.createSpy();
            (component.uploaderContainer.createComponent as jasmine.Spy).and.returnValue({
                setInput: mockSetInput,
                instance: {
                    type: null,
                    index: 0,
                    outDelete: {
                        asObservable: () => of(),
                    },
                    fileSelected: {
                        asObservable: () => of(),
                    },
                },
            } as any);

            component.addComponent();

            expect(mockSetInput).toHaveBeenCalledWith(
                "allowedFileExtensions",
                component.allowedFileExtensions,
            );
        });

        it("should add component to arrayComponents", () => {
            const initialLength = component.arrayComponents.length;
            (component.uploaderContainer.createComponent as jasmine.Spy).and.returnValue({
                setInput: jasmine.createSpy(),
                instance: {
                    type: null,
                    index: 0,
                    outDelete: {
                        asObservable: () => of(),
                    },
                    fileSelected: {
                        asObservable: () => of(),
                    },
                },
            } as any);

            component.addComponent();

            expect(component.arrayComponents.length).toBe(initialLength + 1);
        });

        it("should set index for all components after adding", () => {
            (component.uploaderContainer.createComponent as jasmine.Spy).and.returnValue({
                setInput: jasmine.createSpy(),
                instance: {
                    type: null,
                    index: -1,
                    outDelete: {
                        asObservable: () => of(),
                    },
                    fileSelected: {
                        asObservable: () => of(),
                    },
                },
            } as any);

            component.arrayComponents = [];
            component.addComponent();
            component.addComponent();

            expect(component.arrayComponents[0].instance.index).toBe(1);
        });
    });

    describe("ngOnChanges", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
        });

        it("should clear invalidDates array", () => {
            component.invalidDates = [new Date(2024, 0, 1), new Date(2024, 0, 2)];
            component.inventories = [];

            component.ngOnChanges({} as any);

            expect(component.invalidDates.length).toBe(0);
        });

        it("should add all days of a month for a single inventory", () => {
            const inventory = createMockInventory(1);
            inventory.date = new Date(2024, 2, 15); // March 15, 2024
            component.inventories = [inventory];

            component.ngOnChanges({} as any);

            // Should have 31 days for March
            expect(component.invalidDates.length).toBe(31);
        });

        it("should generate dates for the correct year and month", () => {
            const inventory = createMockInventory(1);
            inventory.date = new Date(2024, 5, 10); // June 10, 2024
            component.inventories = [inventory];

            component.ngOnChanges({} as any);

            // Check first date
            expect(component.invalidDates[0]).toEqual(new Date(2024, 5, 1));
            // Check last date
            expect(component.invalidDates[30]).toEqual(new Date(2024, 5, 31));
        });

        it("should handle multiple inventories with different months", () => {
            const inventory1 = createMockInventory(1);
            inventory1.date = new Date(2024, 0, 10); // January 2024
            const inventory2 = createMockInventory(2);
            inventory2.date = new Date(2024, 1, 15); // February 2024

            component.inventories = [inventory1, inventory2];

            component.ngOnChanges({} as any);

            // 31 days for January + 31 days for February (loop goes 1-31 regardless)
            expect(component.invalidDates.length).toBe(62);
        });

        it("should handle multiple inventories in the same month", () => {
            const inventory1 = createMockInventory(1);
            inventory1.date = new Date(2024, 2, 5); // March 5, 2024
            const inventory2 = createMockInventory(2);
            inventory2.date = new Date(2024, 2, 20); // March 20, 2024

            component.inventories = [inventory1, inventory2];

            component.ngOnChanges({} as any);

            // 31 days + 31 days (duplicates allowed)
            expect(component.invalidDates.length).toBe(62);
        });

        it("should handle empty inventories array", () => {
            component.inventories = [];

            component.ngOnChanges({} as any);

            expect(component.invalidDates.length).toBe(0);
        });

        it("should reset invalidDates before adding new dates", () => {
            const inventory = createMockInventory(1);
            inventory.date = new Date(2024, 0, 1);
            component.inventories = [inventory];
            component.invalidDates = [new Date(2023, 11, 1), new Date(2023, 11, 2)];

            component.ngOnChanges({} as any);

            // Should only have the 31 new dates, not the old ones
            expect(component.invalidDates.length).toBe(31);
            expect(component.invalidDates).not.toContain(new Date(2023, 11, 1));
        });

        it("should add dates from day 1 to day 31", () => {
            const inventory = createMockInventory(1);
            inventory.date = new Date(2024, 3, 15); // April 15, 2024
            component.inventories = [inventory];

            component.ngOnChanges({} as any);

            // Check that we have days 1 through 31
            for (let day = 1; day < 32; day++) {
                const expectedDate = new Date(2024, 3, day);
                const found = component.invalidDates.some(
                    (date) => date.getTime() === expectedDate.getTime(),
                );
                expect(found).toBe(true);
            }
        });

        it("should handle inventories with dates in different years", () => {
            const inventory1 = createMockInventory(1);
            inventory1.date = new Date(2023, 11, 1); // December 2023
            const inventory2 = createMockInventory(2);
            inventory2.date = new Date(2024, 0, 1); // January 2024

            component.inventories = [inventory1, inventory2];

            component.ngOnChanges({} as any);

            expect(component.invalidDates.length).toBe(62);
            // Check first date is from 2023 December
            expect(component.invalidDates[0]).toEqual(new Date(2023, 11, 1));
            // Check date after 31st is from 2024 January
            expect(component.invalidDates[31]).toEqual(new Date(2024, 0, 1));
        });
    });

    describe("uploadAndLaunchLoading", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();

            // Mock uploaderContainer ViewChild to prevent createComponent errors
            component.uploaderContainer = {
                createComponent: jasmine.createSpy("createComponent").and.returnValue({
                    setInput: jasmine.createSpy("setInput"),
                    instance: {
                        type: null,
                        index: 0,
                        outDelete: {
                            asObservable: () => of(),
                        },
                        fileSelected: {
                            asObservable: () => of(),
                        },
                    },
                }),
            } as any;
        });

        it("should call launchLoadInputFiles with correct parameters", () => {
            const formData = new FormData();
            const inventoryId = 123;

            mockLoadingService.launchLoadInputFiles.and.returnValue(of({ taskId: 1 }));
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component.reloadInventoriesAndLoop, "emit");
            spyOn(component, "close");

            component.uploadAndLaunchLoading(formData, inventoryId);

            expect(mockLoadingService.launchLoadInputFiles).toHaveBeenCalledWith(
                inventoryId,
                formData,
            );
        });

        it("should emit sidebarVisibleChange with false on success", (done) => {
            const formData = new FormData();
            const inventoryId = 123;

            mockLoadingService.launchLoadInputFiles.and.returnValue(of({ taskId: 1 }));
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component.reloadInventoriesAndLoop, "emit");
            spyOn(component, "close");

            component.uploadAndLaunchLoading(formData, inventoryId);

            setTimeout(() => {
                expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
                done();
            }, 600);
        });

        it("should emit reloadInventoriesAndLoop with inventoryId on success", (done) => {
            const formData = new FormData();
            const inventoryId = 456;

            mockLoadingService.launchLoadInputFiles.and.returnValue(of({ taskId: 1 }));
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component.reloadInventoriesAndLoop, "emit");
            spyOn(component, "close");

            component.uploadAndLaunchLoading(formData, inventoryId);

            setTimeout(() => {
                expect(component.reloadInventoriesAndLoop.emit).toHaveBeenCalledWith(
                    inventoryId,
                );
                done();
            }, 600);
        });

        it("should call close on success", (done) => {
            const formData = new FormData();
            const inventoryId = 123;

            mockLoadingService.launchLoadInputFiles.and.returnValue(of({ taskId: 1 }));
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component.reloadInventoriesAndLoop, "emit");
            spyOn(component, "close");

            component.uploadAndLaunchLoading(formData, inventoryId);

            setTimeout(() => {
                expect(component.close).toHaveBeenCalled();
                done();
            }, 600);
        });

        it("should emit sidebarPurposeChange with 'upload' on error", (done) => {
            const formData = new FormData();
            const inventoryId = 123;

            mockLoadingService.launchLoadInputFiles.and.returnValue(
                throwError(() => new Error("Upload failed")),
            );
            spyOn(component.sidebarPurposeChange, "emit");

            component.uploadAndLaunchLoading(formData, inventoryId);

            setTimeout(() => {
                expect(component.sidebarPurposeChange.emit).toHaveBeenCalledWith(
                    "upload",
                );
                done();
            }, 600);
        });

        it("should not call close on error", (done) => {
            const formData = new FormData();
            const inventoryId = 123;

            mockLoadingService.launchLoadInputFiles.and.returnValue(
                throwError(() => new Error("Upload failed")),
            );
            spyOn(component, "close");
            spyOn(component.sidebarPurposeChange, "emit");

            component.uploadAndLaunchLoading(formData, inventoryId);

            setTimeout(() => {
                expect(component.close).not.toHaveBeenCalled();
                done();
            }, 600);
        });

        it("should not emit sidebarVisibleChange on error", (done) => {
            const formData = new FormData();
            const inventoryId = 123;

            mockLoadingService.launchLoadInputFiles.and.returnValue(
                throwError(() => new Error("Upload failed")),
            );
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component.sidebarPurposeChange, "emit");

            component.uploadAndLaunchLoading(formData, inventoryId);

            setTimeout(() => {
                expect(component.sidebarVisibleChange.emit).not.toHaveBeenCalled();
                done();
            }, 600);
        });
    });

    describe("downloadWorkspaceReferenceData", () => {
        beforeEach(() => {
            mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
            component.ngOnInit();
            fixture.detectChanges();
        });

        it("should call workspaceDownloadZipFile with correct parameters", (done) => {
            const mockBlob = new Blob(["test data"], { type: "application/zip" });
            const expectedFileName = "Test_Workspace_reference_data.zip";

            mockWorkspaceReferenceDataService.workspaceDownloadZipFile.and.returnValue(
                of(mockBlob),
            );
            mockWorkspaceReferenceDataService.getZipFileName.and.returnValue(
                expectedFileName,
            );

            component.downloadWorkspaceReferenceData();

            setTimeout(() => {
                expect(
                    mockWorkspaceReferenceDataService.workspaceDownloadZipFile,
                ).toHaveBeenCalledWith(1, "Test Org");
                expect(
                    mockWorkspaceReferenceDataService.getZipFileName,
                ).toHaveBeenCalledWith("Test Workspace");
                done();
            }, 100);
        });

        it("should handle different workspace and organization data", (done) => {
            const mockBlob = new Blob(["data"], { type: "application/zip" });
            const customWorkspace = { id: 999, name: "Custom Workspace" };
            const customOrg = { id: 888, name: "Custom Organization" };

            (mockUserService.currentWorkspace$ as BehaviorSubject<any>).next(
                customWorkspace,
            );
            (mockUserService.currentOrganization$ as BehaviorSubject<any>).next(
                customOrg,
            );

            mockWorkspaceReferenceDataService.workspaceDownloadZipFile.and.returnValue(
                of(mockBlob),
            );
            mockWorkspaceReferenceDataService.getZipFileName.and.returnValue(
                "Custom_Workspace_reference_data.zip",
            );

            component.downloadWorkspaceReferenceData();

            setTimeout(() => {
                expect(
                    mockWorkspaceReferenceDataService.workspaceDownloadZipFile,
                ).toHaveBeenCalledWith(999, "Custom Organization");
                expect(
                    mockWorkspaceReferenceDataService.getZipFileName,
                ).toHaveBeenCalledWith("Custom Workspace");
                done();
            }, 100);
        });
    });
});
