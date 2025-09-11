import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import { InDatacenterRest } from "src/app/core/interfaces/input.interface";
import { FileSystemBusinessService } from "src/app/core/service/business/file-system.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesImportComponent } from "./digital-services-import.component";

const mockTemplateFileService = {
    getTemplateFiles: jasmine.createSpy().and.returnValue(
        of([
            {
                name: "datacenter",
                type: "csv",
                metadata: {
                    creationTime: new Date(),
                    size: "200",
                },
            },
        ]),
    ),
    transformTemplateFiles: jasmine.createSpy().and.callFake((files: any) => files),
    getdownloadTemplateFile: jasmine.createSpy(),
};

const mockDigitalServicesData = {
    getDsTasks: jasmine.createSpy().and.returnValue(of({ tasks: [] })),
};

const mockActivatedRoute = {
    snapshot: {
        paramMap: new Map([["digitalServiceId", "123"]]),
    },
};

const mockFileSystemBusinessService = {
    downloadFile: jasmine.createSpy(),
    getTaskDetail: jasmine.createSpy(),
};

const mockUserService = {
    currentSubscriber$: of({ name: "Subscriber A" }),
    currentOrganization$: of({ name: "Org A" }),
};

describe("DigitalServicesImportComponent", () => {
    let component: DigitalServicesImportComponent;
    let fixture: ComponentFixture<DigitalServicesImportComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesImportComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                FormsModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                { provide: TemplateFileService, useValue: mockTemplateFileService },
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesData,
                },
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                {
                    provide: FileSystemBusinessService,
                    useValue: mockFileSystemBusinessService,
                },
                { provide: UserService, useValue: mockUserService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesImportComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create component", () => {
        expect(component).toBeTruthy();
    });

    it("should return correct class and tooltip for COMPLETED status", () => {
        const css = component.getClassStatus("COMPLETED", true);
        const tooltip = component.getClassStatus("COMPLETED", false);
        expect(css).toContain("green-tag");
        expect(tooltip).toBe("common.completed");
    });

    it("should call transformTemplateFiles if template files exist", () => {
        const fileData = [
            {
                name: "datacenter",
                type: "csv",
                metadata: {
                    creationTime: new Date(),
                    size: "200",
                },
            },
        ];
        mockTemplateFileService.getTemplateFiles.and.returnValue(of(fileData));

        component.getTemplates();

        expect(mockTemplateFileService.transformTemplateFiles).toHaveBeenCalled();
    });

    it("should call downloadTemplateFile with correct params", () => {
        component.downloadTemplateFile("template.csv");
        expect(mockTemplateFileService.getdownloadTemplateFile).toHaveBeenCalledWith(
            "template.csv",
            component.dsTemplateParam,
        );
    });

    it("should call fileSystemBusinessService.downloadFile with correct parameters", () => {
        component.selectedSubscriber = "sub";
        component.selectedOrganization = "org";
        component.digitalServicesId = "123";
        component.downloadFileDs("task1");
        expect(mockFileSystemBusinessService.downloadFile).toHaveBeenCalledWith(
            "task1",
            "sub",
            "org",
            "123",
        );
    });

    it("should call getTaskDetail with task id", () => {
        component.getTaskDetail("456");
        expect(mockFileSystemBusinessService.getTaskDetail).toHaveBeenCalledWith("456");
    });

    it("should update selectedMenuIndex and activate the selected tab", () => {
        component.selectTab(1);
        expect(component.selectedMenuIndex).toBe(1);
        expect(component.importDetails.menu[1].active).toBeTrue();
    });

    it("should reset form and emit sidebarVisibleChange as false", () => {
        spyOn(component.sidebarVisibleChange, "emit");
        component.closeSidebar();
        expect(component.importForm.value).toEqual({
            nonCloud: null,
            cloud: null,
            terminal: null,
            network: null,
        });
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should call previousTab and select previous tab if index > 0", () => {
        spyOn(component, "selectTab");
        spyOn(component, "focusFirstTemplate");
        component.previousTab(1);
        expect(component.selectTab).toHaveBeenCalledWith(0);
        expect(component.focusFirstTemplate).toHaveBeenCalled();
    });

    it("should not call selectTab in previousTab if index is 0", () => {
        spyOn(component, "selectTab");
        component.previousTab(0);
        expect(component.selectTab).not.toHaveBeenCalled();
    });

    it("should call nextTab and select next tab if index < menu.length - 1", () => {
        spyOn(component, "selectTab");
        spyOn(component, "focusFirstTemplate");
        component.nextTab(0);
        expect(component.selectTab).toHaveBeenCalledWith(1);
        expect(component.focusFirstTemplate).toHaveBeenCalled();
    });

    it("should not call selectTab in nextTab if index is at last tab", () => {
        spyOn(component, "selectTab");
        const lastIndex = component.importDetails.menu.length - 1;
        component.nextTab(lastIndex);
        expect(component.selectTab).not.toHaveBeenCalled();
    });

    it("should call downloadFileDs when task status is COMPLETED_WITH_ERRORS", () => {
        spyOn(component, "downloadFileDs");
        const task = { id: "1", status: "COMPLETED_WITH_ERRORS" } as any;
        component.downloadFile(task);
        expect(component.downloadFileDs).toHaveBeenCalledWith("1");
    });

    it("should call downloadFileDs when task status is SKIPPED", () => {
        spyOn(component, "downloadFileDs");
        const task = { id: "2", status: "SKIPPED" } as any;
        component.downloadFile(task);
        expect(component.downloadFileDs).toHaveBeenCalledWith("2");
    });

    it("should call getTaskDetail when task status is not COMPLETED_WITH_ERRORS or SKIPPED", () => {
        spyOn(component, "getTaskDetail");
        const task = { id: "3", status: "COMPLETED" } as any;
        component.downloadFile(task);
        expect(component.getTaskDetail).toHaveBeenCalledWith("3");
    });

    it("should focus first template if addFocusElement is defined", () => {
        const focusSpy = jasmine.createSpy("focus");
        component.addFocusElement = { nativeElement: { focus: focusSpy } } as any;
        component.focusFirstTemplate();
        expect(focusSpy).toHaveBeenCalled();
    });

    it("should not throw if addFocusElement is undefined in focusFirstTemplate", () => {
        component.addFocusElement = undefined as any;
        expect(() => component.focusFirstTemplate()).not.toThrow();
    });

    it("should clear interval on ngOnDestroy", () => {
        component.digitalServiceInterval = setInterval(() => {}, 1000);
        spyOn(window, "clearInterval").and.callThrough();
        component.ngOnDestroy();
        expect(clearInterval).toHaveBeenCalledWith(component.digitalServiceInterval);
    });

    it("should set templateFilesDescription to [] if no template files", () => {
        mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
        component.getTemplates();
        expect(component.templateFilesDescription).toEqual([]);
    });

    it("should call getDigitalServiceStatus and loopLoadDigitalServices on onFormSubmit with 'submit'", async () => {
        spyOn(component, "getDigitalServiceStatus").and.returnValue(Promise.resolve());
        spyOn(component, "loopLoadDigitalServices");
        component.digitalServiceInterval = setInterval(() => {}, 1000);
        await component.onFormSubmit("submit");
        expect(component.getDigitalServiceStatus).toHaveBeenCalled();
        expect(component.loopLoadDigitalServices).toHaveBeenCalled();
    });

    it("should not call getDigitalServiceStatus or loopLoadDigitalServices on onFormSubmit with non-submit event", async () => {
        spyOn(component, "getDigitalServiceStatus");
        spyOn(component, "loopLoadDigitalServices");
        await component.onFormSubmit("not-submit");
        expect(component.getDigitalServiceStatus).not.toHaveBeenCalled();
        expect(component.loopLoadDigitalServices).not.toHaveBeenCalled();
    });

    it("should call inDatacentersService.get with digitalServicesId and update digitalServiceStore", async () => {
        const mockInDatacenters = [{ name: "datacenters" }] as InDatacenterRest[];
        const inDatacentersService = TestBed.inject(InDatacentersService);
        const digitalServiceStore = TestBed.inject(DigitalServiceStoreService);

        spyOn(inDatacentersService, "get").and.returnValue(of(mockInDatacenters));
        spyOn(digitalServiceStore, "setInDatacenters");
        spyOn(digitalServiceStore, "initInPhysicalEquipments");
        spyOn(digitalServiceStore, "initInVirtualEquipments");

        await component.callInputApis();

        expect(inDatacentersService.get).toHaveBeenCalledWith(
            component.digitalServicesId,
        );
        expect(digitalServiceStore.setInDatacenters).toHaveBeenCalledWith(
            mockInDatacenters,
        );
        expect(digitalServiceStore.initInPhysicalEquipments).toHaveBeenCalledWith(
            component.digitalServicesId,
        );
        expect(digitalServiceStore.initInVirtualEquipments).toHaveBeenCalledWith(
            component.digitalServicesId,
        );
    });
});
