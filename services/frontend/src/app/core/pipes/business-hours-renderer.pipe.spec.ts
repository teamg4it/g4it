import { TestBed } from "@angular/core/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { BusinessHoursRendererPipe } from "./business-hours-renderer.pipe";

describe("BusinessHoursRendererPipe", () => {
    let pipe: BusinessHoursRendererPipe;
    let translateService: TranslateService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot()],
            providers: [TranslateService, BusinessHoursRendererPipe],
        }).compileComponents();

        translateService = TestBed.inject(TranslateService);
        pipe = TestBed.inject(BusinessHoursRendererPipe);
    });

    it("should return the original value if value does not contain ':00' and language is English", () => {
        translateService.currentLang = "en";
        expect(pipe.transform("10:30")).toEqual("10:30");
    });

    it("should remove ':00' from the value if language is English", () => {
        translateService.currentLang = "en";
        expect(pipe.transform("10:00")).toEqual("10");
    });

    it("should return the original value if language is neither English nor French", () => {
        translateService.currentLang = "es";
        expect(pipe.transform("10:00")).toEqual("10:00");
    });

    it("should transform the value correctly for French language", () => {
        translateService.currentLang = "fr";
        expect(pipe.transform("10:30 AM")).toEqual("10h30");
    });

    it("should transform the value correctly for French language with PM", () => {
        translateService.currentLang = "fr";
        expect(pipe.transform("10:30 PM")).toEqual("22h30");
    });

    it("should return an empty string if the value is empty", () => {
        translateService.currentLang = "en";
        expect(pipe.transform("")).toEqual("");
    });
});
