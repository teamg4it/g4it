import { Component } from "@angular/core";
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";
import { AutofocusDirective } from "./auto-focus.directive";

@Component({
    template: "",
    imports: [AutofocusDirective],
})
class TestHostComponent {}

describe("AutofocusDirective", () => {
    let fixture: ComponentFixture<TestHostComponent>;

    const createFixture = (template: string): HTMLElement => {
        TestBed.overrideComponent(TestHostComponent, { set: { template } });
        fixture = TestBed.createComponent(TestHostComponent);
        fixture.detectChanges();

        return fixture.nativeElement;
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TestHostComponent],
        }).compileComponents();
    });

    it("should focus a focusable host element", fakeAsync(() => {
        const host = createFixture('<button appAutofocus type="button">Open</button>');
        const button = host.querySelector("button") as HTMLButtonElement;

        tick();

        expect(document.activeElement).toBe(button);
    }));

    it("should focus the first focusable descendant", fakeAsync(() => {
        const host = createFixture(
            '<div appAutofocus><button type="button">First</button><input /></div>',
        );
        const button = host.querySelector("button") as HTMLButtonElement;

        tick();

        expect(document.activeElement).toBe(button);
    }));

    it("should make a non-interactive host focusable before focusing it", fakeAsync(() => {
        const host = createFixture("<h2 appAutofocus>Heading</h2>");
        const heading = host.querySelector("h2") as HTMLHeadingElement;

        tick();

        expect(heading.getAttribute("tabindex")).toBe("-1");
        expect(document.activeElement).toBe(heading);
    }));
});
