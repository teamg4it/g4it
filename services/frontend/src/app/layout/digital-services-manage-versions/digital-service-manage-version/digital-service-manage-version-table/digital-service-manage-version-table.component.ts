import { Component } from "@angular/core";
import { DigitalServiceVersionType } from "src/app/core/interfaces/digital-service-version.interface";

@Component({
    selector: "app-digital-service-manage-version-table",
    templateUrl: "./digital-service-manage-version-table.component.html",
    styleUrl: "./digital-service-manage-version-table.component.scss",
})
export class DigitalServiceManageVersionTableComponent {
    versionData = [
        {
            name: "Version 1.0",
            releaseDate: "2022-01-15",
            status: DigitalServiceVersionType.ACTIVE,
        },
        {
            name: "Version 1.1",
            releaseDate: "2022-06-20",
            status: DigitalServiceVersionType.DRAFT,
        },
        {
            name: "Version 2.0",
            releaseDate: "2023-03-10",
            status: DigitalServiceVersionType.ARCHIVED,
        },
    ];

    duplicateDigitalServiceVersion(version: string): void {
        // redirect to version
    }
}
