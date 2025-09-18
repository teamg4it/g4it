import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { MultiFileImportComponent } from "./multi-file-import.component";

describe("MultiFileImportComponent", () => {
    let component: MultiFileImportComponent;
    let fixture: ComponentFixture<MultiFileImportComponent>;

    const mockLoadingService = {
        launchLoadInputFiles: jasmine.createSpy().and.returnValue(of(true)),
    };

    const mockActivatedRoute = {
        snapshot: {
            paramMap: {
                get: () => "mock-ds-id",
            },
        },
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [MultiFileImportComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                FormsModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                { provide: LoadingDataService, useValue: mockLoadingService },
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(MultiFileImportComponent);
        component = fixture.componentInstance;

        component.importForm = {
            get: jasmine.createSpy().and.returnValue({
                setValue: jasmine.createSpy(),
            }),
        };

        component.form = [{ name: "nonCloud" }, { name: "cloud" }];
        component.selectedMenuIndex = 0;

        fixture.detectChanges();
    });

    it("should create component", () => {
        expect(component).toBeTruthy();
    });

    it("should delete the selected file for the given key and update form validity", () => {
        component.selectedFiles = {
            DATACENTER: new File(["content"], "datacenter.csv"),
            EQUIPEMENT_PHYSIQUE: new File(["content"], "physique.csv"),
        };
        spyOn(component, "updateFormValidity");

        component.onDeleteButton("DATACENTER");

        expect(component.selectedFiles["DATACENTER"]).toBeUndefined();
        expect(component.selectedFiles["EQUIPEMENT_PHYSIQUE"]).toBeDefined();
        expect(component.updateFormValidity).toHaveBeenCalled();
    });

    it("should not throw if the key does not exist in selectedFiles", () => {
        component.selectedFiles = {
            EQUIPEMENT_PHYSIQUE: new File(["content"], "physique.csv"),
        };
        spyOn(component, "updateFormValidity");

        expect(() => component.onDeleteButton("NON_EXISTENT_KEY")).not.toThrow();
        expect(component.selectedFiles["EQUIPEMENT_PHYSIQUE"]).toBeDefined();
        expect(component.updateFormValidity).toHaveBeenCalled();
    });

    it("should add selected file to selectedFiles and update form validity", () => {
        const mockFile = new File(["test"], "test.csv");
        const event = { files: [mockFile] };
        spyOn(component, "updateFormValidity");

        component.onSelectFile(event, "DATACENTER");

        expect(component.selectedFiles["DATACENTER"]).toBe(mockFile);
        expect(component.updateFormValidity).toHaveBeenCalled();
    });

    it("should not add file or call updateFormValidity if no file is selected", () => {
        const event = { files: [] };
        spyOn(component, "updateFormValidity");

        component.onSelectFile(event, "DATACENTER");

        expect(component.selectedFiles["DATACENTER"]).toBeUndefined();
        expect(component.updateFormValidity).not.toHaveBeenCalled();
    });

    it("should not add file or call updateFormValidity if event is null", () => {
        spyOn(component, "updateFormValidity");

        component.onSelectFile(null, "DATACENTER");

        expect(component.selectedFiles["DATACENTER"]).toBeUndefined();
        expect(component.updateFormValidity).not.toHaveBeenCalled();
    });

    it("should set fileTypes based on selectedMenuIndex", () => {
        component.selectedMenuIndex = 0;
        component.ngOnChanges();
        expect(component.fileTypes.length).toBe(1);

        component.selectedMenuIndex = 1;
        component.ngOnChanges();
        expect(component.fileTypes.length).toBe(1);

        component.selectedMenuIndex = 2;
        component.ngOnChanges();
        expect(component.fileTypes.length).toBe(3);

        component.selectedMenuIndex = 3;
        component.ngOnChanges();
        expect(component.fileTypes.length).toBe(1);
    });

    it("should call launchLoadInputFiles and handle success", () => {
        spyOn(component as any, "createFormData").and.callThrough();
        spyOn(component as any, "handleUploadSuccess");

        component.selectedFiles = {
            DATACENTER: new File(["data"], "file.csv"),
        };
        component.fileTypes = [{ key: "DATACENTER", label: "Datacenter" }];

        component.uploadAllFiles();

        expect(mockLoadingService.launchLoadInputFiles).toHaveBeenCalled();
        expect((component as any).handleUploadSuccess).toHaveBeenCalled();
    });

    it("should reset state and emit submit on successful upload", () => {
        spyOn(component.formSubmit, "emit");
        spyOn(component, "resetForm");
        spyOn(component, "updateFormValidity");

        (component as any).handleUploadSuccess();

        expect(component.updateFormValidity).toHaveBeenCalled();
        expect(component.formSubmit.emit).toHaveBeenCalledWith("submit");
    });

    it("resetForm", () => {
        component.resetForm();
        expect(Object.keys(component.selectedFiles).length).toBe(0);
    });

    it("should update form field value based on isUploadEnabled", () => {
        spyOn(component, "isUploadEnabled").and.returnValue(true);
        component.updateFormValidity();

        const formField = component.importForm.get(
            component.form[component.selectedMenuIndex!].name,
        );
        expect(formField.setValue).toHaveBeenCalledWith("enabled");
    });
});
