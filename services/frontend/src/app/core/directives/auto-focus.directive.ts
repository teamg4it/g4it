import { AfterViewInit, Directive, ElementRef } from "@angular/core";

@Directive({
    selector: "[appAutofocus]",
    standalone: true,
})
export class AutofocusDirective implements AfterViewInit {
    constructor(private readonly el: ElementRef<HTMLElement>) {}

    ngAfterViewInit(): void {
        setTimeout(() => {
            const element = this.el.nativeElement;

            // If the host itself is focusable
            if (this.isFocusable(element)) {
                element.focus();
                return;
            }

            // Find a focusable element inside the host
            const focusableElement = element.querySelector<HTMLElement>(
                'button, input, select, textarea, a[href], [tabindex]:not([tabindex="-1"])',
            );

            if (focusableElement) {
                focusableElement.focus();
                return;
            }

            // If there is no interactive element, focus the host itself.
            // This is useful for headings/text such as <h2>, <p>, <div>, etc.
            element.setAttribute("tabindex", "-1");
            element.focus();
        });
    }

    private isFocusable(element: HTMLElement): boolean {
        return element.matches(
            'button, input, select, textarea, a[href], [tabindex]:not([tabindex="-1"])',
        );
    }
}
