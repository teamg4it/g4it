import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { ElementRef } from "@angular/core";
import { of } from "rxjs";
import { ReleaseNotesService } from "src/app/core/service/business/release-notes.service";

import { ReleaseNotesComponent } from "./release-notes.component";

describe("ReleaseNotesComponent", () => {
    let component: ReleaseNotesComponent;
    let fixture: ComponentFixture<ReleaseNotesComponent>;
    let releaseNotesService: jasmine.SpyObj<ReleaseNotesService>;
    let originalSanitizerDescriptor: PropertyDescriptor | undefined;

    beforeEach(async () => {
        originalSanitizerDescriptor = Object.getOwnPropertyDescriptor(
            window,
            "Sanitizer",
        );
        releaseNotesService = jasmine.createSpyObj("ReleaseNotesService", [
            "getLatestReleaseNotes",
            "markAsSeen",
        ]);
        releaseNotesService.getLatestReleaseNotes.and.returnValue(of(null));
        releaseNotesService.markAsSeen.and.returnValue(of({}));

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), ReleaseNotesComponent],
            providers: [{ provide: ReleaseNotesService, useValue: releaseNotesService }],
        }).compileComponents();

        fixture = TestBed.createComponent(ReleaseNotesComponent);
        component = fixture.componentInstance;
    });

    afterEach(() => {
        if (originalSanitizerDescriptor) {
            Object.defineProperty(window, "Sanitizer", originalSanitizerDescriptor);
        } else {
            delete (window as Window & { Sanitizer?: unknown }).Sanitizer;
        }
    });

    it("does not open the dialog when there are no release notes", () => {
        const dialog = { showModal: jasmine.createSpy("showModal") };
        component.dialog = new ElementRef(dialog as unknown as HTMLDialogElement);

        component.ngOnInit();

        expect(component).toBeTruthy();
        expect(releaseNotesService.getLatestReleaseNotes).toHaveBeenCalled();
        expect(dialog.showModal).not.toHaveBeenCalled();
    });

    it("renders release notes and opens the dialog when the Sanitizer API is unavailable", () => {
        Object.defineProperty(window, "Sanitizer", {
            configurable: true,
            value: undefined,
        });
        const content = "<p>Release details</p>";
        releaseNotesService.getLatestReleaseNotes.and.returnValue(of({ content }));
        const section = document.createElement("section");
        const dialog = { showModal: jasmine.createSpy("showModal") };
        component.section = new ElementRef(section);
        component.dialog = new ElementRef(dialog as unknown as HTMLDialogElement);

        component.ngOnInit();

        expect(section.innerHTML).toBe(content);
        expect(dialog.showModal).toHaveBeenCalled();
    });

    it("uses the Sanitizer API while allowing release-note classes and line breaks", () => {
        const content = '<p class="release">Release<br>details</p>';
        releaseNotesService.getLatestReleaseNotes.and.returnValue(of({ content }));
        const allowAttribute = jasmine.createSpy("allowAttribute");
        const allowElement = jasmine.createSpy("allowElement");
        let sanitizerInstance: MockSanitizer;
        class MockSanitizer {
            allowAttribute = sanitizer.allowAttribute;
            allowElement = sanitizer.allowElement;

            constructor() {
                sanitizerInstance = this;
            }
        }
        const sanitizer = { allowAttribute, allowElement };
        Object.defineProperty(window, "Sanitizer", {
            configurable: true,
            value: MockSanitizer,
        });
        const setHTML = jasmine.createSpy("setHTML");
        const section = { setHTML } as unknown as HTMLElement;
        const dialog = { showModal: jasmine.createSpy("showModal") };
        component.section = new ElementRef(section);
        component.dialog = new ElementRef(dialog as unknown as HTMLDialogElement);

        component.ngOnInit();

        expect(allowAttribute).toHaveBeenCalledOnceWith("class");
        expect(allowElement).toHaveBeenCalledOnceWith("br");
        expect(setHTML).toHaveBeenCalledOnceWith(content, {
            sanitizer: sanitizerInstance!,
        });
        expect(dialog.showModal).toHaveBeenCalled();
    });

    it("closes the dialog without marking notes as seen when the option is unchecked", () => {
        const dialog = { close: jasmine.createSpy("close") };
        component.dialog = new ElementRef(dialog as unknown as HTMLDialogElement);

        component.closeDialog();

        expect(dialog.close).toHaveBeenCalled();
        expect(releaseNotesService.markAsSeen).not.toHaveBeenCalled();
    });

    it("marks release notes as seen when closing with the hide option checked", () => {
        const dialog = { close: jasmine.createSpy("close") };
        component.dialog = new ElementRef(dialog as unknown as HTMLDialogElement);
        component.checked.set(true);

        component.closeDialog();

        expect(dialog.close).toHaveBeenCalled();
        expect(releaseNotesService.markAsSeen).toHaveBeenCalled();
        expect(component.checked()).toBeTrue();
    });
});
