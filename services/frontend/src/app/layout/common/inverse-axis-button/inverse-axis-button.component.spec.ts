import { ComponentFixture, TestBed } from "@angular/core/testing";

import { InverseAxisButtonComponent } from "./inverse-axis-button.component";

describe("InverseAxisButtonComponent", () => {
    let component: InverseAxisButtonComponent;
    let fixture: ComponentFixture<InverseAxisButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [InverseAxisButtonComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(InverseAxisButtonComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
