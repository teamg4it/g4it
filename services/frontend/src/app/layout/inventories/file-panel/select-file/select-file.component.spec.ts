/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { FileUploadModule } from "primeng/fileupload";

import { CommonModule } from "@angular/common";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { SelectFileComponent } from "./select-file.component";

describe("SelectFileComponent", () => {
    let component: SelectFileComponent;
    let fixture: ComponentFixture<SelectFileComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SelectFileComponent],
            imports: [
                CommonModule,
                ReactiveFormsModule,
                HttpClientTestingModule,
                FileUploadModule,
                DropdownModule,
                ButtonModule,
                FormsModule,
                TranslateModule.forRoot(),
            ],
            providers: [TranslatePipe, TranslateService],
            schemas: [NO_ERRORS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(SelectFileComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should emit outDelete if onDeleteButton is called", () => {
        spyOn(component.outDelete, "emit");
        component.onDeleteButton();
        expect(component.outDelete.emit).toHaveBeenCalled();
    });

    it("should set file when onFileChange is called with selected file", () => {
        const file = new File(["content"], "test.csv", { type: "text/csv" });
        const fileInput = {
            currentFiles: [file],
        };
        component.onFileChange(fileInput);
        expect(component.file).toBe(file);
    });

    it("should not set file when onFileChange is called with null selected file", () => {
        const fileInput = {
            currentFiles: null,
        };
        component.onFileChange(fileInput);
        expect(component.file).toBeUndefined();
    });
});
