import { Component, inject } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { DigitalServiceVersionType } from "src/app/core/interfaces/digital-service-version.interface";

@Component({
    selector: "app-digital-service-manage-version-table",
    templateUrl: "./digital-service-manage-version-table.component.html",
    styleUrl: "./digital-service-manage-version-table.component.scss",
})
export class DigitalServiceManageVersionTableComponent {
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);

    versionData = [
        {
            id: "1",
            name: "Version 1.0",
            releaseDate: "2022-01-15",
            status: DigitalServiceVersionType.ACTIVE,
        },
        {
            id: "2",
            name: "Version 1.1",
            releaseDate: "2022-06-20",
            status: DigitalServiceVersionType.DRAFT,
        },
        {
            id: "3",
            name: "Version 2.0",
            releaseDate: "2023-03-10",
            status: DigitalServiceVersionType.ARCHIVED,
        },
    ];
    selectedVersions: any[] = [];

    onVersionSelect(version: any): void {
        const index = this.selectedVersions.findIndex((v: any) => v === version.id);
        if (index > -1) {
            this.selectedVersions.splice(index, 1);
            version.selected = false; // Uncheck the checkbox
        } else {
            if (this.selectedVersions.length < 2) {
                this.selectedVersions.push(version.id);
                version.selected = true; // Check the checkbox
            } else {
                version.selected = false; // Prevent checking if already 2 selected
            }
        }
    }

    compareVersions(): void {
        if (this.selectedVersions.length === 2) {
            // Implement your comparison logic here
            this.router.navigate(["../compare-versions"], {
                relativeTo: this.route,
                queryParams: {
                    version1: this.selectedVersions[0],
                    version2: this.selectedVersions[1],
                },
            });
        }
    }

    duplicateDigitalServiceVersion(version: string): void {
        // redirect to version
    }
}
