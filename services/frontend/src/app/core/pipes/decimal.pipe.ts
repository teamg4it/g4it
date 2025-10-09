import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: "decimals",
})
export class DecimalsPipe implements PipeTransform {
    transform(value: number): string {
        if (value === undefined) return "";
        if (Number.isNaN(Number(value))) return value.toString();
        if (value < 0.01 && value != 0) {
            return value.toExponential(2);
        } else if (value >= 1000) {
            return Intl.NumberFormat("fr-FR", { maximumFractionDigits: 0 }).format(value);
        } else {
            return value.toFixed(2).replace(".00", "");
        }
    }
}
