/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Pipe, PipeTransform } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";

@Pipe({
    name: "monthYear",
})
export class MonthYearPipe implements PipeTransform {
    constructor(private readonly translate: TranslateService) {}

    transform(value: string | undefined): string {
        let translatedDate;
        if (!value) {
            return "";
        }
        // Check if the value is in the format MM-YYYY
        const regex = /\d{2}-\d{4}/;
        if (!regex.exec(value)) {
            return value;
        }
        const [month, year] = value.split("-");
        const date = new Date(Number(year), Number(month) - 1);
        if (this.translate.currentLang == "fr") {
            translatedDate = date.toLocaleString("fr-FR", {
                month: "long",
                year: "numeric",
            });
        } else {
            translatedDate = date.toLocaleString("en-US", {
                month: "long",
                year: "numeric",
            });
        }
        //Return with Capital letter on the month
        return translatedDate.charAt(0).toUpperCase() + translatedDate.slice(1);
    }
}
