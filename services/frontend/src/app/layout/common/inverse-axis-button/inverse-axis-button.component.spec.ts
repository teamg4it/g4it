import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateLoader, TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import { InverseAxisButtonComponent } from "./inverse-axis-button.component";

describe("InverseAxisButtonComponent", () => {
    let component: InverseAxisButtonComponent;
    let fixture: ComponentFixture<InverseAxisButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                InverseAxisButtonComponent,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: {
                            getTranslation: () => of({}),
                        },
                    },
                }),
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(InverseAxisButtonComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize with isInverted as false", () => {
        expect(component.isInverted()).toBe(false);
    });

    it("should toggle isInverted to true on first call to toggleAxis", () => {
        component.toggleAxis();
        expect(component.isInverted()).toBe(true);
    });

    it("should toggle isInverted back to false on second call to toggleAxis", () => {
        component.toggleAxis(); // First toggle: false -> true
        component.toggleAxis(); // Second toggle: true -> false
        expect(component.isInverted()).toBe(false);
    });

    it("should emit true when toggleAxis is called from initial state", () => {
        let emittedValue: boolean | undefined;
        component.inverseAxisChange.subscribe((value: boolean) => {
            emittedValue = value;
        });

        component.toggleAxis();

        expect(emittedValue).toBe(true);
    });

    it("should emit false when toggleAxis is called from inverted state", () => {
        component.isInverted.set(true); // Set to inverted state

        let emittedValue: boolean | undefined;
        component.inverseAxisChange.subscribe((value: boolean) => {
            emittedValue = value;
        });

        component.toggleAxis();

        expect(emittedValue).toBe(false);
    });

    it("should emit values on multiple toggles", () => {
        const emittedValues: boolean[] = [];
        component.inverseAxisChange.subscribe((value: boolean) => {
            emittedValues.push(value);
        });

        component.toggleAxis(); // Emit true
        component.toggleAxis(); // Emit false
        component.toggleAxis(); // Emit true again

        expect(emittedValues).toEqual([true, false, true]);
    });
});
