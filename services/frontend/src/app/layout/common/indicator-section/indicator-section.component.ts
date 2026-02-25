import { Component, input } from "@angular/core";
import { StatGroup } from "src/app/core/interfaces/indicator.interface";
import { SharedModule } from "src/app/core/shared/shared.module";

@Component({
    selector: "app-indicator-section",
    standalone: true,
    imports: [SharedModule],
    templateUrl: "./indicator-section.component.html",
    styleUrl: "./indicator-section.component.scss",
})
export class IndicatorSectionComponent {
    statGroups = input<StatGroup[]>([]);
    isApplication = input<boolean>(false);
}
