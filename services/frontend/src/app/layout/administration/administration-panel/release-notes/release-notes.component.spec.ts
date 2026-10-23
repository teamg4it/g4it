import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of, throwError } from "rxjs";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { TranslateService } from "@ngx-translate/core";

import { ReleaseNotesComponent } from "./release-notes.component";

describe("ReleaseNotesComponent", () => {
    let component: ReleaseNotesComponent;
    let fixture: ComponentFixture<ReleaseNotesComponent>;
    let administrationService: jasmine.SpyObj<AdministrationService>;
    let messageService: jasmine.SpyObj<MessageService>;
    let translateService: jasmine.SpyObj<TranslateService>;

    beforeEach(async () => {
        administrationService = jasmine.createSpyObj("AdministrationService", [
            "getLatestReleaseNotes",
            "saveReleaseNotes",
        ]);
        administrationService.getLatestReleaseNotes.and.returnValue(
            of({ content: "Published notes" }),
        );
        administrationService.saveReleaseNotes.and.returnValue(of({}));
        messageService = jasmine.createSpyObj("MessageService", ["add"]);
        translateService = jasmine.createSpyObj("TranslateService", ["instant"]);
        translateService.instant.and.callFake((key: string) => key);

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), ReleaseNotesComponent],
            providers: [
                { provide: AdministrationService, useValue: administrationService },
                { provide: MessageService, useValue: messageService },
                { provide: TranslateService, useValue: translateService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ReleaseNotesComponent);
        component = fixture.componentInstance;
        component.ngOnInit();
    });

    it("loads the published notes as the initial pristine form value", () => {
        expect(component).toBeTruthy();
        expect(administrationService.getLatestReleaseNotes).toHaveBeenCalled();
        expect(component.initialNotes()).toBe("Published notes");
        expect(component.form.value.releaseNotes).toBe("Published notes");
        expect(component.form.pristine).toBeTrue();
    });

    it("keeps the empty initial value when no notes have been published", () => {
        administrationService.getLatestReleaseNotes.and.returnValue(of({ content: "" }));

        component.ngOnInit();

        expect(component.initialNotes()).toBe("");
        expect(component.form.value.releaseNotes).toBe("");
    });

    it("updates the editor value and marks the form dirty", () => {
        component.onEditorContentChange("Updated notes");

        expect(component.form.value.releaseNotes).toBe("Updated notes");
        expect(component.form.dirty).toBeTrue();
    });

    it("warns and does not publish when the content is empty", () => {
        component.onEditorContentChange("");

        component.saveReleaseNotes();

        expect(administrationService.saveReleaseNotes).not.toHaveBeenCalled();
        expect(messageService.add).toHaveBeenCalledWith({
            severity: "warn",
            summary: "common.attention",
            detail: "administration.release-notes.save-invalid",
        });
    });

    it("does not publish unchanged content", () => {
        component.onEditorContentChange("Published notes");

        component.saveReleaseNotes();

        expect(administrationService.saveReleaseNotes).not.toHaveBeenCalled();
        expect(messageService.add).toHaveBeenCalledWith({
            severity: "info",
            summary: "common.attention",
            detail: "administration.release-notes.save-unchanged",
        });
    });

    it("publishes changed content, updates the baseline and shows success", () => {
        component.onEditorContentChange("New release notes");

        component.saveReleaseNotes();

        expect(administrationService.saveReleaseNotes).toHaveBeenCalledWith({
            content: "New release notes",
        });
        expect(component.initialNotes()).toBe("New release notes");
        expect(component.form.pristine).toBeTrue();
        expect(messageService.add).toHaveBeenCalledWith({
            severity: "success",
            summary: "common.success",
            detail: "administration.release-notes.save-success",
        });
    });

    it("keeps the current edits and reports an error when publishing fails", () => {
        administrationService.saveReleaseNotes.and.returnValue(
            throwError(() => new Error("Save failed")),
        );
        component.onEditorContentChange("New release notes");

        component.saveReleaseNotes();

        expect(component.initialNotes()).toBe("Published notes");
        expect(component.form.value.releaseNotes).toBe("New release notes");
        expect(component.form.dirty).toBeTrue();
        expect(messageService.add).toHaveBeenCalledWith({
            severity: "error",
            summary: "common.error",
            detail: "administration.release-notes.save-error",
        });
    });

    it("undoes edits by restoring the last published content", () => {
        component.onEditorContentChange("Unpublished edits");

        component.undoChanges();

        expect(component.form.value.releaseNotes).toBe("Published notes");
        expect(component.form.pristine).toBeTrue();
    });
});
