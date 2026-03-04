import { IntegerPipe } from "./integer.pipe";

describe("IntegerPipe", () => {
    let pipe: IntegerPipe;

    beforeEach(() => {
        pipe = new IntegerPipe();
    });

    it("should create the pipe", () => {
        expect(pipe).toBeTruthy();
    });

    it("should return '0' when value is 0", () => {
        expect(pipe.transform(0)).toBe("0");
    });

    it("should return '< 1' when value is less than 1 but greater than 0", () => {
        expect(pipe.transform(0.5)).toBe("< 1");
        expect(pipe.transform(0.99)).toBe("< 1");
    });

    it("should format integer values using fr-FR locale", () => {
        expect(pipe.transform(1)).toBe("1");
        expect(pipe.transform(10)).toBe("10");
        expect(pipe.transform(1000)).toBe("1 000"); // French thousands separator
    });

    it("should round down decimals using maximumFractionDigits = 0", () => {
        expect(pipe.transform(1.4)).toBe("1");
        expect(pipe.transform(1.9)).toBe("2");
    });

    it("should parse string numbers correctly", () => {
        expect(pipe.transform("12.7" as unknown as number)).toBe("13");
        expect(pipe.transform("1000" as unknown as number)).toBe("1 000");
    });
});
