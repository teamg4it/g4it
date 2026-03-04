import { DecimalsPipe } from "./decimal.pipe";

describe("DecimalsPipe", () => {
    let pipe: DecimalsPipe;

    beforeEach(() => {
        pipe = new DecimalsPipe();
    });

    it("should create the pipe", () => {
        expect(pipe).toBeTruthy();
    });

    it("should return empty string when value is undefined", () => {
        expect(pipe.transform(undefined as unknown as number)).toBe("");
    });

    it("should return string value when input is NaN", () => {
        expect(pipe.transform(NaN)).toBe("NaN");
    });

    it("should return exponential notation when value is less than 0.01 and not zero", () => {
        expect(pipe.transform(0.009)).toBe("9.00e-3");
        expect(pipe.transform(0.0005)).toBe("5.00e-4");
    });

    it("should NOT use exponential notation when value is exactly 0", () => {
        expect(pipe.transform(0)).toBe("0");
    });

    it("should format values greater than or equal to 1000 using fr-FR locale", () => {
        expect(pipe.transform(1000)).toBe("1 000");
        expect(pipe.transform(12345.67)).toBe("12 346"); // rounded
    });

    it("should format values with two decimals and remove trailing .00", () => {
        expect(pipe.transform(1)).toBe("1");
        expect(pipe.transform(1.5)).toBe("1.50");
        expect(pipe.transform(10.25)).toBe("10.25");
    });

    it("should keep two decimals when value has non-zero decimals", () => {
        expect(pipe.transform(2.1)).toBe("2.10");
        expect(pipe.transform(2.345)).toBe("2.35");
    });

    it("should handle values between 0.01 and 999.99 correctly", () => {
        expect(pipe.transform(0.01)).toBe("0.01");
        expect(pipe.transform(0.1)).toBe("0.10");
        expect(pipe.transform(999.99)).toBe("999.99".replace(".00", "")); // rounds to 1000
    });
});
