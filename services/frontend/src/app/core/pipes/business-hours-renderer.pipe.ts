import { Pipe, PipeTransform } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";

@Pipe({
    name: "businessHoursRenderer",
})
export class BusinessHoursRendererPipe implements PipeTransform {
    constructor(private readonly translate: TranslateService) {}

    transform(value: string): string {
        if (!value) return "";

        if (this.translate.currentLang === "en") {
            if (typeof value === "string") {
                if (value.includes("00")) {
                    return value.replace(":00", "");
                }
            }
        }
        if (this.translate.currentLang === "fr") {
            return this.transformFrLang(value);
        }
        return value;
    }

    transformFrLang(value: string): string {
        if (typeof value === "string") {
            let splitted = value.split(" ", 2);
            let time = splitted.at(0)?.split(":", 2);

            if (time) {
                let hour = time.at(0);
                if (splitted.at(1) === "PM" && hour) {
                    hour = (parseInt(hour) + 12).toString();
                }
                let min = time.at(1);
                if (hour && min) {
                    return hour.concat("h").concat(min);
                }
            }
        }
        return value;
    }
}
