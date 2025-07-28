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

    it("should initialize formGroup and intitalDataState correctly on ngOnInit", () => {
        component.digitalService = { enableDataInconsistency: true } as any;
        component.ngOnInit();
        expect(component.intitalDataState).toBe(true);
        expect(component.formGroup.get("dataConsistencyCheckbox")?.value).toBe(true);
    });

    it("should set dataConsistencyCheckbox to false when resetForm is called", () => {
        component.digitalService = { enableDataInconsistency: true } as any;
        component.ngOnInit();
        component.resetForm();
        expect(component.formGroup.get("dataConsistencyCheckbox")?.value).toBe(false);
    });

    it("should emit onClose event when closePopup is called", () => {
        spyOn(component.onClose, "emit");
        component.closePopup();
        expect(component.onClose.emit).toHaveBeenCalled();
    });

    it("should initialize dataConsistencyCheckbox to false if enableDataInconsistency is undefined", () => {
        component.digitalService = {} as any;
        component.ngOnInit();
        expect(component.formGroup.get("dataConsistencyCheckbox")?.value).toBe(false);
    });
});
