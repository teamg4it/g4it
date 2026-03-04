import { SimpleChange } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CommonEditorComponent } from "./common-editor.component";

import { CommonModule } from "@angular/common";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { DomSanitizer } from "@angular/platform-browser";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { SharedModule } from "src/app/core/shared/shared.module";

describe("CommonEditorComponent", () => {
    let component: CommonEditorComponent;
    let fixture: ComponentFixture<CommonEditorComponent>;
    let messageService: MessageService;
    let confirmationService: ConfirmationService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SharedModule,
                TranslateModule.forRoot(),
                CommonModule,
                ButtonModule,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                TranslatePipe,
                TranslateService,
                {
                    provide: DomSanitizer,
                    useValue: {
                        sanitize: (_ctx: any, val: string) => val,
                        bypassSecurityTrustHtml: (val: string) => val,
                    },
                },
            ],
        }).compileComponents();
        fixture = TestBed.createComponent(CommonEditorComponent);
        component = fixture.componentInstance;
        // Get the component's own service instances (from component-level providers)
        messageService = fixture.debugElement.injector.get(MessageService);
        confirmationService = fixture.debugElement.injector.get(ConfirmationService);
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should remove style", () => {
        expect(component.removeStylesFromText("")).toBe("");
        expect(component.removeStylesFromText("<p>  </p>")).toBe("  ");
        expect(component.removeStylesFromText("<p>  </p><p>  </p>")).toBe("    ");
    });

    describe("ngOnChanges", () => {
        it("should reset editor text when content changes to null", () => {
            component.editorTextValue = "previous";
            component.editorTextValueUnmodified = "previous";

            component.ngOnChanges({
                content: new SimpleChange("previous", null, false),
            });

            expect(component.editorTextValue).toBe("");
            expect(component.editorTextValueUnmodified).toBe("");
        });

        it("should set editor text when content has a value", () => {
            component.ngOnChanges({
                content: new SimpleChange(null, "new content", true),
            });

            expect(component.editorTextValue).toBe("new content");
            expect(component.editorTextValueUnmodified).toBe("new content");
        });

        it("should not change editor text when content change is undefined", () => {
            component.editorTextValue = "existing";
            component.editorTextValueUnmodified = "existing";

            component.ngOnChanges({});

            expect(component.editorTextValue).toBe("existing");
            expect(component.editorTextValueUnmodified).toBe("existing");
        });
    });

    describe("saveContent", () => {
        it("should show error when editor text is empty", () => {
            spyOn(messageService, "add");
            component.editorTextValue = "";

            component.saveContent();

            expect(messageService.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "common.note.no-content",
            });
        });

        it("should show error when editor text contains only whitespace", () => {
            spyOn(messageService, "add");
            component.editorTextValue = "<p>   </p>";

            component.saveContent();

            expect(messageService.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "common.note.no-content",
            });
        });

        it("should show error when content exceeds max length", () => {
            spyOn(messageService, "add");
            component.maxContentLength = 10;
            component.editorTextValue = "This content is way too long";

            component.saveContent();

            expect(messageService.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "common.note.content-length-exceeded",
            });
        });

        it("should emit sanitized content when valid", () => {
            spyOn(component.saveValue, "emit");
            component.editorTextValue = "<p>Valid content</p>";

            component.saveContent();

            expect(component.saveValue.emit).toHaveBeenCalledWith("<p>Valid content</p>");
        });
    });

    describe("cancelContent", () => {
        it("should call confirmationService.confirm", () => {
            spyOn(confirmationService, "confirm");
            const event = { target: document.createElement("button") };

            component.cancelContent(event);

            expect(confirmationService.confirm).toHaveBeenCalled();
        });

        it("should reset editor text and emit outClose on accept", () => {
            spyOn(component.outClose, "emit");
            component.editorTextValue = "modified";
            component.editorTextValueUnmodified = "original";

            spyOn(confirmationService, "confirm").and.callFake((config: any) => {
                config.accept();
                return confirmationService;
            });

            const event = { target: document.createElement("button") };
            component.cancelContent(event);

            expect(component.editorTextValue).toBe("original");
            expect(component.outClose.emit).toHaveBeenCalled();
        });
    });

    describe("deleteContent", () => {
        it("should call confirmationService.confirm", () => {
            spyOn(confirmationService, "confirm");
            const event = { target: document.createElement("button") };

            component.deleteContent(event);

            expect(confirmationService.confirm).toHaveBeenCalled();
        });

        it("should clear editor text and emit delete on accept", () => {
            spyOn(component.delete, "emit");
            component.editorTextValue = "some content";

            spyOn(confirmationService, "confirm").and.callFake((config: any) => {
                config.accept();
                return confirmationService;
            });

            const event = { target: document.createElement("button") };
            component.deleteContent(event);

            expect(component.editorTextValue).toBe("");
            expect(component.delete.emit).toHaveBeenCalled();
        });

        it("should not clear editor text on reject", () => {
            component.editorTextValue = "some content";

            spyOn(confirmationService, "confirm").and.callFake((config: any) => {
                config.reject();
                return confirmationService;
            });

            const event = { target: document.createElement("button") };
            component.deleteContent(event);

            expect(component.editorTextValue).toBe("some content");
        });
    });
});
