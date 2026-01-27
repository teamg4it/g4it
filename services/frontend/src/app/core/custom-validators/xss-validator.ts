import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function xssFormGroupValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        if (!control.value || typeof control.value !== "object") {
            return null;
        }

        // Function to check a single string for XSS
        const containsXss = (value: string): boolean => {
            const lower = value.toLowerCase();
            const jsProtocolPattern = /^\s*javascript\s*:/i;

            return (
                lower.includes("<script") || // catches opening script tags
                lower.includes("</script") || // catches closing script tags
                jsProtocolPattern.test(value) || // safer for Sonar
                /\bon[a-z]{1,32}\s*=/.test(lower) || // bounded event handler attributes
                /<[^>]+>/.test(value) // bounded tag length to prevent abuse
            );
        };

        const hasXss = Object.values(control.value).some(
            (value) => typeof value === "string" && containsXss(value),
        );

        return hasXss ? { xssDetected: true } : null;
    };
}
