import { ComponentFixture, TestBed } from "@angular/core/testing";

import { DigitalServicesEcomindParametersComponent } from "./digital-services-ecomind-parameters.component";

describe("DigitalServicesEcomindParametersComponent", () => {
    let component: DigitalServicesEcomindParametersComponent;
    let fixture: ComponentFixture<DigitalServicesEcomindParametersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesEcomindParametersComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesEcomindParametersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
