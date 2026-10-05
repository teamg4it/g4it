import { Component, input } from "@angular/core";
import { DigitalServiceVersionType } from "src/app/core/interfaces/digital-service-version.interface";
import { TranslatePipe } from "@ngx-translate/core";

@Component({
    selector: "app-version-type-tag",
    templateUrl: "./version-type-tag.component.html",
    standalone: true,
    imports: [TranslatePipe],
})
export class VersionTypeTagComponent {
    versionType = input<string>();
    digitalServiceVersionType = DigitalServiceVersionType;
}
