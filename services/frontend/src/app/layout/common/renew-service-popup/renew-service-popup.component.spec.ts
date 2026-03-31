import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { ClipboardService } from "ngx-clipboard";
import { RenewServicePopupComponent } from "./renew-service-popup.component";

describe("RenewServicePopupComponent", () => {
    let component: RenewServicePopupComponent;
    let fixture: ComponentFixture<RenewServicePopupComponent>;
    const mockClipboard = {
        copy: jasmine.createSpy("copy"),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RenewServicePopupComponent, TranslateModule.forRoot()],
            providers: [{ provide: ClipboardService, useValue: mockClipboard }],
        }).compileComponents();

        fixture = TestBed.createComponent(RenewServicePopupComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
