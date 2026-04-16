import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { ImpactButtonComponent } from "./impact-button.component";

describe("ImpactButtonComponent", () => {
    let component: ImpactButtonComponent;
    let fixture: ComponentFixture<ImpactButtonComponent>;

    const translateServiceMock = {
        currentLang: "en",
        translations: {
            en: {
                criteria: {
                    "...": { icon: "hourglass" },
                    climate: { icon: "climate-icon" },
                    other: { icon: "other-icon" },
                },
            },
        },
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ImpactButtonComponent],
            providers: [{ provide: TranslateService, useValue: translateServiceMock }],
        }).compileComponents();

        fixture = TestBed.createComponent(ImpactButtonComponent);
        component = fixture.componentInstance;
    });

    it('should set default image and unit when impact is "..."', () => {
        component.impact = "...";

        component.ngOnInit();

        expect(component.impactImage).toBe("assets/images/icons/icon-hourglass.svg");
        expect(component.impactUnite).toBe("N/A");
    });

    it("should set impactImage based on translation", () => {
        component.impact = "climate";

        component.ngOnInit();

        expect(component.impactImage).toBe("assets/images/icons/icon-climate-icon.svg");
    });

    it("should set selectedLang from translate service", () => {
        component.ngOnInit();

        expect(component.selectedLang).toBe("en");
    });

    it("should emit selectedCriteriaChange event", () => {
        spyOn(component.selectedCriteriaChange, "emit");

        component.changeCritere("climate", "test-id");

        expect(component.selectedCriteriaChange.emit).toHaveBeenCalledWith("climate");
    });

    it("should blur the button when changeCritere is called", () => {
        const button = document.createElement("button");
        spyOn(button, "blur");

        const wrapper = document.createElement("div");
        wrapper.id = "test-id";
        wrapper.appendChild(button);
        document.body.appendChild(wrapper);

        component.changeCritere("climate", "test-id");

        expect(button.blur).toHaveBeenCalled();

        wrapper.remove();
    });

    it("should not throw error if button is not found", () => {
        expect(() => {
            component.changeCritere("climate", "invalid-id");
        }).not.toThrow();
    });
});
