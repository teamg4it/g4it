import { Pipe, PipeTransform } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { of } from "rxjs";

import PanelDatacenterComponent from "./datacenter.component";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Pipe({ name: "translate" })
class MockTranslatePipe implements PipeTransform {
    transform(value: string): string {
        return value;
    }
}

describe("PanelDatacenterComponent", () => {
    let component: PanelDatacenterComponent;
    let fixture: ComponentFixture<PanelDatacenterComponent>;

    const userServiceMock = {
        isAllowedDigitalServiceWrite$: of(true),
    };

    const storeMock = {
        countryMap: () => ({
            FR: "France",
            DE: "Germany",
        }),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ReactiveFormsModule],
            declarations: [
                PanelDatacenterComponent,
                MockTranslatePipe,
            ],
            providers: [
                { provide: UserService, useValue: userServiceMock },
                { provide: DigitalServiceStoreService, useValue: storeMock },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(PanelDatacenterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize form with default values", () => {
        expect(component.datacenterForm.value.name).toBe("");
        expect(component.datacenterForm.value.pue).toBe(2);
        expect(component.datacenterForm.value.country).toBe("France");
    });

    it("should set isToLow correctly", () => {
        component.datacenterForm.patchValue({ pue: 0.5 });
        component.verifyPue();
        expect(component.isToLow).toBeTrue();

        component.datacenterForm.patchValue({ pue: 2 });
        component.verifyPue();
        expect(component.isToLow).toBeFalse();
    });

    it("should disable button when name is empty", () => {
        component.datacenterForm.patchValue({ name: "   " });
        component.onInputCheck();
        expect(component.disableButton).toBeTrue();
    });

    it("should emit serverChange and close when valid", () => {
        spyOn(component.serverChange, "emit");
        spyOn(component, "close");

        component.datacenterForm.patchValue({
            name: "DC1",
            country: "France",
            pue: 2,
        });

        component.submitFormData();

        expect(component.serverChange.emit).toHaveBeenCalled();
        expect(component.close).toHaveBeenCalled();
    });

    it("should NOT emit when isToLow is true", () => {
        component.isToLow = true;
        spyOn(component.serverChange, "emit");

        component.submitFormData();

        expect(component.serverChange.emit).not.toHaveBeenCalled();
    });

    it("should close and reset form", () => {
        spyOn(component.addSidebarVisibleChange, "emit");

        component.close();

        expect(component.addSidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should toggle internal buttons visibility", () => {
        component.showInternalButtons = false;
        expect(component.hideInternalButtons).toBeTrue();
    });

    it("should return forceWriteAccess when provided", () => {
        component.forceWriteAccess = true;
        expect(component.canWrite$).toBeTruthy();
    });
});