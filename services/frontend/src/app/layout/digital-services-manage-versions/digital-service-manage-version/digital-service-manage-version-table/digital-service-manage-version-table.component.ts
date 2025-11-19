import { Component, inject, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { DigitalServiceVersionResponse } from "src/app/core/interfaces/digital-service-version.interface";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";

@Component({
    selector: "app-digital-service-manage-version-table",
    templateUrl: "./digital-service-manage-version-table.component.html",
    styleUrl: "./digital-service-manage-version-table.component.scss",
})
export class DigitalServiceManageVersionTableComponent implements OnInit {
    private readonly digitalServiceVersionDataService = inject(
        DigitalServiceVersionDataService,
    );
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    versionData: DigitalServiceVersionResponse[] = [];
    dsVersionUid: string = "";
    selectedVersions: string[] = [];

    ngOnInit() {
        this.dsVersionUid =
            this.route.snapshot.paramMap.get("digitalServiceVersionId") ?? "";
        this.digitalServiceVersionDataService
            .getVersions(this.dsVersionUid)
            .subscribe((versions) => {
                this.versionData = versions;
            });
    }

    redirectToVersionDetails(version: string): void {
        this.router.navigate(
            ["digital-service-version", version, "footprint", "resources"],
            { relativeTo: this.route.parent?.parent },
        );
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

    duplicateDigitalServiceVersion(dsvUid: string): void {
        this.digitalServiceVersionDataService
            .duplicateVersion(dsvUid)
            .subscribe((version) => {
                this.redirectToVersionDetails(version.uid);
            });
    }
}
