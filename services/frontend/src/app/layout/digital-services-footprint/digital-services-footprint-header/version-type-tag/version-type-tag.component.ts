import { Component, input } from "@angular/core";
import { DigitalServiceVersionType } from "src/app/core/interfaces/digital-service-version.interface";

@Component({
    selector: "app-version-type-tag",
    templateUrl: "./version-type-tag.component.html",
    styleUrl: "./version-type-tag.component.scss",
})
export class VersionTypeTagComponent {
    versionType = input<string>();
    digitalServiceVersionType = DigitalServiceVersionType;
}
