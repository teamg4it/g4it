import { TranslateService } from "@ngx-translate/core";
import { MonthYearPipe } from "./monthyear.pipe";

describe("MonthYearPipe", () => {
    let pipe: MonthYearPipe;
    let translateServiceMock: Partial<TranslateService>;

    beforeEach(() => {
        translateServiceMock = {
            currentLang: "en",
        };

        pipe = new MonthYearPipe(translateServiceMock as TranslateService);
    });

    it("should create the pipe", () => {
        expect(pipe).toBeTruthy();
    });

    it("should return empty string if value is undefined", () => {
        expect(pipe.transform(undefined)).toBe("");
    });

    it("should return the value unchanged if format is invalid", () => {
        const value = "2024-05";
        expect(pipe.transform(value)).toBe(value);
    });

    it("should format date in English when currentLang is en", () => {
        translateServiceMock.currentLang = "en";

        const result = pipe.transform("05-2024");

        expect(result).toBe("May 2024");
    });

    it("should format date in French when currentLang is fr", () => {
        translateServiceMock.currentLang = "fr";

        const result = pipe.transform("05-2024");

        expect(result).toBe("Mai 2024");
    });

    it("should capitalize the first letter of the month", () => {
        translateServiceMock.currentLang = "fr";

        const result = pipe.transform("01-2024");

        expect(result.charAt(0)).toBe(result.charAt(0).toUpperCase());
    });

    it("should correctly handle single-digit months with leading zero", () => {
        translateServiceMock.currentLang = "en";

        const result = pipe.transform("01-2023");

        expect(result).toBe("January 2023");
    });
});
