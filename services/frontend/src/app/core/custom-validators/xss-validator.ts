import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function xssFormGroupValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        if (!control.value || typeof control.value !== "object") {
            return null;
        }

        const xssPattern = /<script.*?>.*?<\/script>|javascript:|on\w+=|<.*?>/i;

        const hasXss = Object.values(control.value).some(
            (value) => typeof value === "string" && xssPattern.test(value),
        );

        return hasXss ? { xssDetected: true } : null;
    };
}
