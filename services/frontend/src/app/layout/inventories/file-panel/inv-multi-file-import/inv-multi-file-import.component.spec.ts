/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";
import { SharedModule } from "src/app/core/shared/shared.module";
import { InvMultiFileImportComponent } from "./inv-multi-file-import.component";

describe("InvMultiFileImportComponent", () => {
    let component: InvMultiFileImportComponent;
    let fixture: ComponentFixture<InvMultiFileImportComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                FormsModule,
                SharedModule,
                TranslateModule.forRoot(),
                InvMultiFileImportComponent,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(InvMultiFileImportComponent);
        component = fixture.componentInstance;

        component.importForm = {
            get: jasmine.createSpy().and.returnValue({
                setValue: jasmine.createSpy(),
            }),
        };

        component.form = [
            { name: "info" },
            { name: "infra" },
            { name: "endUser" },
            { name: "appServices" },
            { name: "externalServices" },
        ];
        component.inventoryId = 42;
        component.selectedMenuIndex = 1;

        fixture.detectChanges();
        component.ngOnChanges();
    });

    it("should create component", () => {
        expect(component).toBeTruthy();
    });

    it("should set fileTypes and default rows based on selectedMenuIndex", () => {
        component.selectedMenuIndex = 0;
        component.ngOnChanges();
        expect(component.fileTypes).toHaveSize(0);
        expect(component.rows).toHaveSize(0);

        component.selectedMenuIndex = 1;
        component.ngOnChanges();
        expect(component.fileTypes).toHaveSize(2);
        expect(component.fileTypes.map((t) => t.key)).toEqual([
            "DATACENTER",
            "EQUIPEMENT_PHYSIQUE",
        ]);
        expect(component.rows.map((r) => r.type.key)).toEqual([
            "DATACENTER",
            "EQUIPEMENT_PHYSIQUE",
        ]);

        component.selectedMenuIndex = 2;
        component.ngOnChanges();
        expect(component.fileTypes).toHaveSize(1);
        expect(component.fileTypes.map((t) => t.key)).toEqual(["EQUIPEMENT_PHYSIQUE"]);

        component.selectedMenuIndex = 3;
        component.ngOnChanges();
        expect(component.fileTypes).toHaveSize(2);
        expect(component.fileTypes.map((t) => t.key)).toEqual([
            "EQUIPEMENT_VIRTUEL",
            "APPLICATION",
        ]);

        component.selectedMenuIndex = 4;
        component.ngOnChanges();
        expect(component.fileTypes).toHaveSize(2);
        expect(component.fileTypes.map((t) => t.key)).toEqual([
            "EQUIPEMENT_VIRTUEL",
            "AI_SERVICES",
        ]);
    });

    it("should add a new row defaulting to the first allowed file type", () => {
        component.selectedMenuIndex = 4;
        component.ngOnChanges();
        const initialSize = component.rows.length;

        component.addRow();

        expect(component.rows).toHaveSize(initialSize + 1);
        expect(component.rows[initialSize].type.key).toEqual("EQUIPEMENT_VIRTUEL");
    });

    it("should not add a row when there are no allowed file types", () => {
        component.selectedMenuIndex = 0;
        component.ngOnChanges();

        component.addRow();

        expect(component.rows).toHaveSize(0);
    });

    it("should delete the selected row and update form validity", () => {
        spyOn(component, "updateFormValidity");
        const rowToDelete = component.rows[0];

        component.onDeleteRow(rowToDelete);

        expect(component.rows.find((r) => r.id === rowToDelete.id)).toBeUndefined();
        expect(component.updateFormValidity).toHaveBeenCalled();
    });

    it("should add selected file to the row and update form validity", () => {
        const mockFile = new File(["test"], "test.csv");
        const event = { files: [mockFile] };
        const row = component.rows[0];
        spyOn(component, "updateFormValidity");

        component.onSelectFile(event, row);

        expect(row.file).toBe(mockFile);
        expect(component.updateFormValidity).toHaveBeenCalled();
    });

    it("should not add file or call updateFormValidity if no file is selected", () => {
        const event = { files: [] };
        const row = component.rows[0];
        spyOn(component, "updateFormValidity");

        component.onSelectFile(event, row);

        expect(row.file).toBeUndefined();
        expect(component.updateFormValidity).not.toHaveBeenCalled();
    });

    it("should build FormData with the selected file for the current tab", () => {
        component.rows[0].file = new File(["data"], "datacenter.csv");

        const formData = component.getFormData();

        expect(formData.has("DATACENTER")).toBeTrue();
        expect(formData.has("EQUIPEMENT_PHYSIQUE")).toBeFalse();
    });

    it("should aggregate files selected across every visited tab", () => {
        component.rows[0].file = new File(["data"], "datacenter.csv");

        fixture.componentRef.setInput("selectedMenuIndex", 2);
        fixture.detectChanges();
        component.rows[0].file = new File(["data"], "physique.csv");

        const formData = component.getFormData();

        expect(formData.has("DATACENTER")).toBeTrue();
        expect(formData.has("EQUIPEMENT_PHYSIQUE")).toBeTrue();
    });

    it("should return an empty FormData when no files were selected", () => {
        const formData = component.getFormData();

        expect(Array.from(formData.keys())).toHaveSize(0);
    });

    it("resetForm", () => {
        component.rows[0].file = new File(["data"], "file.csv");
        component.resetForm();
        expect(component.rows.every((r) => !r.file)).toBeTrue();
    });

    it("should keep a selected file when switching tabs and back (via input binding)", () => {
        fixture.componentRef.setInput("selectedMenuIndex", 1);
        fixture.detectChanges();
        component.rows[0].file = new File(["data"], "datacenter.csv");

        fixture.componentRef.setInput("selectedMenuIndex", 2);
        fixture.detectChanges();
        expect(component.rows.every((r) => !r.file)).toBeTrue();

        fixture.componentRef.setInput("selectedMenuIndex", 1);
        fixture.detectChanges();
        expect(component.rows[0].file?.name).toEqual("datacenter.csv");
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
