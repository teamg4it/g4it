import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { ClipboardService } from "ngx-clipboard";
import { LinkCreatePopupComponent } from "./link-create-popup.component";

describe("LinkCreatePopupComponent", () => {
    let component: LinkCreatePopupComponent;
    let fixture: ComponentFixture<LinkCreatePopupComponent>;

    const mockClipboard = {
        copy: jasmine.createSpy("copy"),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot()],
            declarations: [LinkCreatePopupComponent],
            providers: [{ provide: ClipboardService, useValue: mockClipboard }],
        }).compileComponents();

        fixture = TestBed.createComponent(LinkCreatePopupComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("closePopup should emit outClose", () => {
        spyOn(component.outClose, "emit");
        component.closePopup();
        expect(component.outClose.emit).toHaveBeenCalled();
    });

    it("copyUrl should set isLinkCopied and call clipboard.copy with sharedLink value", () => {
        fixture.componentRef.setInput("sharedLink", "https://bound-from-parent.com");
        component.copyUrl();
        expect(component.isLinkCopied).toBeTrue();
        expect(mockClipboard.copy).toHaveBeenCalledWith("https://bound-from-parent.com");
    });

    it("moveCaretToStart should set caret at start and reset scrollLeft", () => {
        const inputEl = document.createElement("input");
        inputEl.value = "abcdef";
        inputEl.scrollLeft = 25;
        const selSpy = spyOn(inputEl, "setSelectionRange");
        component.moveCaretToStart(inputEl);
        expect(selSpy).toHaveBeenCalledWith(0, 0);
        expect(inputEl.scrollLeft).toBe(0);
    });
});
