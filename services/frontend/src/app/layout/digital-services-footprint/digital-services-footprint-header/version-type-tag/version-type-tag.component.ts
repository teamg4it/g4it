import { Component, input, ViewEncapsulation } from "@angular/core";
import { DigitalServiceVersionType } from "src/app/core/interfaces/digital-service-version.interface";

@Component({
    selector: "app-version-type-tag",
    templateUrl: "./version-type-tag.component.html",
    encapsulation: ViewEncapsulation.None,
})
export class VersionTypeTagComponent {
    versionType = input<string>();
    digitalServiceVersionType = DigitalServiceVersionType;
}
