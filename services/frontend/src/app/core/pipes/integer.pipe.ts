import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: "integer",
})
export class IntegerPipe implements PipeTransform {
    transform(value: number): string {
        if (value === undefined) return "";

        if (typeof value === "string") {
            value = Number.parseFloat(value);
        }
        if (value === 0) {
            return "0";
        }
        if (value < 1) {
            return "< 1";
        } else {
            return Intl.NumberFormat("fr-FR", { maximumFractionDigits: 0 }).format(value);
        }
    }
}
