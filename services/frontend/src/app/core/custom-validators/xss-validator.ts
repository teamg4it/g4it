import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function xssFormGroupValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        if (!control.value || typeof control.value !== "object") {
            return null;
        }

        const xssPatterns = [
            /<script\b[^>]*>/i,
            /<\/script\s*>/i,
            /javascript\s*:/i,
            /on[a-z]+\s*=/i,
            /<[^>]+>/,
        ];

        const hasXss = Object.values(control.value).some(
            (value) =>
                typeof value === "string" &&
                xssPatterns.some((pattern) => pattern.test(value)),
        );

        return hasXss ? { xssDetected: true } : null;
    };
}
