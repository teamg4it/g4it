import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TranslateModule } from "@ngx-translate/core";
import { SetViewPopupComponent } from "./set-view-popup.component";

describe("SetViewPopupComponent", () => {
    let component: SetViewPopupComponent;
    let fixture: ComponentFixture<SetViewPopupComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot()],
            declarations: [SetViewPopupComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(SetViewPopupComponent);
        component = fixture.componentInstance;
        component.digitalService = { enableDataInconsistency: true } as any;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
