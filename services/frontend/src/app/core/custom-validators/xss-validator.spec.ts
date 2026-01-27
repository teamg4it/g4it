import { FormControl, FormGroup } from "@angular/forms";
import { xssFormGroupValidator } from "./xss-validator"; // adjust path

describe("xssFormGroupValidator", () => {
    let validatorFn: ReturnType<typeof xssFormGroupValidator>;

    beforeEach(() => {
        validatorFn = xssFormGroupValidator();
    });

    it("should return null if control value is not an object", () => {
        const control = new FormControl("just a string");
        expect(validatorFn(control)).toBeNull();

        const control2 = new FormControl(null);
        expect(validatorFn(control2)).toBeNull();

        const control3 = new FormControl(42);
        expect(validatorFn(control3)).toBeNull();
    });

    it("should return null if object has no XSS patterns", () => {
        const control = new FormGroup(
            {
                name: new FormControl("John Doe"),
                email: new FormControl("john@example.com"),
            },
            { validators: validatorFn },
        );

        expect(validatorFn(control)).toBeNull();
    });

    it("should detect <script> tag", () => {
        const control = new FormGroup(
            { comment: new FormControl("<script>alert(1)</script>") },
            { validators: validatorFn },
        );

        expect(validatorFn(control)).toEqual({ xssDetected: true });
    });

    it("should detect javascript: URLs", () => {
        const control = new FormGroup(
            { url: new FormControl("javascript:alert(1)") },
            { validators: validatorFn },
        );

        expect(validatorFn(control)).toEqual({ xssDetected: true });
    });

    it("should detect inline event handlers", () => {
        const control = new FormGroup(
            { input: new FormControl('<input onmouseover="evil()">') },
            { validators: validatorFn },
        );

        expect(validatorFn(control)).toEqual({ xssDetected: true });
    });

    it("should detect other HTML tags", () => {
        const control = new FormGroup(
            { bio: new FormControl("<b>bold</b>") },
            { validators: validatorFn },
        );

        expect(validatorFn(control)).toEqual({ xssDetected: true });
    });

    it("should ignore non-string values", () => {
        const control = new FormGroup(
            { age: new FormControl(30), active: new FormControl(true) },
            { validators: validatorFn },
        );

        expect(validatorFn(control)).toBeNull();
    });

    it("should detect XSS if one of multiple fields contains XSS", () => {
        const control = new FormGroup(
            {
                name: new FormControl("Alice"),
                comment: new FormControl("<script>alert('hi')</script>"),
            },
            { validators: validatorFn },
        );

        expect(validatorFn(control)).toEqual({ xssDetected: true });
    });
});
